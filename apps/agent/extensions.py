# extensions.py
import redis
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from config import Config
# 配置 Redis 存储
redis_uri = f"redis://{Config.REDIS_HOST}:{Config.REDIS_PORT}/{Config.REDIS_DB}"
limiter = Limiter(
    key_func=get_remote_address,
    storage_uri='redis://localhost:6379/0',
    default_limits=None
)
