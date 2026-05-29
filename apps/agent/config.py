import os
from dotenv import load_dotenv

# 自动加载根目录下的 .env
load_dotenv()

class Config:
    # Flask
    FLASK_HOST = os.getenv("FLASK_HOST", "0.0.0.0")
    FLASK_PORT = int(os.getenv("FLASK_PORT", 5000))
    FLASK_ENV = os.getenv("FLASK_ENV", "production")
    FLASK_DEBUG = os.getenv("FLASK_DEBUG", "0") == "1"

    # CORS
    ALLOWED_ORIGINS = os.getenv("ALLOWED_ORIGINS", "*")

    # Rate Limiter
    DEFAULT_LIMITS = ["30 per minute", "1 per second"]
    CHAT_LIMIT = "10/minute"

    # OpenAI / DeepSeek
    DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY")
    DEEPSEEK_API_URL = os.getenv("DEEPSEEK_API_URL", "https://api.deepseek.com")

    # Redis
    REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
    REDIS_PORT = int(os.getenv("REDIS_PORT", 6379))
    REDIS_DB = int(os.getenv("REDIS_DB", 0))

    # MongoDB
    MONGODB_URI = os.getenv("MONGODB_URI", "mongodb://localhost:27017")

    #jwt
    JWT_SECRET = os.getenv("JWT_SECRET")
    JWT_ALGORITHM = "HS256"

    DEFAULT_LIMIT = "100 per hour"  # 每小时允许最多访问1000次

