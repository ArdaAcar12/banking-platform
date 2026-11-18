export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface ApiResponse<T> {
  timestamp: string
  success: boolean
  data?: T
  message?: string
}
