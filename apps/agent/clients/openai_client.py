from openai import OpenAI
from config import Config

# 初始化 DeepSeek/OpenAI 客户端
openai = OpenAI(
    api_key=Config.DEEPSEEK_API_KEY,
    base_url=Config.DEEPSEEK_API_URL
)
