from pymongo import MongoClient
from config import Config

mongo_client = MongoClient(Config.MONGODB_URI)
db = mongo_client["csnet"]
archive = db["ai_conversations"]
