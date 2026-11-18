import { adminService } from '@/services/adminService'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/Card'
import { CardDescription } from '@/components/ui/Card'
import { CardFooter } from '@/components/ui/Card'
import { CardHeader } from '@/components/ui/Card'
import { CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Alert } from '@/components/ui/Alert'
import { Skeleton } from '@/components/ui/Skeleton'
import { useEffect, useState } from 'react'
import type { UserResponse } from '@/types/user'

export function UserListPage() {
  const [users, setUsers] = useState<UserResponse[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    adminService
      .getAllUsers()
      .then((data) => {
        setUsers(data)
        setLoading(false)
      })
      .catch(() => {
        setError('Kullanıcılar yüklenemedi.')
        setLoading(false)
      })
  }, [])

  if (loading) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-bold text-text-primary">Kullanıcı Listesi</h1>
        <div className="space-y-4">
          {[0, 1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      </div>
    )
  }

  if (error) {
    return <Alert variant="error">{error}</Alert>
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Kullanıcı Listesi</h1>
          <p className="text-text-secondary mt-1">
            Yalnızca ADMIN rolündeki kullanıcılar görebilir.
          </p>
        </div>
        <Badge variant="outline" className="text-xs">
          {users?.length || 0} kullanıcı
        </Badge>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Tüm Kullanıcılar</CardTitle>
          <CardDescription>
            Backend'den GET /api/v1/admin/users endpointi ile çekilen veriler.
            Şifre hash'i gibi hassas alanlar yanıtta yer almaz.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border">
                  <th className="pb-3 text-xs font-medium text-text-secondary">
                    Ad Soyad
                  </th>
                  <th className="pb-3 text-xs font-medium text-text-secondary">
                    Kullanıcı Adı
                  </th>
                  <th className="pb-3 text-xs font-medium text-text-secondary">
                    E-posta
                  </th>
                  <th className="pb-3 text-xs font-medium text-text-secondary">
                    Rol
                  </th>
                  <th className="pb-3 text-xs font-medium text-text-secondary">
                    Durum
                  </th>
                  <th className="pb-3 text-xs font-medium text-text-secondary">
                    Kayıt Tarihi
                  </th>
                </tr>
              </thead>
              <tbody>
                {(users || []).map((user) => (
                  <tr key={user.id} className="border-b border-border hover:bg-muted/50">
                    <td className="py-4 text-text-primary">
                      {user.firstName} {user.lastName}
                    </td>
                    <td className="py-4 text-text-primary">{user.username}</td>
                    <td className="py-4 text-text-primary">{user.email}</td>
                    <td className="py-4">
                      <Badge variant="outline">{user.role}</Badge>
                    </td>
                    <td className="py-4">
                      <span className={`flex items-center gap-1.5`}>
                        <span
                          className={`h-2.5 w-2.5 rounded-full ${
                            user.enabled ? 'bg-success' : 'bg-error'
                          }`}
                        />
                        {user.enabled ? 'Etkin' : 'Devre Dışı'}
                      </span>
                    </td>
                    <td className="py-4 text-text-primary">
                      {new Date(user.createdAt).toLocaleDateString('tr-TR')}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardContent>
        <CardFooter>
          <p className="text-xs text-text-secondary">
            Kullanıcı silme, rol değiştirme ve hesap kilidi kaldırma gibi
            işlemler için backend endpointi bulunmamaktadır.
          </p>
        </CardFooter>
      </Card>
    </div>
  )
}
