import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { getAccessToken, getRefreshToken, clearTokens, setTokens } from '@/lib/tokenStorage'
import authService from '@/services/authService'

const API_BASE_URL = (import.meta as any).env.VITE_API_BASE_URL || 'http://localhost:8080'

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
})

// Single-flight refresh state
let isRefreshing = false
let refreshPromise: Promise<string | null> | null = null

// Request interceptor: attach Bearer token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error),
)

// Response interceptor: handle 401 with single-flight refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      // Don't retry refresh or login endpoints
      if (
        originalRequest.url?.includes('/auth/login') ||
        originalRequest.url?.includes('/auth/register') ||
        originalRequest.url?.includes('/auth/refresh')
      ) {
        return Promise.reject(error)
      }

      originalRequest._retry = true

      // Initialize single-flight refresh
      if (!isRefreshing) {
        isRefreshing = true
        const refreshToken = getRefreshToken()
        refreshPromise = refreshToken
          ? authService.refresh({ refreshToken }).then((response) => {
              setTokens(response.accessToken, response.refreshToken)
              return response.accessToken
            }).catch(() => {
              clearTokens()
              return null
            })
          : Promise.resolve(null)
        refreshPromise.finally(() => {
          isRefreshing = false
          refreshPromise = null
        })
      }

      try {
        const newAccessToken = await refreshPromise
        if (newAccessToken) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return apiClient(originalRequest)
        }
      } catch {
        // Refresh failed, fall through to reject
      }

      clearTokens()
      if (typeof window !== 'undefined') {
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }

    return Promise.reject(error)
  },
)

export default apiClient
