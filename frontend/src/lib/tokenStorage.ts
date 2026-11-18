export interface TokenStorage {
  accessToken: string | null
  refreshToken: string | null
}

const ACCESS_TOKEN_KEY = 'bp_access_token'
const REFRESH_TOKEN_KEY = 'bp_refresh_token'

// Use sessionStorage instead of localStorage for better security
// sessionStorage is cleared when the browser tab is closed
const storage = typeof window !== 'undefined' ? sessionStorage : null

export function getAccessToken(): string | null {
  if (!storage) return null
  return storage.getItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken(): string | null {
  if (!storage) return null
  return storage.getItem(REFRESH_TOKEN_KEY)
}

export function setTokens(accessToken: string, refreshToken: string): void {
  if (!storage) return
  storage.setItem(ACCESS_TOKEN_KEY, accessToken)
  storage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

export function clearTokens(): void {
  if (!storage) return
  storage.removeItem(ACCESS_TOKEN_KEY)
  storage.removeItem(REFRESH_TOKEN_KEY)
}

export function getTokenExpiry(): number | null {
  if (!storage) return null
  const expiry = storage.getItem('bp_token_expiry')
  return expiry ? parseInt(expiry, 10) : null
}

export function setTokenExpiry(timestamp: number): void {
  if (!storage) return
  storage.setItem('bp_token_expiry', String(timestamp))
}
