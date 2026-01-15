"""
OCR 服务配置文件
"""
import os
from typing import List

# 服务配置
HOST = os.getenv("OCR_HOST", "0.0.0.0")
PORT = int(os.getenv("OCR_PORT", "8082"))
DEBUG = os.getenv("OCR_DEBUG", "false").lower() == "true"

# PaddleOCR 配置
OCR_LANG = os.getenv("OCR_LANG", "ch")  # 支持的语言：ch（中文）、en（英文）等
OCR_USE_ANGLE_CLS = os.getenv("OCR_USE_ANGLE_CLS", "true").lower() == "true"  # 是否使用角度分类器
OCR_USE_GPU = os.getenv("OCR_USE_GPU", "false").lower() == "true"  # 是否使用 GPU
OCR_DET_MODEL_DIR = os.getenv("OCR_DET_MODEL_DIR", None)  # 检测模型目录（可选）
OCR_REC_MODEL_DIR = os.getenv("OCR_REC_MODEL_DIR", None)  # 识别模型目录（可选）
OCR_CLS_MODEL_DIR = os.getenv("OCR_CLS_MODEL_DIR", None)  # 分类模型目录（可选）

# 文件上传配置
UPLOAD_DIR = os.getenv("OCR_UPLOAD_DIR", "./uploads")
MAX_FILE_SIZE_MB = int(os.getenv("OCR_MAX_FILE_SIZE_MB", "10"))
ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".bmp", ".gif", ".webp"}

# 请求超时配置（秒）
REQUEST_TIMEOUT = int(os.getenv("OCR_REQUEST_TIMEOUT", "60"))

# 日志配置
LOG_LEVEL = os.getenv("OCR_LOG_LEVEL", "INFO")

