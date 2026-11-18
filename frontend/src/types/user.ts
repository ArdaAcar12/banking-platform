import type { Role } from './auth'

export interface UserResponse {
  id: string
  email: string
  username: string
  firstName: string
  lastName: string
  fullName: string
  role: Role
  enabled: boolean
  accountNonLocked: boolean
  createdAt: string
}

export interface CurrentUser {
  id: string
  email: string
  username: string
  firstName: string
  lastName: string
  fullName: string
  role: Role
  enabled: boolean
  accountNonLocked: boolean
  createdAt: string
}
