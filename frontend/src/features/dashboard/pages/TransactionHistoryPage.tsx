import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { transactionService } from '@/services/transactionService'
import type { TransactionType, TransactionStatus, TransactionFilters } from '@/types/transaction'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/Card'
import { CardDescription } from '@/components/ui/Card'
import { CardFooter } from '@/components/ui/Card'
import { CardHeader } from '@/components/ui/Card'
import { CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Alert } from '@/components/ui/Alert'
import { EmptyState } from '@/components/dashboard/EmptyState'
import { LoadingState } from '@/components/dashboard/LoadingState'
import { CreditCard, RefreshCw, ChevronLeft, ChevronRight } from 'lucide-react'
import { cn } from '@/lib/utils'

export function TransactionHistoryPage() {
  const { user, isLoading: authLoading } = useAuth()
  const queryClient = useQueryClient()

  const [filters, setFilters] = useState<TransactionFilters>({})
  const [page, setPage] = useState(0)
  const [size] = useState(10)

  const {
    data,
    isLoading: transactionsLoading,
    error: transactionsError,
  } = useQuery({
    queryKey: ['transactions', user?.id, filters, page, size],
    queryFn: () =>
      transactionService.listTransactions({
        page,
        size,
        sort: 'createdAt,desc',
        type: filters.type,
        status: filters.status,
        startDate: filters.startDate,
        endDate: filters.endDate,
      }),
    enabled: !!user,
    retry: 1,
  })

  const isLoading = authLoading || transactionsLoading
  const transactions = data?.data ?? []
  const totalElements = data?.totalElements ?? 0
  const totalPages = data?.totalPages ?? 0
  const currentPage = data?.page ?? 0

  if (isLoading) {
    return <LoadingState />
  }

  if (transactionsError) {
    return (
      <div className="max-w-2xl mx-auto space-y-8">
        <h1 className="text-2xl font-bold text-text-primary">İşlem Geçmişi</h1>
        <Alert variant="error">
          İşlem geçmişi yüklenemedi. Lütfen tekrar deneyin.
        </Alert>
        <Button
          onClick={() => queryClient.invalidateQueries({ queryKey: ['transactions'] })}
          variant="accent"
        >
          Yeniden Yükle
        </Button>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">İşlem Geçmişi</h1>
          <p className="text-text-secondary mt-1">
            Tüm banka işlemlerinizi buradan görüntüleyebilirsiniz.
          </p>
        </div>
        <div className="flex items-end gap-3">
          <Button
            onClick={() => queryClient.invalidateQueries({ queryKey: ['transactions'] })}
            variant="outline"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Yeniden Yükle
          </Button>
        </div>
      </div>

      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap gap-4">
            <div className="flex items-center gap-2">
              <span className="text-xs font-medium text-text-secondary">
                Tür:
              </span>
              <select
                value={filters.type ?? ''}
                onChange={(e) =>
                  setFilters({
                    ...filters,
                    type: (e.target.value as TransactionType) || undefined,
                  })
                }
                className="w-full h-10 rounded-md border border-border bg-surface px-3 py-2 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-accent focus-visible:border-accent"
              >
                <option value="">Tümü</option>
                <option value="TRANSFER">Transfer</option>
                <option value="DEPOSIT">Yatırma</option>
                <option value="WITHDRAWAL">Çekme</option>
                <option value="FEE">Ücret</option>
              </select>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-medium text-text-secondary">
                Durum:
              </span>
              <select
                value={filters.status ?? ''}
                onChange={(e) =>
                  setFilters({
                    ...filters,
                    status: (e.target.value as TransactionStatus) || undefined,
                  })
                }
                className="w-full h-10 rounded-md border border-border bg-surface px-3 py-2 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-accent focus-visible:border-accent"
              >
                <option value="">Tümü</option>
                <option value="PENDING">Beklemede</option>
                <option value="COMPLETED">Tamamlandı</option>
                <option value="FAILED">Başarısız</option>
                <option value="REVERSED">Geri Alındı</option>
              </select>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-medium text-text-secondary">
                Tarih:
              </span>
              <input
                type="date"
                value={filters.startDate ?? ''}
                onChange={(e) =>
                  setFilters({ ...filters, startDate: e.target.value || undefined })
                }
                className="w-full h-10 rounded-md border border-border bg-surface px-3 py-2 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-accent focus-visible:border-accent"
              />
              <input
                type="date"
                value={filters.endDate ?? ''}
                onChange={(e) =>
                  setFilters({ ...filters, endDate: e.target.value || undefined })
                }
                className="w-full h-10 rounded-md border border-border bg-surface px-3 py-2 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-accent focus-visible:border-accent"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>İşlemler</CardTitle>
          <CardDescription>
            {totalElements} işlem bulunuyor.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {transactions.length === 0 ? (
            <EmptyState
              title="Henüz işlem bulunamadı"
              subtitle="Bu kriterlere uygun işlem kaydı bulunamadı."
              icon={CreditCard}
            />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse divide-y divide-border">
                <thead>
                  <tr className="border-b border-border">
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
                      Referans
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
                      Tür
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider text-right">
                      Tutar
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
                      Para Birimi
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
                      Durum
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
                      Tarih
                    </th>
                    <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider text-right">
                      İşlem
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {transactions.map((tx) => (
                    <tr
                      key={tx.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-accent/10">
                            <span className="text-[10px] font-medium text-accent">
                              {tx.referenceNumber.charAt(0)}
                            </span>
                          </div>
                          <div>
                            <p className="text-sm font-medium text-text-primary">
                              {tx.description}
                            </p>
                            <p className="text-xs text-text-secondary">
                              {tx.referenceNumber}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <span className="text-xs text-text-secondary">
                          {tx.transactionType === 'TRANSFER'
                            ? 'Transfer'
                            : tx.transactionType === 'DEPOSIT'
                              ? 'Yatırma'
                              : tx.transactionType === 'WITHDRAWAL'
                                ? 'Çekme'
                                : 'Ücret'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <span className={cn('text-sm font-medium', tx.amount >= 0 ? 'text-success' : 'text-error')}>
                          {tx.amount >= 0 ? '+' : '-'}{new Intl.NumberFormat('tr-TR', {
                            style: 'currency',
                            currency: tx.currency,
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2,
                          }).format(Math.abs(tx.amount))}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <span className="text-sm text-text-secondary">
                          {tx.currency}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <Badge
                          variant={
                            tx.status === 'COMPLETED'
                              ? 'success'
                              : tx.status === 'PENDING'
                                ? 'warning'
                                : tx.status === 'FAILED'
                                  ? 'error'
                                  : 'secondary'
                          }
                        >
                          {tx.status === 'COMPLETED'
                            ? 'Tamamlandı'
                            : tx.status === 'PENDING'
                              ? 'Beklemede'
                              : tx.status === 'FAILED'
                                ? 'Başarısız'
                                : 'Geri Alındı'}
                        </Badge>
                      </td>
                      <td className="px-6 py-4">
                        <span className="text-sm text-text-secondary">
                          {new Date(tx.createdAt).toLocaleDateString('tr-TR', {
                            day: '2-digit',
                            month: '2-digit',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit',
                          })}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <button
                          className="text-xs text-accent font-medium hover:underline"
                        >
                          Detay
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
        <CardFooter className="flex items-center justify-between">
          <div className="text-xs text-text-secondary">
            {currentPage * size + 1}-{Math.min((currentPage + 1) * size, totalElements)} / {totalElements}
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(currentPage - 1)}
              disabled={currentPage === 0}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <span className="text-xs text-text-secondary">
              Sayfa {currentPage + 1} / {Math.max(totalPages, 1)}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(currentPage + 1)}
              disabled={currentPage >= totalPages - 1}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </CardFooter>
      </Card>
    </div>
  )
}
