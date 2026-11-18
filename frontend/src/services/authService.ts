import { apiClient } from '@/services/api/apiClient'
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  RefreshRequest,
  LogoutRequest,
} from '@/types/auth'
import type { UserResponse } from '@/types/user'

const AUTH_BASE = '/api/v1/auth'

export const authService = {
  register: (data: RegisterRequest): Promise<UserResponse> =>
    apiClient.post(`${AUTH_BASE}/register`, data).then((r) => r.data),

  login: (data: LoginRequest): Promise<AuthResponse> =>
    apiClient.post(`${AUTH_BASE}/login`, data).then((r) => r.data),

  refresh: (data: RefreshRequest): Promise<AuthResponse> =>
    apiClient.post(`${AUTH_BASE}/refresh`, data).then((r) => r.data),

  logout: (data: LogoutRequest): Promise<void> =>
    apiClient.post(`${AUTH_BASE}/logout`, data).then(() => undefined),

  me: (): Promise<AuthResponse['user']> =>
    apiClient.get(`${AUTH_BASE}/me`).then((r) => r.data),
}

export default authService
