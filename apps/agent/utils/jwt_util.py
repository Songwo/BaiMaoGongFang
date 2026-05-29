import jwt
from config import Config
from jwt import ExpiredSignatureError, InvalidTokenError

def decode_token(token: str):
    """
    解码并验证 JWT Token
    """
    try:
        decoded = jwt.decode(token, Config.JWT_SECRET, algorithms=[Config.JWT_ALGORITHM])
        return decoded  # 包含用户信息、权限等
    except ExpiredSignatureError:
        raise Exception("Token has expired")
    except InvalidTokenError:
        raise Exception("Invalid token")

def get_user_info_from_token(token: str):
    """
    获取用户名、用户ID等（根据你JWT结构）
    """
    payload = decode_token(token)
    username = payload.get("sub")  # subject
    user_id = payload.get("userId")
    return username, user_id
