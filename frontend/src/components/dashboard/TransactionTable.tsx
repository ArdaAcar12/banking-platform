import { cn } from '@/lib/utils'

export interface TransactionTableProps {
  transactions: Array<{
    id: string
    description: string
    category: string
    amount: number
    type: 'INCOME' | 'EXPENSE'
    date: string
    status: 'COMPLETED' | 'PENDING' | 'FAILED'
    merchant?: string
  }>
}

const statusConfig: Record<string, { bg: string; text: string; label: string }> = {
  COMPLETED: { bg: 'bg-success/10', text: 'text-success', label: 'Tamamlandi' },
  PENDING: { bg: 'bg-warning/10', text: 'text-warning', label: 'Beklemede' },
  FAILED: { bg: 'bg-error/10', text: 'text-error', label: 'Basarisiz' },
}

const typeConfig: Record<string, { bg: string; text: string; label: string }> = {
  INCOME: { bg: 'bg-success/10', text: 'text-success', label: 'Gelir' },
  EXPENSE: { bg: 'bg-error/10', text: 'text-error', label: 'Gider' },
}

export function TransactionTable({ transactions }: TransactionTableProps) {
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency: 'TRY',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value)
  }

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('tr-TR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    })
  }

  return (
    <div className="rounded-xl border border-border bg-surface shadow-sm overflow-hidden">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="border-b border-border">
            <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
              Islem
            </th>
            <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
              Kategori
            </th>
            <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider text-right">
              Tutar
            </th>
            <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
              Tarih
            </th>
            <th className="px-6 py-4 text-xs font-medium text-text-secondary uppercase tracking-wider">
              Durum
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {transactions.map((tx) => {
            const status = statusConfig[tx.status]
            const type = typeConfig[tx.type]
            return (
              <tr key={tx.id} className="hover:bg-muted/50 transition-colors">
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded-full bg-accent/10">
                      <span className="text-[10px] font-medium text-accent">
                        {tx.merchant ? tx.merchant.charAt(0) : tx.description.charAt(0)}
                      </span>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-text-primary">
                        {tx.description}
                      </p>
                      {tx.merchant && (
                        <p className="text-xs text-text-secondary">
                          {tx.merchant}
                        </p>
                      )}
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className="text-xs text-text-secondary">
                    {tx.category}
                  </span>
                </td>
                <td className="px-6 py-4 text-right">
                  <span className={cn('text-sm font-medium', type.text)}>
                    {type.label === 'Gelir' ? '+' : '-'}
                    {formatCurrency(Math.abs(tx.amount))}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <span className="text-sm text-text-secondary">
                    {formatDate(tx.date)}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <span className={cn('inline-flex items-center rounded-full px-2 py-1 text-xs font-medium', status.bg, status.text)}>
                    {status.label}
                  </span>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
