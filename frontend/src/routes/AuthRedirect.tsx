import { Navigate } from 'react-router-dom'
import { useAuth } from '@/features/auth/hooks/useAuth'

/**
 * Kök rotaya (/) geldiğinde kimlik durumuna göre yönlendirme yapar.
 * - Kimlik doğrulanmışsa → /dashboard
 * - Kimliği doğrulanmamışsa → /login
 * - Auth durumu hâlâ yükleniyorsa → boş (loading)
 */
export function AuthRedirect() {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return null
  }

  return isAuthenticated
    ? <Navigate to="/dashboard" replace />
    : <Navigate to="/login" replace />
}
