import { cn } from '@/lib/utils'
import type { Transaction } from '@/types/transaction'

export interface TransactionTableProps {
  transactions: Transaction[]
}

const statusConfig: Record<string, { bg: string; text: string; label: string }> = {
  COMPLETED: { bg: 'bg-success/10', text: 'text-success', label: 'Tamamlandı' },
  PENDING: { bg: 'bg-warning/10', text: 'text-warning', label: 'Beklemede' },
  FAILED: { bg: 'bg-error/10', text: 'text-error', label: 'Başarısız' },
  REVERSED: { bg: 'bg-error/10', text: 'text-error', label: 'Geri Alındı' },
}

const typeConfig: Record<string, { bg: string; text: string; label: string }> = {
  TRANSFER: { bg: 'bg-primary/10', text: 'text-primary', label: 'Transfer' },
  DEPOSIT: { bg: 'bg-success/10', text: 'text-success', label: 'Yatırma' },
  WITHDRAWAL: { bg: 'bg-error/10', text: 'text-error', label: 'Çekme' },
  FEE: { bg: 'bg-warning/10', text: 'text-warning', label: 'Ücret' },
}

export function TransactionTable({ transactions }: TransactionTableProps) {
  const formatCurrency = (value: number, currency: string = 'TRY') => {
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value)
  }

  return (
    <div className="rounded-xl border border-border bg-surface shadow-sm overflow-hidden">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="border-b border-border">
            <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
              İşlem
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
              İşlem Detayı
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {transactions.map((tx) => {
            const status = statusConfig[tx.status]
            const type = typeConfig[tx.transactionType]
            return (
              <tr key={tx.id} className="hover:bg-muted/50 transition-colors">
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
                    {type.label}
                  </span>
                </td>
                <td className="px-6 py-4 text-right">
                  <span className={cn('text-sm font-medium', tx.amount >= 0 ? 'text-success' : 'text-error')}>
                    {tx.amount >= 0 ? '+' : '-'}{formatCurrency(Math.abs(tx.amount), tx.currency)}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <span className="text-sm text-text-secondary">
                    {tx.currency}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <span className={cn('inline-flex items-center rounded-full px-2 py-1 text-xs font-medium', status.bg, status.text)}>
                    {status.label}
                  </span>
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
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
