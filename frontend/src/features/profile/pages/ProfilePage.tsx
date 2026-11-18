import { useAuth } from '@/features/auth/hooks/useAuth'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/Card'
import { CardDescription } from '@/components/ui/Card'
import { CardHeader } from '@/components/ui/Card'
import { CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Alert } from '@/components/ui/Alert'
import { Shield, Clock } from 'lucide-react'

export function ProfilePage() {
  const { user, isLoading, isAuthenticated } = useAuth()

  if (isLoading) {
    return null
  }

  if (!isAuthenticated || !user) {
    return <Alert variant="error">Kullanıcı bilgileri yüklenemedi.</Alert>
  }

  return (
    <div className="max-w-2xl space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-text-primary">Profilim</h1>
        <p className="text-text-secondary mt-1">
          Kişisel bilgilerinizi ve hesap durumunuzu görüntüleyebilirsiniz.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Kişisel Bilgiler</CardTitle>
          <CardDescription>
            Backend'den GET /api/v1/auth/me endpointi ile çekilen veriler.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-accent text-accent-foreground text-lg font-bold">
              {user.firstName.charAt(0)}{user.lastName.charAt(0)}
            </div>
            <div>
              <h2 className="font-bold text-text-primary">
                {user.firstName} {user.lastName}
              </h2>
              <p className="text-text-secondary">{user.username}</p>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-1">
              <p className="text-xs font-medium text-text-secondary">E-posta</p>
              <p className="text-text-primary">{user.email}</p>
            </div>
            <div className="space-y-1">
              <p className="text-xs font-medium text-text-secondary">Kullanıcı Adı</p>
              <p className="text-text-primary">{user.username}</p>
            </div>
            <div className="space-y-1">
              <p className="text-xs font-medium text-text-secondary">Rol</p>
              <Badge variant="outline">{user.role}</Badge>
            </div>
            <div className="space-y-1">
              <p className="text-xs font-medium text-text-secondary">Hesap Durumu</p>
              <span className={`flex items-center gap-1.5`}>
                <span
                  className={`h-2.5 w-2.5 rounded-full ${
                    user.enabled ? 'bg-success' : 'bg-error'
                  }`}
                />
                {user.enabled ? 'Etkin' : 'Devre Dışı'}
              </span>
            </div>
            <div className="space-y-1">
              <p className="text-xs font-medium text-text-secondary">Hesap Kilidi</p>
              <span className={`flex items-center gap-1.5`}>
                <span
                  className={`h-2.5 w-2.5 rounded-full ${
                    user.accountNonLocked ? 'bg-success' : 'bg-error'
                  }`}
                />
                {user.accountNonLocked ? 'Kilitli Değil' : 'Kilitli'}
              </span>
            </div>
            <div className="space-y-1">
              <p className="text-xs font-medium text-text-secondary">Kayıt Tarihi</p>
              <p className="text-text-primary">
                {new Date(user.createdAt).toLocaleDateString('tr-TR')}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Oturum Bilgileri</CardTitle>
          <CardDescription>Güvenlik ve oturum durumu bilgileri.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Shield className="h-5 w-5 text-text-secondary" />
                <span className="text-sm text-text-secondary">Kimlik Doğrulama</span>
              </div>
              <Badge variant={isAuthenticated ? 'success' : 'error'}>
                {isAuthenticated ? 'Aktif' : 'Süresi Dolmuş'}
              </Badge>
            </div>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Clock className="h-5 w-5 text-text-secondary" />
                <span className="text-sm text-text-secondary">Token Süresi</span>
              </div>
              <span className="text-sm text-text-primary">15 dakika (access token)</span>
            </div>
          </div>
        </CardContent>
        <CardDescription className="p-4 text-center">
          <p className="text-xs text-text-secondary">
            Şifre değiştirme ve sıfırlama özellikleri backend tarafından henüz desteklenmemektedir.
          </p>
        </CardDescription>
      </Card>
    </div>
  )
}
