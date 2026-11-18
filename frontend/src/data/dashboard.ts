import type { Role } from '@/types/auth'

export interface DemoUser {
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

export const demoUser: DemoUser = {
  id: 'demo-user-001',
  email: 'kullanici@ornek.com',
  username: 'kullanici123',
  firstName: 'Arda',
  lastName: 'Çelik',
  fullName: 'Arda Çelik',
  role: 'CUSTOMER',
  enabled: true,
  accountNonLocked: true,
  createdAt: '2026-01-15T08:30:00Z',
}

export const demoAuth = {
  accessToken: 'demo-access-token',
  refreshToken: 'demo-refresh-token',
  tokenType: 'Bearer',
  expiresIn: 900,
  expiresAt: new Date(Date.now() + 900 * 1000).toISOString(),
  user: demoUser,
}
