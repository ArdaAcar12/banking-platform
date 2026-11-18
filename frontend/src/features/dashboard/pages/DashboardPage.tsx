import { useQuery } from '@tanstack/react-query'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { authService } from '@/services/authService'
import { useNavigate } from 'react-router-dom'

import { BalanceCard } from '@/components/dashboard/BalanceCard'
import { AccountCard } from '@/components/dashboard/AccountCard'
import { QuickActionCard } from '@/components/dashboard/QuickActionCard'
import { FinancialChart } from '@/components/dashboard/FinancialChart'
import { TransactionTable } from '@/components/dashboard/TransactionTable'
import { DashboardSection } from '@/components/dashboard/DashboardSection'
import { EmptyState } from '@/components/dashboard/EmptyState'
import { LoadingState } from '@/components/dashboard/LoadingState'

import { demoUser } from '@/data/dashboard'
import { demoAccounts, demoTotalBalance, demoAvailableBalance } from '@/data/accounts'
import { demoTransactions, demoMonthlyBalanceHistory } from '@/data/transactions'

import { CreditCard, Send, Clock, RefreshCw, BarChart3 } from 'lucide-react'

export function DashboardPage() {
  const { user: authUser, isLoading: authLoading, logout } = useAuth()
  const navigate = useNavigate()

  // Fetch real user data from /api/v1/auth/me
  const { data: meData, isLoading: meLoading, error: meError } = useQuery({
    queryKey: ['me'],
    queryFn: authService.me,
    enabled: !!authUser,
    retry: 1,
  })

  const user = meData ?? (authUser as any ?? demoUser)
  const isLoading = authLoading || meLoading
  const showDemo = !authUser || meError || isLoading

  if (isLoading) {
    return <LoadingState />
  }

  return (
    <div className="space-y-8">
      {/* Welcome Section */}
      <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
        <div className="flex-1">
          <h1 className="text-2xl font-bold text-text-primary">
            Merhaba, {user.firstName}
          </h1>
          <p className="text-text-secondary mt-1">
            Finansal durumuna genel bir bakis. {showDemo ? '(Demo modu)' : ''}
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
            Cikis Yap
          </button>
        </div>
      </div>

      {/* Demo notice (only when using demo data) */}
      {showDemo && (
        <div className="rounded-lg border border-warning/20 bg-warning/5 p-4 flex items-center gap-3">
          <span className="h-5 w-5 rounded-full bg-warning/20 text-warning flex items-center justify-center">
            <span className="text-[10px] font-bold">!</span>
          </span>
          <div className="flex-1">
            <p className="text-sm text-warning-foreground font-medium">
              Demo modu etkin
            </p>
            <p className="text-xs text-warning-foreground/70 mt-0.5">
              Gercek hesap ve bakiye API'leri eklenmedigi icin demo veriler goruntulenmektedir.
            </p>
          </div>
        </div>
      )}

      {/* Total Balance Card */}
      <DashboardSection title="Toplam Varlik" subtitle="Hesaplarinizdaki toplam bakiye ozeti">
        <BalanceCard
          totalBalance={showDemo ? demoTotalBalance : 0}
          availableBalance={showDemo ? demoAvailableBalance : 0}
          currency="TRY"
        />
      </DashboardSection>

      {/* Account Cards */}
      <DashboardSection title="Hesaplarim" subtitle="Hesap detaylarini goruntuleyin">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {showDemo ? (
            demoAccounts.map((acc) => (
              <AccountCard
                key={acc.id}
                name={acc.name}
                type={acc.type}
                maskedIban={acc.maskedIban}
                lastFour={acc.lastFour}
                balance={acc.balance}
                currency={acc.currency}
                demo
              />
            ))
          ) : (
            <EmptyState
              title="Hesap verisi yok"
              subtitle="Hesap API'si eklenmedigi icin veri goruntulenemiyor."
              icon={CreditCard}
            />
          )}
        </div>
      </DashboardSection>

      {/* Quick Actions */}
      <DashboardSection title="Hizli Islemler" subtitle="Cok kullanilan bankacilik islemlerine erisim">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
          <QuickActionCard
            title="Para Gonder"
            href="/transfers"
            icon={Send}
            disabled
            demo
          />
          <QuickActionCard
            title="Hesaplarim"
            href="/accounts"
            icon={CreditCard}
            disabled
            demo
          />
          <QuickActionCard
            title="Kartlarim"
            href="/cards"
            icon={CreditCard}
            disabled
            demo
          />
          <QuickActionCard
            title="Islem Geçmisi"
            href="/transactions"
            icon={Clock}
            disabled
            demo
          />
          <QuickActionCard
            title="Odeme Yap"
            href="/payments"
            icon={Send}
            disabled
            demo
          />
        </div>
      </DashboardSection>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <DashboardSection title="Bakiye Degisikligi" subtitle="Son 6 aylik bakiye trendi">
          <FinancialChart data={demoMonthlyBalanceHistory} />
        </DashboardSection>
        <DashboardSection title="Kategori Dagilimi" subtitle="Son aya ait harcama kategorileri">
          <div className="h-64 flex items-center justify-center bg-muted/50 rounded-lg">
            <EmptyState
              title="Grafik yakinda"
              subtitle="Kategori dagilimi grafiigi yakinda eklenecektir."
              icon={BarChart3}
            />
          </div>
        </DashboardSection>
      </div>

      {/* Recent Transactions */}
      <DashboardSection title="Son Islemler" subtitle="En son yapilan bankacilik hesap hareketleri">
        <div className="overflow-x-auto">
          {showDemo ? (
            <TransactionTable transactions={demoTransactions.slice(0, 6)} />
          ) : (
            <EmptyState
              title="Islem verisi yok"
              subtitle="Islem gecmisi API'si eklenmedigi icin veri goruntulenemiyor."
              icon={RefreshCw}
            />
          )}
        </div>
        <div className="mt-4 flex justify-end">
          <button className="text-sm text-accent font-medium hover:underline">
            Tum islemleri goster
          </button>
        </div>
      </DashboardSection>
    </div>
  )
}
