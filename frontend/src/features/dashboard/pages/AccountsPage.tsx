import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { accountService } from '@/services/accountService'
import type { AccountRequest } from '@/types/account'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/Card'
import { CardDescription } from '@/components/ui/Card'
import { CardHeader } from '@/components/ui/Card'
import { CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Alert } from '@/components/ui/Alert'
import { EmptyState } from '@/components/dashboard/EmptyState'
import { LoadingState } from '@/components/dashboard/LoadingState'
import { CreditCard, Plus, ArrowLeft } from 'lucide-react'

export function AccountsPage() {
  const { user, isLoading: authLoading } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [openForm, setOpenForm] = useState(false)
  const [formData, setFormData] = useState<AccountRequest>({
    accountType: 'CURRENT',
    currency: 'TRY',
  })

  const {
    data,
    isLoading: accountsLoading,
    error: accountsError,
  } = useQuery({
    queryKey: ['accounts'],
    queryFn: accountService.listAccounts,
    enabled: !!user,
    retry: 1,
  })

  const openAccountMutation = useMutation({
    mutationFn: accountService.openAccount,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['accounts'] })
      setOpenForm(false)
      setFormData({ accountType: 'CURRENT', currency: 'TRY' })
    },
  })

  const isLoading = authLoading || accountsLoading
  const accounts = data?.data ?? []

  if (isLoading) {
    return <LoadingState />
  }

  if (accountsError) {
    return (
      <div className="max-w-2xl mx-auto space-y-8">
        <h1 className="text-2xl font-bold text-text-primary">Hesaplarım</h1>
        <Alert variant="error">
          Hesaplar yüklenemedi. Lütfen tekrar deneyin.
        </Alert>
        <Button
          onClick={() => queryClient.invalidateQueries({ queryKey: ['accounts'] })}
          variant="accent"
        >
          Yeniden Yükle
        </Button>
        <Button
          onClick={() => navigate('/dashboard')}
          variant="outline"
        >
          Geri Dön
        </Button>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Hesaplarım</h1>
          <p className="text-text-secondary mt-1">
            Banka hesaplarınızı görüntüleyebilir ve yeni hesap açabilirsiniz.
          </p>
        </div>
        <div className="flex items-end gap-3">
          <Button
            onClick={() => setOpenForm(true)}
            variant="accent"
          >
            <Plus className="h-4 w-4 mr-2" /> Yeni Hesap Aç
          </Button>
          <Button
            onClick={() => navigate('/dashboard')}
            variant="outline"
          >
            <ArrowLeft className="h-4 w-4 mr-2" /> Geri Dön
          </Button>
        </div>
      </div>

      {accounts.length === 0 ? (
        <EmptyState
          title="Henüz hesapınız yok"
          subtitle="Yeni bir hesap açmak için 'Yeni Hesap Aç' butonuna tıklayabilirsiniz."
          icon={CreditCard}
          action={
            <Button variant="accent" onClick={() => setOpenForm(true)}>
              Yeni Hesap Aç
            </Button>
          }
        />
      ) : (
        <Card>
          <CardHeader>
            <CardTitle>Hesaplarınız</CardTitle>
            <CardDescription>
              Toplam {accounts.length} hesap bulunmakta.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse divide-y divide-border">
                <thead>
                  <tr className="border-b border-border">
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
                      Hesap Türü
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
                      Hesap No
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
                      IBAN
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider text-right">
                      Bakiye
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider text-right">
                      Kullanılabilir
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
                      Durum
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {accounts.map((acc) => (
                    <tr
                      key={acc.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="px-6 py-4">
                        <span className="text-sm font-medium text-text-primary">
                          {acc.accountType === 'CURRENT' ? 'Vadesiz' : 'Birikim'}
                        </span>
                        <p className="text-xs text-text-secondary">
                          {acc.currency}
                        </p>
                      </td>
                      <td className="px-6 py-4 text-sm text-text-primary">
                        {acc.accountNumber}
                      </td>
                      <td className="px-6 py-4 text-sm text-text-primary">
                        {acc.iban}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <span className="text-sm font-medium text-text-primary">
                          {new Intl.NumberFormat('tr-TR', {
                            style: 'currency',
                            currency: acc.currency,
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2,
                          }).format(acc.balance)}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <span className="text-sm font-medium text-text-primary">
                          {new Intl.NumberFormat('tr-TR', {
                            style: 'currency',
                            currency: acc.currency,
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2,
                          }).format(acc.availableBalance)}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <Badge
                          variant={
                            acc.status === 'ACTIVE'
                              ? 'success'
                              : acc.status === 'FROZEN'
                                ? 'warning'
                                : 'error'
                          }
                        >
                          {acc.status === 'ACTIVE'
                            ? 'Aktif'
                            : acc.status === 'FROZEN'
                              ? 'Dondurulmuş'
                              : 'Kapalı'}
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Open Account Form */}
      {openForm && (
        <Card className="max-w-md mx-auto">
          <CardHeader>
            <CardTitle>Yeni Hesap Aç</CardTitle>
            <CardDescription>
              Yeni bir banka hesabı oluşturun. Bakiyeniz sıfırdan başlayacaktır.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form
              onSubmit={(e) => {
                e.preventDefault()
                openAccountMutation.mutate(formData)
              }}
              className="space-y-4"
            >
              <div className="space-y-3">
                <label className="block text-xs font-medium text-text-secondary mb-1">
                  Hesap Türü
                </label>
                <select
                  value={formData.accountType}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      accountType: e.target.value as 'CURRENT' | 'SAVINGS',
                    })
                  }
                  className="w-full h-10 rounded-md border border-border bg-surface px-4 py-2 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-accent focus-visible:border-accent"
                >
                  <option value="CURRENT">Vadesiz Hesap</option>
                  <option value="SAVINGS">Birikim Hesabı</option>
                </select>
              </div>
              <div className="space-y-3">
                <label className="block text-xs font-medium text-text-secondary mb-1">
                  Para Birimi
                </label>
                <select
                  value={formData.currency}
                  onChange={(e) =>
                    setFormData({ ...formData, currency: e.target.value })
                  }
                  className="w-full h-10 rounded-md border border-border bg-surface px-4 py-2 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-accent focus-visible:border-accent"
                >
                  <option value="TRY">Türk Lirası (TRY)</option>
                  <option value="USD">Amerikan Doları (USD)</option>
                  <option value="EUR">Euro (EUR)</option>
                </select>
              </div>
              <Alert variant="info">
                Hesap numarası ve IBAN otomatik olarak oluşturulacaktır.
                Başlangıç bakiyeniz 0,00 olarak ayarlanacaktır.
              </Alert>
              <div className="flex items-end gap-3 pt-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setOpenForm(false)}
                  disabled={openAccountMutation.isPending}
                >
                  İptal
                </Button>
                <Button
                  type="submit"
                  variant="accent"
                  disabled={openAccountMutation.isPending}
                >
                  {openAccountMutation.isPending ? 'Oluşturuluyor...' : 'Hesap Oluştur'}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
