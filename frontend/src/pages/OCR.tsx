import { useState, useEffect } from 'react'
import {
  Card,
  Button,
  Space,
  Typography,
  Alert,
  Spin,
  Tag,
  Row,
  Col,
  Upload,
  message,
  Divider,
} from 'antd'
import {
  ClearOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  UploadOutlined,
  DeleteOutlined,
  FileTextOutlined,
} from '@ant-design/icons'
import type { UploadFile } from 'antd'
import { recognizeImageFile, recognizeImagePath, checkOCRHealth, OCRRecognizeResponse, OCRDetail } from '../api/ocr'
import './OCR.css'

const { Title, Text } = Typography

const OCR = () => {
  const [uploadedFile, setUploadedFile] = useState<UploadFile | null>(null)
  const [imageUrl, setImageUrl] = useState('')
  const [ocrResult, setOcrResult] = useState<OCRRecognizeResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [serviceStatus, setServiceStatus] = useState<{
    available: boolean
    loading: boolean
  }>({ available: false, loading: true })

  // 检查服务状态
  const checkServiceStatus = async () => {
    setServiceStatus((prev) => ({ ...prev, loading: true }))
    try {
      const health = await checkOCRHealth()
      setServiceStatus({ available: health.ocr_available, loading: false })
    } catch (err) {
      setServiceStatus({ available: false, loading: false })
    }
  }

  // 组件加载时检查服务状态
  useEffect(() => {
    checkServiceStatus()
  }, [])

  // OCR 识别
  const handleRecognize = async () => {
    if (!imageUrl && !uploadedFile) {
      setError('请先上传图片或输入图片路径')
      return
    }

    setLoading(true)
    setError(null)
    setOcrResult(null)

    try {
      let response: OCRRecognizeResponse

      if (uploadedFile && uploadedFile.originFileObj) {
        // 使用文件上传方式（返回位置信息）
        response = await recognizeImageFile(uploadedFile.originFileObj, true, true)
      } else if (imageUrl) {
        // 使用文件路径方式（返回位置信息）
        response = await recognizeImagePath(imageUrl, true, true)
      } else {
        throw new Error('没有可识别的图片')
      }

      if (response.success) {
        setOcrResult(response)
        message.success('OCR 识别成功')
      } else {
        setError(response.error || 'OCR 识别失败')
        message.error(response.error || 'OCR 识别失败')
      }
    } catch (err: any) {
      console.error('OCR 识别失败:', err)
      const errorMsg = err.message || '识别失败，请检查服务是否运行'
      setError(errorMsg)
      message.error(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  // 文件上传处理
  const handleUpload = async (file: File) => {
    setUploading(true)
    setError(null)
    
    try {
      // 验证文件类型
      const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
      if (!validTypes.includes(file.type)) {
        throw new Error('不支持的文件类型，请上传图片文件（jpg, png, gif, webp, bmp）')
      }

      // 验证文件大小（10MB）
      const maxSize = 10 * 1024 * 1024
      if (file.size > maxSize) {
        throw new Error('文件大小不能超过 10MB')
      }

      setUploadedFile({
        uid: file.name,
        name: file.name,
        status: 'done',
        originFileObj: file,
      } as UploadFile)
      
      // 自动识别
      setImageUrl('')
      message.success('图片上传成功，可以开始识别')
    } catch (err: any) {
      const errorMsg = err.message || '上传失败'
      setError(errorMsg)
      message.error(errorMsg)
    } finally {
      setUploading(false)
    }
    
    return false // 阻止默认上传行为
  }

  // 删除上传的文件
  const handleRemoveFile = () => {
    setImageUrl('')
    setUploadedFile(null)
    setOcrResult(null)
  }

  // 清空输入
  const handleClear = () => {
    setImageUrl('')
    setUploadedFile(null)
    setOcrResult(null)
    setError(null)
  }

  // 计算平均置信度
  const calculateAvgConfidence = (details?: OCRDetail[]): number | null => {
    if (!details || details.length === 0) return null
    const confidences = details
      .map(d => d.confidence)
      .filter((c): c is number => c !== undefined && c !== null)
    if (confidences.length === 0) return null
    const sum = confidences.reduce((a, b) => a + b, 0)
    return Math.round((sum / confidences.length) * 100) / 100
  }

  return (
    <div className="ocr-container">
      {/* 标题和服务状态合并在一行 */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <Title level={2} style={{ margin: 0, marginBottom: 4 }}>
            OCR 文字识别
          </Title>
          <Text type="secondary">基于 PaddleOCR 的图片文字识别服务</Text>
        </div>
        <Space>
          <Space>
            {serviceStatus.loading ? (
              <Spin size="small" />
            ) : serviceStatus.available ? (
              <CheckCircleOutlined style={{ color: '#52c41a', fontSize: 16 }} />
            ) : (
              <CloseCircleOutlined style={{ color: '#ff4d4f', fontSize: 16 }} />
            )}
            <Text type="secondary" style={{ fontSize: 14 }}>
              服务状态: {serviceStatus.available ? '可用' : '不可用'}
            </Text>
          </Space>
          <Button size="small" onClick={checkServiceStatus} loading={serviceStatus.loading}>
            刷新
          </Button>
          <Tag color={serviceStatus.available ? 'success' : 'error'}>
            {serviceStatus.available ? '正常' : '异常'}
          </Tag>
        </Space>
      </div>

      {/* 错误提示 */}
      {error && (
        <Alert
          message="错误"
          description={error}
          type="error"
          showIcon
          closable
          onClose={() => setError(null)}
          style={{ marginBottom: 24 }}
        />
      )}

      <Row gutter={24}>
        {/* 左侧：输入区域 */}
        <Col xs={24} lg={12}>
          <Card title="输入" extra={<Text type="secondary">选择图片</Text>}>
            <Space direction="vertical" style={{ width: '100%' }} size="large">
              <div>
                <Text strong>上传图片 *</Text>
                <Space direction="vertical" style={{ width: '100%' }} size="small">
                  <Upload
                    beforeUpload={handleUpload}
                    onRemove={handleRemoveFile}
                    fileList={uploadedFile ? [uploadedFile] : []}
                    maxCount={1}
                    accept="image/*"
                    disabled={loading || uploading}
                  >
                    <Button 
                      icon={<UploadOutlined />} 
                      loading={uploading}
                      disabled={loading || uploading}
                    >
                      上传图片
                    </Button>
                  </Upload>
                  {!uploadedFile && (
                    <div>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        或输入本地文件路径（file:// 格式）
                      </Text>
                      <input
                        type="text"
                        placeholder="file:///path/to/image.jpg"
                        value={imageUrl}
                        onChange={(e) => setImageUrl(e.target.value)}
                        disabled={loading}
                        style={{
                          width: '100%',
                          padding: '4px 11px',
                          border: '1px solid #d9d9d9',
                          borderRadius: '4px',
                          marginTop: 8,
                        }}
                      />
                    </div>
                  )}
                  {uploadedFile && (
                    <div style={{ padding: '8px 0' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          已上传: {uploadedFile.name}
                        </Text>
                        <Button
                          type="link"
                          size="small"
                          icon={<DeleteOutlined />}
                          onClick={handleRemoveFile}
                          disabled={loading}
                        >
                          删除
                        </Button>
                      </div>
                    </div>
                  )}
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    支持 jpg, png, gif, webp, bmp 格式，最大 10MB
                  </Text>
                </Space>
              </div>

              <Space>
                <Button
                  type="primary"
                  icon={<FileTextOutlined />}
                  onClick={handleRecognize}
                  loading={loading}
                  disabled={!imageUrl && !uploadedFile}
                >
                  开始识别
                </Button>
                <Button icon={<ClearOutlined />} onClick={handleClear} disabled={loading}>
                  清空
                </Button>
              </Space>
            </Space>
          </Card>
        </Col>

        {/* 右侧：输出区域 */}
        <Col xs={24} lg={12}>
          <Card
            title="识别结果"
            extra={
              ocrResult && (
                <Space>
                  {ocrResult.details && ocrResult.details.length > 0 && (
                    <Tag color="blue">
                      {ocrResult.details.length} 行
                    </Tag>
                  )}
                  {calculateAvgConfidence(ocrResult.details) !== null && (
                    <Tag color="success">
                      平均置信度: {(calculateAvgConfidence(ocrResult.details)! * 100).toFixed(1)}%
                    </Tag>
                  )}
                  {ocrResult.text && (
                    <Tag color="default">
                      {ocrResult.text.length} 字符
                    </Tag>
                  )}
                </Space>
              )
            }
          >
            {loading ? (
              <div style={{ textAlign: 'center', padding: '40px 0' }}>
                <Spin size="large" />
                <div style={{ marginTop: 16 }}>
                  <Text type="secondary">正在识别图片中的文字...</Text>
                </div>
              </div>
            ) : ocrResult ? (
              <div>
                {/* 完整文本结果 */}
                {ocrResult.text && (
                  <div style={{ marginBottom: 16 }}>
                    <Text strong style={{ fontSize: 14, marginBottom: 8, display: 'block' }}>
                      识别文本：
                    </Text>
                    <div className="result-content">
                      <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word', margin: 0 }}>
                        {ocrResult.text}
                      </pre>
                    </div>
                  </div>
                )}

                {/* 详细信息 */}
                {ocrResult.details && ocrResult.details.length > 0 && (
                  <div>
                    <Divider orientation="left" style={{ margin: '16px 0' }}>
                      <Text strong style={{ fontSize: 14 }}>识别详情</Text>
                    </Divider>
                    <div style={{ maxHeight: 400, overflowY: 'auto' }}>
                      {ocrResult.details.map((detail, index) => (
                        <div
                          key={index}
                          style={{
                            padding: '8px 12px',
                            marginBottom: 8,
                            background: '#f5f5f5',
                            borderRadius: 4,
                            border: '1px solid #e8e8e8',
                          }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                            <Text style={{ fontSize: 13 }}>{detail.text}</Text>
                            {detail.confidence !== undefined && (
                              <Tag color={detail.confidence > 0.9 ? 'success' : detail.confidence > 0.7 ? 'warning' : 'error'}>
                                {(detail.confidence * 100).toFixed(1)}%
                              </Tag>
                            )}
                          </div>
                          {detail.box && detail.box.length > 0 && (
                            <div style={{ marginTop: 4 }}>
                              <Text type="secondary" style={{ fontSize: 11 }}>
                                位置: [
                                {detail.box.map((point, i) => (
                                  <span key={i}>
                                    [{point[0]}, {point[1]}]
                                    {i < detail.box!.length - 1 ? ', ' : ''}
                                  </span>
                                ))}]
                              </Text>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* 无识别结果 */}
                {(!ocrResult.text || ocrResult.text.trim() === '') && (!ocrResult.details || ocrResult.details.length === 0) && (
                  <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
                    <Text type="secondary">未识别到文字</Text>
                  </div>
                )}
              </div>
            ) : (
              <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
                <Text type="secondary">识别结果将显示在这里</Text>
              </div>
            )}
          </Card>
        </Col>
      </Row>

      {/* 使用说明 */}
      <Card size="small" style={{ marginTop: 24 }}>
        <Title level={5}>使用说明</Title>
        <ul style={{ margin: 0, paddingLeft: 20 }}>
          <li>
            <Text>确保 OCR 服务已启动（http://localhost:8082）</Text>
          </li>
          <li>
            <Text>支持上传本地图片文件或输入本地文件路径（file:// 格式）</Text>
          </li>
          <li>
            <Text>支持 jpg, png, gif, webp, bmp 格式，最大 10MB</Text>
          </li>
          <li>
            <Text>识别结果包含完整文本和每行的详细信息（包括置信度和位置坐标）</Text>
          </li>
        </ul>
      </Card>
    </div>
  )
}

export default OCR

