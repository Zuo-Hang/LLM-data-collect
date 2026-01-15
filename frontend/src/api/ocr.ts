// OCR 服务 API 客户端
// @ts-ignore - Vite 环境变量
const OCR_API_BASE_URL = import.meta.env.VITE_OCR_API_BASE_URL || '/api/ocr'

export interface OCRRecognizeResponse {
  success: boolean
  text?: string
  details?: OCRDetail[]
  error?: string
}

export interface OCRDetail {
  text: string
  confidence?: number
  box?: number[][]
}

export interface OCRHealthResponse {
  status: string
  service: string
  ocr_available: boolean
  message?: string
}

/**
 * 健康检查
 */
export const checkOCRHealth = async (): Promise<OCRHealthResponse> => {
  const response = await fetch(`${OCR_API_BASE_URL}/health`)
  if (!response.ok) {
    throw new Error(`OCR 服务不可用: ${response.statusText}`)
  }
  return response.json()
}

/**
 * 通过文件路径识别图片中的文字
 */
export const recognizeImagePath = async (
  imagePath: string,
  returnBoxes: boolean = true,  // 默认返回位置信息
  returnConfidence: boolean = true
): Promise<OCRRecognizeResponse> => {
  const params = new URLSearchParams({
    image_path: imagePath,
    return_boxes: String(returnBoxes),
    return_confidence: String(returnConfidence),
  })
  
  const response = await fetch(`${OCR_API_BASE_URL}/recognize-path?${params.toString()}`, {
    method: 'POST',
  })
  
  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: response.statusText }))
    throw new Error(error.error || `OCR 识别失败: ${response.statusText}`)
  }
  
  return response.json()
}

/**
 * 通过文件上传识别图片中的文字
 */
export const recognizeImageFile = async (
  file: File,
  returnBoxes: boolean = true,  // 默认返回位置信息
  returnConfidence: boolean = true
): Promise<OCRRecognizeResponse> => {
  const formData = new FormData()
  formData.append('file', file)
  
  const params = new URLSearchParams({
    return_boxes: String(returnBoxes),
    return_confidence: String(returnConfidence),
  })
  
  const response = await fetch(`${OCR_API_BASE_URL}/recognize?${params.toString()}`, {
    method: 'POST',
    body: formData,
  })
  
  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: response.statusText }))
    throw new Error(error.error || `OCR 识别失败: ${response.statusText}`)
  }
  
  return response.json()
}

