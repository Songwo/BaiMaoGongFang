from flask import Blueprint, request, jsonify
from extensions import limiter
from datetime import datetime
import logging
from clients.openai_client import openai
from clients.redis_client import redis_client
from clients.mongo_client import archive
from config import Config
from utils.jwt_util import decode_token
import json
import time

logger = logging.getLogger(__name__)

chat_bp = Blueprint("chat", __name__, url_prefix="/api/chat")


@chat_bp.route("/instance", methods=["POST"])
def create_instance():
    """
    前端调用：POST /api/lab/instance
    请求体 JSON 包含：
      - userId: 用户 ID
      - challengeId: 挑战 ID
      - labUrl: 实验环境 URL
      - startTime: 开始时间戳（毫秒）
      - duration: 持续时间（分钟）
    将数据存入 Redis，并设置过期时间为 startTime+duration - 当前时间
    返回：{ "message": "Instance created", "challengeId": ..., "expireIn": 秒 }
    """
    data = request.get_json()
    required = ("userId", "challengeId", "labUrl", "startTime", "duration")
    if not data or not all(field in data for field in required):
        return jsonify({"error": "Missing required fields"}), 400

    user_id = data["userId"]
    challenge_id = data["challengeId"]
    lab_url = data["labUrl"]
    start_time = int(data["startTime"])  # 毫秒
    duration_minutes = int(data["duration"])

    duration_ms = duration_minutes * 60 * 1000
    now = int(time.time() * 1000)
    remaining_ms = start_time + duration_ms - now
    remaining_seconds = max(remaining_ms // 1000, 0)

    redis_key = f"lab:instance:{user_id}:{challenge_id}"
    redis_client.set(redis_key, json.dumps({
        "labUrl": lab_url,
        "startTime": start_time,
        "duration": duration_ms // 1000  # 存入秒
    }))
    redis_client.expire(redis_key, remaining_seconds)

    return jsonify({
        "message": "Instance created",
        "challengeId": challenge_id,
        "expireIn": remaining_seconds
    }), 201


@chat_bp.route("/instance", methods=["GET"])
def restore_instance():
    """
    前端调用：GET /api/lab/instance?userId=...&challengeId=...
    从 Redis 获取实例信息，判断是否还在运行。
    返回：
      - { "running": true, "labUrl": ..., "remaining": 剩余毫秒 }
      - { "running": false }
    """
    user_id = request.args.get("userId")
    challenge_id = request.args.get("challengeId")
    if not user_id or not challenge_id:
        return jsonify({"error": "Missing userId or challengeId"}), 400

    redis_key = f"lab:instance:{user_id}:{challenge_id}"
    raw = redis_client.get(redis_key)
    if not raw:
        return jsonify({"running": False})

    record = json.loads(raw)
    start_time = int(record.get("startTime", 0))
    duration = int(record.get("duration", 0))  # 秒
    lab_url = record.get("labUrl")

    now = int(time.time() * 1000)
    end_time = start_time + duration * 1000

    if now < end_time:
        remaining = end_time - now
        return jsonify({
            "running": True,
            "labUrl": lab_url,
            "remaining": remaining
        })
    else:
        redis_client.delete(redis_key)
        return jsonify({"running": False})

@chat_bp.route("", methods=["POST"])
@limiter.limit(Config.CHAT_LIMIT)
def chat():
    try:
        # 1) 解析 Token 获取 user_id
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"error": "Missing or invalid token"}), 401
        token = auth_header.split(" ")[1]
        payload = decode_token(token)
        if not payload or not isinstance(payload, dict):
            return jsonify({"error": "Invalid or expired token"}), 401

        user_id = payload.get("userId")
        if not user_id:
            return jsonify({"error": "Invalid token payload: missing userId"}), 401

        # 2) 解析用户输入
        data = request.get_json()
        if not data or "message" not in data:
            return jsonify({"error": "Missing message"}), 400
        user_message = data["message"].strip()
        if not user_message:
            return jsonify({"error": "Empty message"}), 400

        # 3) Redis 缓存上下文
        redis_key = f"chat:user:{user_id}"
        history = redis_client.lrange(redis_key, 0, -1)[::-1]  # 从 Redis 读取历史记录，倒序读取

        # 构建聊天消息
        messages = [
            {
                "role": "system",
                "content": (
                    "You are a seasoned network security expert. "
                    "You have deep knowledge of network protocols, firewalls, "
                    "intrusion detection systems, vulnerability assessment, "
                    "and incident response. Provide clear, actionable recommendations."
                )
            }
        ]

        # Redis 消息格式化
        for item in history:
            if isinstance(item, bytes):
                item = item.decode("utf-8")
            role, content = item.split(":", 1)
            logger.debug(f"Loaded history role: {role}, content: {content}")
            messages.append({"role": role.lower(), "content": content})

        # 添加用户的新消息
        messages.append({"role": "user", "content": user_message})

        # 4) MongoDB 获取历史（选填）
        user_record = archive.find_one({"user_id": user_id})
        if user_record and "messages" in user_record:
            for msg in user_record["messages"][-10:]:
                messages.insert(-1, {"role": msg["role"], "content": msg["content"]})

        # 5) 调用大模型
        resp = openai.chat.completions.create(
            model="deepseek-chat",
            messages=messages,
            stream=False
        )
        bot_reply = resp.choices[0].message.content

        # 6) 写入 Redis
        redis_client.lpush(redis_key, f"User:{user_message}")
        redis_client.lpush(redis_key, f"Assistant:{bot_reply}")
        redis_client.expire(redis_key, 3600)  # 设置过期时间
        redis_client.ltrim(redis_key, 0, 19)  # 保持最多 20 条记录

        # 7) 写入 MongoDB
        archive.update_one(
            {"user_id": user_id},
            {
                "$push": {
                    "messages": {"role": "user", "content": user_message, "ts": datetime.utcnow()}
                },
                "$setOnInsert": {"created_at": datetime.utcnow()}
            },
            upsert=True
        )
        archive.update_one(
            {"user_id": user_id},
            {"$push": {"messages": {"role": "assistant", "content": bot_reply, "ts": datetime.utcnow()}}}
        )

        # 返回响应
        return jsonify({"reply": bot_reply})

    except Exception as e:
        logger.error(f"Chat error: {e}")
        return jsonify({"error": "Internal server error"}), 500
@chat_bp.route("/history", methods=["GET"])
def get_chat_history():
    try:
        # 获取并解析 token
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"error": "Missing or invalid token"}), 401
        token = auth_header.split(" ")[1]
        payload = decode_token(token)
        if not payload or not isinstance(payload, dict):
            return jsonify({"error": "Invalid or expired token"}), 401

        user_id = payload.get("userId")
        if not user_id:
            return jsonify({"error": "Invalid token payload: missing userId"}), 401

        # 获取 Redis 聊天记录
        redis_key = f"chat:user:{user_id}"
        history = redis_client.lrange(redis_key, 0, -1)[::-1]  # 倒序取出
        chat_history = []

        for item in history:
            if isinstance(item, bytes):
                item = item.decode("utf-8")
            role, content = item.split(":", 1)
            chat_history.append({"role": role.lower(), "content": content})

        return jsonify({"history": chat_history})

    except Exception as e:
        logger.error(f"History error: {e}")
        return jsonify({"error": "Internal server error"}), 500

