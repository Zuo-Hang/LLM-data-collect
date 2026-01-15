"""
OCR 服务主程序 - FastAPI 实现
"""
import logging
import os
import uuid
from pathlib import Path
from typing import Optional

from fastapi import FastAPI, File, UploadFile, HTTPException, Query
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

import config
from ocr_service import OCRService

# 配置日志
logging.basicConfig(
    level=getattr(logging, config.LOG_LEVEL),
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s"
)
logger = logging.getLogger(__name__)

# 创建 FastAPI 应用
app = FastAPI(
    title="OCR Service",
    description="基于 PaddleOCR 的文字识别服务",
    version="1.0.0"
)

# 配置 CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应限制具体域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 初始化 OCR 服务
ocr_service = OCRService(
    lang=config.OCR_LANG,
    use_angle_cls=config.OCR_USE_ANGLE_CLS,
    use_gpu=config.OCR_USE_GPU,
    det_model_dir=config.OCR_DET_MODEL_DIR,
    rec_model_dir=config.OCR_REC_MODEL_DIR,
    cls_model_dir=config.OCR_CLS_MODEL_DIR
)

# 确保上传目录存在
upload_dir = Path(config.UPLOAD_DIR)
upload_dir.mkdir(parents=True, exist_ok=True)


# 响应模型
class OCRResponse(BaseModel):
    """OCR 识别响应"""
    success: bool
    text: Optional[str] = None
    details: Optional[list] = None
    error: Optional[str] = None


class HealthResponse(BaseModel):
    """健康检查响应"""
    status: str
    service: str
    ocr_available: bool
    message: Optional[str] = None


@app.get("/", tags=["Root"])
async def root():
    """根路径，返回服务信息"""
    return {
        "service": "OCR Service",
        "version": "1.0.0",
        "description": "基于 PaddleOCR 的文字识别服务",
        "endpoints": {
            "health": "/health",
            "ocr": "/api/ocr/recognize",
            "docs": "/docs"
        }
    }


@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    """健康检查接口"""
    ocr_available = ocr_service.is_available()
    return HealthResponse(
        status="healthy" if ocr_available else "degraded",
        service="OCR Service",
        ocr_available=ocr_available,
        message="服务正常" if ocr_available else "OCR 模型未初始化"
    )


@app.post("/api/ocr/recognize", response_model=OCRResponse, tags=["OCR"])
async def recognize_image(
    file: UploadFile = File(..., description="要识别的图片文件"),
    return_boxes: bool = Query(False, description="是否返回文本框坐标"),
    return_confidence: bool = Query(True, description="是否返回置信度")
):
    """
    识别图片中的文字
    
    - **file**: 图片文件（支持 jpg, png, bmp, gif, webp）
    - **return_boxes**: 是否返回文本框坐标
    - **return_confidence**: 是否返回置信度
    """
    try:
        # 1. 验证文件类型
        file_ext = Path(file.filename).suffix.lower()
        if file_ext not in config.ALLOWED_EXTENSIONS:
            raise HTTPException(
                status_code=400,
                detail=f"不支持的文件类型: {file_ext}。支持的类型: {', '.join(config.ALLOWED_EXTENSIONS)}"
            )
        
        # 2. 验证文件大小
        file_content = await file.read()
        file_size_mb = len(file_content) / (1024 * 1024)
        if file_size_mb > config.MAX_FILE_SIZE_MB:
            raise HTTPException(
                status_code=400,
                detail=f"文件大小超过限制: {file_size_mb:.2f}MB > {config.MAX_FILE_SIZE_MB}MB"
            )
        
        # 3. 保存文件（可选，用于调试）
        if config.DEBUG:
            # 注意：DEBUG 模式会保存上传的文件，生产环境建议关闭
            saved_path = upload_dir / f"{uuid.uuid4()}{file_ext}"
            saved_path.write_bytes(file_content)
            logger.debug(f"文件已保存: {saved_path}")
        
        # 4. 执行 OCR 识别
        logger.info(f"开始 OCR 识别: filename={file.filename}, size={file_size_mb:.2f}MB")
        
        result = ocr_service.recognize_from_bytes(
            file_content,
            return_boxes=return_boxes,
            return_confidence=return_confidence
        )
        
        text_length = len(result.get('text', ''))
        logger.info(f"OCR 识别完成: filename={file.filename}, text_length={text_length}")
        
        return OCRResponse(
            success=True,
            text=result.get("text", ""),
            details=result.get("details", [])
        )
        
    except HTTPException:
        raise
    except FileNotFoundError as e:
        logger.error(f"文件不存在: {e}")
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        logger.error(f"OCR 识别失败: {e}", exc_info=True)
        return OCRResponse(
            success=False,
            error=f"OCR 识别失败: {str(e)}"
        )


@app.post("/api/ocr/recognize-path", response_model=OCRResponse, tags=["OCR"])
async def recognize_image_path(
    image_path: str = Query(..., description="图片文件路径（本地路径）"),
    return_boxes: bool = Query(False, description="是否返回文本框坐标"),
    return_confidence: bool = Query(True, description="是否返回置信度")
):
    """
    通过文件路径识别图片中的文字
    
    - **image_path**: 图片文件路径（本地路径，支持 file:// 协议）
    - **return_boxes**: 是否返回文本框坐标
    - **return_confidence**: 是否返回置信度
    """
    try:
        # 处理 file:// 协议
        if image_path.startswith("file://"):
            from urllib.parse import urlparse
            parsed = urlparse(image_path)
            image_path = parsed.path
        
        # 验证文件是否存在
        if not os.path.exists(image_path):
            raise HTTPException(status_code=404, detail=f"文件不存在: {image_path}")
        
        # 执行 OCR 识别
        logger.info(f"开始 OCR 识别（路径）: path={image_path}")
        
        result = ocr_service.recognize(
            image_path,
            return_boxes=return_boxes,
            return_confidence=return_confidence
        )
        
        text_length = len(result.get('text', ''))
        logger.info(f"OCR 识别完成（路径）: path={image_path}, text_length={text_length}")
        
        return OCRResponse(
            success=True,
            text=result.get("text", ""),
            details=result.get("details", [])
        )
        
    except HTTPException:
        raise
    except FileNotFoundError as e:
        logger.error(f"文件不存在: {e}")
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        logger.error(f"OCR 识别失败: {e}", exc_info=True)
        return OCRResponse(
            success=False,
            error=f"OCR 识别失败: {str(e)}"
        )


if __name__ == "__main__":
    import uvicorn
    
    logger.info(f"启动 OCR 服务: http://{config.HOST}:{config.PORT}")
    logger.info(f"API 文档: http://{config.HOST}:{config.PORT}/docs")
    
    uvicorn.run(
        "app:app",
        host=config.HOST,
        port=config.PORT,
        reload=config.DEBUG,
        log_level=config.LOG_LEVEL.lower()
    )

