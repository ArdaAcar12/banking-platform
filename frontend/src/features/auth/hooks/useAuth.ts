import { useState, useCallback, useEffect } from 'react'
import { authService } from '@/services/authService'
import {
  getAccessToken,
  getRefreshToken,
  clearTokens,
  setTokens,
  setTokenExpiry,
  getTokenExpiry,
} from '@/lib/tokenStorage'
import type { AuthState, LoginCredentials, RegisterData } from '@/types/auth'
import type { UserResponse } from '@/types/user'

export function useAuth() {
  const [state, setState] = useState<AuthState>({
    user: null,
    accessToken: null,
    refreshToken: null,
    isAuthenticated: false,
    isLoading: true,
  })

  // Initialize auth state from sessionStorage
  useEffect(() => {
    const accessToken = getAccessToken()
    const refreshToken = getRefreshToken()
    const expiry = getTokenExpiry()

    if (accessToken && refreshToken && expiry && expiry > Date.now()) {
      setState({
        user: null,
        accessToken,
        refreshToken,
        isAuthenticated: true,
        isLoading: true,
      })
      // Fetch user profile
      authService
        .me()
        .then((user) => {
          setState((prev) => ({ ...prev, user, isLoading: false }))
        })
        .catch(() => {
          clearTokens()
          setState({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        })
    } else {
      clearTokens()
      setState((prev) => ({ ...prev, isLoading: false }))
    }
  }, [])

  const login = useCallback(
    async (credentials: LoginCredentials) => {
      setState((prev) => ({ ...prev, isLoading: true }))
      try {
        const response = await authService.login({
          identifier: credentials.identifier,
          password: credentials.password,
        })

        const expiryTime = Date.now() + (response.expiresIn || 900) * 1000
        setTokens(response.accessToken, response.refreshToken)
        setTokenExpiry(expiryTime)

        setState({
          user: response.user,
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
          isAuthenticated: true,
          isLoading: false,
        })

        return response
      } catch (error) {
        setState((prev) => ({ ...prev, isLoading: false }))
        throw error
      }
    },
    [],
  )

  const register = useCallback(
    async (data: RegisterData) => {
      setState((prev) => ({ ...prev, isLoading: true }))
      try {
        const response = await authService.register({
          email: data.email,
          username: data.username,
          password: data.password,
          firstName: data.firstName,
          lastName: data.lastName,
        })

        // Backend register endpoint returns UserResponse (201), NOT AuthResponse.
        // No tokens are issued on registration; the user must log in separately.
        setState((prev) => ({ ...prev, isLoading: false }))

        return response
      } catch (error) {
        setState((prev) => ({ ...prev, isLoading: false }))
        throw error
      }
    },
    [],
  )

  const refresh = useCallback(async (): Promise<void> => {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }

    const response = await authService.refresh({ refreshToken })
    const expiryTime = Date.now() + (response.expiresIn || 900) * 1000
    setTokens(response.accessToken, response.refreshToken)
    setTokenExpiry(expiryTime)

    setState((prev) => ({
      ...prev,
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
    }))
  }, [])

  const logout = useCallback(async () => {
    const refreshToken = getRefreshToken()
    try {
      if (refreshToken) {
        await authService.logout({ refreshToken })
      }
    } catch {
      // Ignore logout errors - clear tokens anyway
    } finally {
      clearTokens()
      setState({
        user: null,
        accessToken: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,
      })
    }
  }, [])

  const updateUser = useCallback((user: UserResponse) => {
    setState((prev) => ({ ...prev, user }))
  }, [])

  return {
    ...state,
    login,
    register,
    refresh,
    logout,
    updateUser,
  }
}
