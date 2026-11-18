import { apiClient } from '@/services/api/apiClient'
import type { UserResponse } from '@/types/user'

const ADMIN_BASE = '/api/v1/admin'

export const adminService = {
  getAllUsers: (): Promise<UserResponse[]> =>
    apiClient.get(`${ADMIN_BASE}/users`).then((r) => r.data),

  getUserById: (id: string): Promise<UserResponse> =>
    apiClient.get(`${ADMIN_BASE}/users/${id}`).then((r) => r.data),
}

export default adminService
