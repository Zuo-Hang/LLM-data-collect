"""
PaddleOCR 服务封装
"""
import logging
import os
from typing import List, Optional, Tuple, Dict, Any
import cv2
import numpy as np
from PIL import Image
from paddleocr import PaddleOCR

logger = logging.getLogger(__name__)


class OCRService:
    """PaddleOCR 服务封装类"""
    
    def __init__(self, 
                 lang: str = "ch",
                 use_angle_cls: bool = True,
                 use_gpu: bool = False,
                 det_model_dir: Optional[str] = None,
                 rec_model_dir: Optional[str] = None,
                 cls_model_dir: Optional[str] = None):
        """
        初始化 OCR 服务
        
        Args:
            lang: 识别语言，支持 'ch'（中文）、'en'（英文）等
            use_angle_cls: 是否使用角度分类器
            use_gpu: 是否使用 GPU
            det_model_dir: 检测模型目录（可选）
            rec_model_dir: 识别模型目录（可选）
            cls_model_dir: 分类模型目录（可选）
        """
        self.lang = lang
        self.use_angle_cls = use_angle_cls
        self.use_gpu = use_gpu
        self.det_model_dir = det_model_dir
        self.rec_model_dir = rec_model_dir
        self.cls_model_dir = cls_model_dir
        
        self.ocr = None
        self._initialized = False
        
    def _initialize(self):
        """延迟初始化 OCR 模型（首次调用时加载）"""
        if self._initialized:
            return
            
        try:
            logger.info(f"正在初始化 PaddleOCR (lang={self.lang}, use_angle_cls={self.use_angle_cls}, use_gpu={self.use_gpu})...")
            
            init_params = {
                "lang": self.lang,
                "use_angle_cls": self.use_angle_cls,
            }
            
            # 注意：新版本 PaddleOCR 不再支持 use_gpu 参数
            # GPU 支持通过安装 GPU 版 PaddlePaddle 自动启用
            # if self.use_gpu:
            #     init_params["use_gpu"] = self.use_gpu
            
            if self.det_model_dir:
                init_params["det_model_dir"] = self.det_model_dir
            if self.rec_model_dir:
                init_params["rec_model_dir"] = self.rec_model_dir
            if self.cls_model_dir:
                init_params["cls_model_dir"] = self.cls_model_dir
                
            self.ocr = PaddleOCR(**init_params)
            self._initialized = True
            logger.info("PaddleOCR 初始化成功")
        except Exception as e:
            logger.error(f"PaddleOCR 初始化失败: {e}", exc_info=True)
            raise
    
    def recognize(self, 
                  image_path: str,
                  return_boxes: bool = False,
                  return_confidence: bool = True) -> Dict[str, Any]:
        """
        识别图片中的文字
        
        Args:
            image_path: 图片路径
            return_boxes: 是否返回文本框坐标
            return_confidence: 是否返回置信度
            
        Returns:
            {
                "text": "识别出的完整文本",
                "details": [
                    {
                        "text": "单行文本",
                        "confidence": 0.95,
                        "box": [[x1, y1], [x2, y2], [x3, y3], [x4, y4]]  # 可选
                    },
                    ...
                ]
            }
        """
        if not self._initialized:
            self._initialize()
        
        if not os.path.exists(image_path):
            raise FileNotFoundError(f"图片文件不存在: {image_path}")
        
        try:
            # 执行 OCR 识别
            # 注意：新版本 PaddleOCR 的 ocr() 方法不再支持 cls 参数
            # 角度分类在初始化时通过 use_angle_cls 控制
            result = self.ocr.ocr(image_path)
            
            if not result or not result[0]:
                return {
                    "text": "",
                    "details": []
                }
            
            page_result = result[0]
            
            # 解析结果
            text_lines = []
            details = []
            
            # PaddleOCR 3.x 新版本返回格式：字典格式
            if isinstance(page_result, dict):
                rec_texts = page_result.get('rec_texts', [])
                rec_scores = page_result.get('rec_scores', [])
                rec_polys = page_result.get('rec_polys', [])
                
                # 确保所有列表长度一致
                min_len = min(len(rec_texts), len(rec_scores))
                
                for i in range(min_len):
                    text = rec_texts[i] if i < len(rec_texts) else ""
                    confidence = float(rec_scores[i]) if i < len(rec_scores) else 0.0
                    box = rec_polys[i].tolist() if i < len(rec_polys) and rec_polys[i] is not None else None
                    
                    if text:  # 只添加非空文本
                        text_lines.append(text)
                        
                        detail = {
                            "text": text,
                            "confidence": confidence
                        }
                        
                        if return_boxes and box:
                            detail["box"] = box
                            
                        details.append(detail)
            
            # PaddleOCR 2.x 旧版本返回格式：列表格式
            elif isinstance(page_result, list):
                for line in page_result:
                    if not line:
                        continue
                    
                    try:
                        box = line[0]  # 文本框坐标
                        text_info = line[1]  # (text, confidence) 或 dict
                        
                        # 处理不同的返回格式
                        if isinstance(text_info, tuple):
                            text = text_info[0] if len(text_info) > 0 else ""
                            confidence = float(text_info[1]) if len(text_info) > 1 else 0.0
                        elif isinstance(text_info, dict):
                            text = text_info.get("text", "")
                            confidence = float(text_info.get("confidence", 0.0))
                        elif isinstance(text_info, str):
                            text = text_info
                            confidence = 1.0
                        else:
                            text = str(text_info) if text_info else ""
                            confidence = 0.0
                    except (IndexError, TypeError, ValueError) as e:
                        logger.warning(f"解析 OCR 结果行时出错: line={line}, error={e}")
                        continue
                    
                    if text:  # 只添加非空文本
                        text_lines.append(text)
                        
                        detail = {
                            "text": text,
                            "confidence": confidence
                        }
                        
                        if return_boxes:
                            detail["box"] = box
                            
                        details.append(detail)
            
            # 合并所有文本
            full_text = "\n".join(text_lines)
            
            result_dict = {
                "text": full_text,
                "details": details
            }
            
            if not return_confidence:
                # 移除置信度信息
                for detail in result_dict["details"]:
                    detail.pop("confidence", None)
            
            # 打印识别结果日志（仅统计信息，不打印具体内容）
            logger.info(f"OCR 识别完成: image_path={image_path}, 识别文本行数={len(text_lines)}")
            
            return result_dict
            
        except Exception as e:
            logger.error(f"OCR 识别失败: image_path={image_path}, error={e}", exc_info=True)
            raise
    
    def recognize_from_bytes(self,
                            image_bytes: bytes,
                            return_boxes: bool = False,
                            return_confidence: bool = True) -> Dict[str, Any]:
        """
        从字节流识别文字
        
        Args:
            image_bytes: 图片字节流
            return_boxes: 是否返回文本框坐标
            return_confidence: 是否返回置信度
            
        Returns:
            同 recognize 方法
        """
        if not self._initialized:
            self._initialize()
        
        try:
            # 将字节流转换为 numpy 数组
            nparr = np.frombuffer(image_bytes, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
            
            if img is None:
                raise ValueError("无法解析图片字节流")
            
            # 执行 OCR 识别
            # 注意：新版本 PaddleOCR 的 ocr() 方法不再支持 cls 参数
            # 角度分类在初始化时通过 use_angle_cls 控制
            result = self.ocr.ocr(img)
            
            if not result or not result[0]:
                return {
                    "text": "",
                    "details": []
                }
            
            page_result = result[0]
            
            # 解析结果（与 recognize 方法相同）
            text_lines = []
            details = []
            
            # PaddleOCR 3.x 新版本返回格式：字典格式
            if isinstance(page_result, dict):
                rec_texts = page_result.get('rec_texts', [])
                rec_scores = page_result.get('rec_scores', [])
                rec_polys = page_result.get('rec_polys', [])
                
                # 确保所有列表长度一致
                min_len = min(len(rec_texts), len(rec_scores))
                
                for i in range(min_len):
                    text = rec_texts[i] if i < len(rec_texts) else ""
                    confidence = float(rec_scores[i]) if i < len(rec_scores) else 0.0
                    box = rec_polys[i].tolist() if i < len(rec_polys) and rec_polys[i] is not None else None
                    
                    if text:  # 只添加非空文本
                        text_lines.append(text)
                        
                        detail = {
                            "text": text,
                            "confidence": confidence
                        }
                        
                        if return_boxes and box:
                            detail["box"] = box
                            
                        details.append(detail)
            
            # PaddleOCR 2.x 旧版本返回格式：列表格式
            elif isinstance(page_result, list):
                for line in page_result:
                    if not line:
                        continue
                    
                    try:
                        box = line[0]
                        text_info = line[1]
                        
                        # 处理不同的返回格式
                        if isinstance(text_info, tuple):
                            text = text_info[0] if len(text_info) > 0 else ""
                            confidence = float(text_info[1]) if len(text_info) > 1 else 0.0
                        elif isinstance(text_info, dict):
                            text = text_info.get("text", "")
                            confidence = float(text_info.get("confidence", 0.0))
                        elif isinstance(text_info, str):
                            text = text_info
                            confidence = 1.0
                        else:
                            text = str(text_info) if text_info else ""
                            confidence = 0.0
                    except (IndexError, TypeError, ValueError) as e:
                        logger.warning(f"解析 OCR 结果行时出错: line={line}, error={e}")
                        continue
                    
                    if text:  # 只添加非空文本
                        text_lines.append(text)
                        
                        detail = {
                            "text": text,
                            "confidence": confidence
                        }
                        
                        if return_boxes:
                            detail["box"] = box
                            
                        details.append(detail)
            
            full_text = "\n".join(text_lines)
            
            result_dict = {
                "text": full_text,
                "details": details
            }
            
            if not return_confidence:
                for detail in result_dict["details"]:
                    detail.pop("confidence", None)
            
            # 打印识别结果日志（仅统计信息，不打印具体内容）
            logger.info(f"OCR 识别完成（字节流）, 识别文本行数={len(text_lines)}")
            
            return result_dict
            
        except Exception as e:
            logger.error(f"OCR 识别失败（字节流）: error={e}", exc_info=True)
            raise
    
    def is_available(self) -> bool:
        """检查 OCR 服务是否可用"""
        try:
            if not self._initialized:
                self._initialize()
            return self.ocr is not None
        except Exception as e:
            logger.error(f"OCR 服务不可用: {e}")
            return False

