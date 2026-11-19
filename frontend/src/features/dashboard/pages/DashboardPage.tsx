import { useQuery } from '@tanstack/react-query'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { authService } from '@/services/authService'
import { accountService } from '@/services/accountService'
import { transactionService } from '@/services/transactionService'
import { useNavigate } from 'react-router-dom'

import { BalanceCard } from '@/components/dashboard/BalanceCard'
import { AccountCard } from '@/components/dashboard/AccountCard'
import { QuickActionCard } from '@/components/dashboard/QuickActionCard'
import { TransactionTable } from '@/components/dashboard/TransactionTable'
import { DashboardSection } from '@/components/dashboard/DashboardSection'
import { EmptyState } from '@/components/dashboard/EmptyState'
import { LoadingState } from '@/components/dashboard/LoadingState'

import { demoUser } from '@/data/dashboard'

import { CreditCard, Send, Clock, BarChart3 } from 'lucide-react'
import { Button } from '@/components/ui/Button'

export function DashboardPage() {
  const { user: authUser, isLoading: authLoading, logout } = useAuth()
  const navigate = useNavigate()

  const { data: meData, isLoading: meLoading } = useQuery({
    queryKey: ['me'],
    queryFn: authService.me,
    enabled: !!authUser,
    retry: 1,
  })

  const {
    data: accountsData,
    isLoading: accountsLoading,
    error: accountsError,
  } = useQuery({
    queryKey: ['accounts'],
    queryFn: accountService.listAccounts,
    enabled: !!authUser,
    retry: 1,
  })

  const {
    data: transactionsData,
    isLoading: transactionsLoading,
  } = useQuery({
    queryKey: ['transactions'],
    queryFn: () =>
      transactionService.listTransactions({
        page: 0,
        size: 6,
        sort: 'createdAt,desc',
      }),
    enabled: !!authUser,
    retry: 1,
  })

  const user = meData ?? (authUser as any ?? demoUser)
  const isLoading = authLoading || meLoading || accountsLoading || transactionsLoading

  if (isLoading) {
    return <LoadingState />
  }

  const accounts = accountsData?.data ?? []
  const totalBalance = accounts.reduce(
    (sum, acc) => sum + acc.balance,
    0
  )
  const availableBalance = accounts.reduce(
    (sum, acc) => sum + acc.availableBalance,
    0
  )
  const recentTransactions = transactionsData?.data ?? []

  return (
    <div className="space-y-8">
      <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
        <div className="flex-1">
          <h1 className="text-2xl font-bold text-text-primary">
            Merhaba, {user.firstName}
          </h1>
          <p className="text-text-secondary mt-1">
            Finansal durumuna genel bir bakış.
          </p>
        </div>
        <div className="flex items-end gap-3">
          <button
            onClick={() => navigate('/accounts')}
            className="px-4 py-2.5 text-sm font-medium text-accent-foreground bg-accent rounded-lg hover:bg-accent-hover transition-colors"
          >
            Hesaplar
          </button>
          <button
            onClick={logout}
            className="px-4 py-2.5 text-sm font-medium text-error-foreground bg-error/10 rounded-lg hover:bg-error/20 transition-colors"
          >
            Çıkış Yap
          </button>
        </div>
      </div>

      {accountsError && (
        <div className="rounded-lg border border-error/20 bg-error/5 p-4 flex items-center gap-3">
          <span className="h-5 w-5 rounded-full bg-error/20 text-error flex items-center justify-center">
            <span className="text-[10px] font-bold">!</span>
          </span>
          <div className="flex-1">
            <p className="text-sm text-error-foreground font-medium">
              Hesaplar yüklenemedi
            </p>
            <p className="text-xs text-error-foreground/70 mt-0.5">
              Lütfen sayfayı yenileyin veya tekrar giriş yapın.
            </p>
          </div>
        </div>
      )}

      <DashboardSection title="Toplam Varlık" subtitle="Hesaplarınızdaki toplam bakiye özeti">
        <BalanceCard
          totalBalance={totalBalance}
          availableBalance={availableBalance}
          currency="TRY"
        />
      </DashboardSection>

      <DashboardSection title="Hesaplarım" subtitle="Hesap detaylarınızı görüntüleyin">
        {accounts.length === 0 ? (
          <EmptyState
            title="Henüz hesapınız yok"
            subtitle="Yeni bir hesap açmak için Hesaplar sayfasına gidin."
            icon={CreditCard}
            action={
              <Button
                onClick={() => navigate('/accounts')}
                variant="accent"
              >
                Yeni Hesap Aç
              </Button>
            }
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {accounts.map((acc) => (
              <AccountCard
                key={acc.id}
                name={
                  acc.accountType === 'CURRENT'
                    ? 'Vadesiz Hesap'
                    : 'Birikim Hesabı'
                }
                type={acc.accountType}
                maskedIban={acc.iban}
                lastFour={acc.accountNumber.slice(-4)}
                balance={acc.balance}
                currency={acc.currency}
              />
            ))}
          </div>
        )}
      </DashboardSection>

      <DashboardSection title="Hızlı İşlemler" subtitle="Çok kullanılan bankacılık işlemlerine erişim">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
          <QuickActionCard
            title="Para Gönder"
            icon={Send}
            onClick={() => {}}
            disabled
          />
          <QuickActionCard
            title="Hesaplarım"
            icon={CreditCard}
            onClick={() => navigate('/accounts')}
          />
          <QuickActionCard
            title="Kartlarım"
            icon={CreditCard}
            onClick={() => {}}
            disabled
          />
          <QuickActionCard
            title="İşlem Geçmişi"
            icon={Clock}
            onClick={() => navigate('/transactions')}
          />
          <QuickActionCard
            title="Ödeme Yap"
            icon={Send}
            onClick={() => {}}
            disabled
          />
        </div>
      </DashboardSection>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <DashboardSection title="Bakiye Değişikliği" subtitle="Demo veriler gösterilmektedir.">
          <div className="h-64 flex items-center justify-center bg-muted/50 rounded-lg">
            <EmptyState
              title="Grafik yakında"
              subtitle="Grafik verileri gerçek işlemlerle birlikte gösterilecektir."
              icon={BarChart3}
            />
          </div>
        </DashboardSection>
        <DashboardSection title="Kategori Dağılımı" subtitle="Demo veriler gösterilmektedir.">
          <div className="h-64 flex items-center justify-center bg-muted/50 rounded-lg">
            <EmptyState
              title="Grafik yakında"
              subtitle="Kategori dağılımı grafiği gerçek işlemlerle birlikte gösterilecektir."
              icon={BarChart3}
            />
          </div>
        </DashboardSection>
      </div>

      <DashboardSection title="Son İşlemler" subtitle="Gerçek işlem geçmişiniz">
        <div className="overflow-x-auto">
          {recentTransactions.length === 0 ? (
            <EmptyState
              title="Henüz işlem bulunamadı"
              subtitle="İşlem geçmişiniz burada görünecektir. İlk işleminizi yaparak başlayabilirsiniz."
              icon={Clock}
            />
          ) : (
            <TransactionTable transactions={recentTransactions} />
          )}
        </div>
        <div className="mt-4 flex justify-end">
          <button
            className="text-sm text-accent font-medium hover:underline"
            onClick={() => navigate('/transactions')}
          >
            Tüm işlemleri göster →
          </button>
        </div>
      </DashboardSection>
    </div>
  )
}
