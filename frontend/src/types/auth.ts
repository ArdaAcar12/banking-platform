import type { UserResponse } from './user'

export type Role = 'CUSTOMER' | 'ADMIN' | 'SUPPORT'

export interface LoginRequest {
  identifier: string
  password: string
}

export interface RegisterRequest {
  email: string
  username: string
  password: string
  firstName: string
  lastName: string
}

export interface RefreshRequest {
  refreshToken: string
}

export interface LogoutRequest {
  refreshToken: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  expiresAt: string
  user: UserResponse
}

export interface TokenPair {
  accessToken: string
  refreshToken: string
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}

export interface AuthState {
  user: UserResponse | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
}

export interface LoginCredentials {
  identifier: string
  password: string
}

export interface RegisterData {
  email: string
  username: string
  password: string
  firstName: string
  lastName: string
}
