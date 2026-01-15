# OCR Service (Python)

基于 PaddleOCR 的独立文字识别服务，提供 HTTP API 接口。

## 功能特性

- ✅ 支持中文、英文等多种语言识别
- ✅ 支持图片文件上传识别
- ✅ 支持本地文件路径识别
- ✅ 支持文本框坐标返回
- ✅ 支持置信度返回
- ✅ 自动角度检测和校正
- ✅ RESTful API 接口
- ✅ 自动 API 文档（Swagger UI）

## 环境要求

- Python 3.8+
- PaddlePaddle 2.5.0+
- PaddleOCR 2.7.0+

## 快速开始

### 1. 安装依赖

```bash
# 创建虚拟环境（推荐）
python3 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 安装依赖
pip install -r requirements.txt
```

### 2. 启动服务

#### 方式一：使用启动脚本（推荐）

```bash
./start.sh
```

#### 方式二：直接运行

```bash
python3 app.py
```

#### 方式三：使用 uvicorn

```bash
uvicorn app:app --host 0.0.0.0 --port 8082
```

### 3. 访问服务

- **服务地址**: http://localhost:8082
- **健康检查**: http://localhost:8082/health
- **API 文档**: http://localhost:8082/docs
- **识别接口**: http://localhost:8082/api/ocr/recognize

## 配置说明

通过环境变量配置服务：

```bash
# 服务配置
export OCR_HOST=0.0.0.0          # 服务监听地址
export OCR_PORT=8082              # 服务端口
export OCR_DEBUG=false            # 调试模式

# PaddleOCR 配置
export OCR_LANG=ch                # 识别语言（ch=中文, en=英文）
export OCR_USE_ANGLE_CLS=true     # 是否使用角度分类器
export OCR_USE_GPU=false          # 是否使用 GPU（需要安装 GPU 版 PaddlePaddle）

# 文件上传配置
export OCR_UPLOAD_DIR=./uploads   # 上传文件保存目录
export OCR_MAX_FILE_SIZE_MB=10    # 最大文件大小（MB）
export OCR_REQUEST_TIMEOUT=60      # 请求超时时间（秒）

# 日志配置
export OCR_LOG_LEVEL=INFO         # 日志级别
```

## API 接口

### 1. 健康检查

```bash
GET /health
```

**响应示例**:
```json
{
  "status": "healthy",
  "service": "OCR Service",
  "ocr_available": true,
  "message": "服务正常"
}
```

### 2. 图片文件识别

```bash
POST /api/ocr/recognize
Content-Type: multipart/form-data

file: <图片文件>
return_boxes: false (可选，是否返回文本框坐标)
return_confidence: true (可选，是否返回置信度)
```

**cURL 示例**:
```bash
curl -X POST "http://localhost:8082/api/ocr/recognize?return_boxes=false&return_confidence=true" \
  -F "file=@/path/to/image.jpg"
```

**响应示例**:
```json
{
  "success": true,
  "text": "识别出的完整文本\n多行文本",
  "details": [
    {
      "text": "识别出的完整文本",
      "confidence": 0.95,
      "box": [[10, 20], [100, 20], [100, 40], [10, 40]]
    },
    {
      "text": "多行文本",
      "confidence": 0.92
    }
  ]
}
```

### 3. 本地文件路径识别

```bash
POST /api/ocr/recognize-path?image_path=/path/to/image.jpg&return_boxes=false&return_confidence=true
```

**cURL 示例**:
```bash
curl -X POST "http://localhost:8082/api/ocr/recognize-path?image_path=/path/to/image.jpg&return_boxes=false&return_confidence=true"
```

**响应格式**: 同 `/api/ocr/recognize`

## 与 Java 服务集成

在 `local-llm-client` 模块中配置 OCR 服务端点：

```yaml
# application.yml
local-llm:
  ocr:
    endpoint: http://localhost:8082/api/ocr/recognize-path
    enabled: true
    timeout: 30
```

## 性能优化

### GPU 加速

如果系统有 NVIDIA GPU，可以启用 GPU 加速：

```bash
# 安装 GPU 版 PaddlePaddle
pip install paddlepaddle-gpu

# 启用 GPU
export OCR_USE_GPU=true
```

### 模型优化

首次运行会自动下载模型文件（约 100-200MB），下载位置：
- Linux/Mac: `~/.paddleocr/`
- Windows: `C:\Users\<用户名>\.paddleocr\`

可以预先下载模型或使用自定义模型路径：

```bash
export OCR_DET_MODEL_DIR=/path/to/det/model
export OCR_REC_MODEL_DIR=/path/to/rec/model
export OCR_CLS_MODEL_DIR=/path/to/cls/model
```

## 故障排查

### 1. 模型下载失败

如果模型下载失败，可以手动下载：

```bash
# 创建模型目录
mkdir -p ~/.paddleocr/whl

# 下载模型文件（示例，实际 URL 可能不同）
# 可以从 PaddleOCR GitHub 仓库获取下载链接
```

### 2. 内存不足

如果遇到内存不足，可以：
- 减少并发请求
- 使用更小的图片
- 关闭角度分类器：`export OCR_USE_ANGLE_CLS=false`

### 3. 识别准确率低

- 确保图片清晰度足够
- 尝试启用角度分类器：`export OCR_USE_ANGLE_CLS=true`
- 检查图片格式是否支持

## 开发说明

### 项目结构

```
ocr-service-python/
├── app.py              # FastAPI 主程序
├── ocr_service.py      # OCR 服务封装
├── config.py          # 配置文件
├── requirements.txt   # 依赖列表
├── start.sh          # 启动脚本
├── README.md         # 说明文档
└── uploads/          # 上传文件目录（自动创建）
```

### 扩展开发

1. **添加新语言支持**: 修改 `config.py` 中的 `OCR_LANG`
2. **自定义模型**: 通过环境变量指定模型路径
3. **添加新接口**: 在 `app.py` 中添加新的路由

## 许可证

本项目使用与主项目相同的许可证。

