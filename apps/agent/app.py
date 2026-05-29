from flask import Flask
from flask_cors import CORS
import logging
from extensions import limiter
from config import Config
from routes.chat import chat_bp
# 初始化 Flask
app = Flask(__name__)
app.config.from_object(Config)

limiter.init_app(app)

# CORS
CORS(app, resources={r"/api/*": {"origins": Config.ALLOWED_ORIGINS}})
# 日志
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)

# 注册 Blueprint
app.register_blueprint(chat_bp)

if __name__ == "__main__":
    app.run(
        host=Config.FLASK_HOST,
        port=Config.FLASK_PORT,
        debug=Config.FLASK_DEBUG
    )
