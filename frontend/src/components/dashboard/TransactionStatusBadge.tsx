import { cn } from '@/lib/utils'

export interface TransactionStatusBadgeProps {
  status: 'COMPLETED' | 'PENDING' | 'FAILED'
}

const statusConfig: Record<string, { bg: string; text: string; label: string }> = {
  COMPLETED: { bg: 'bg-success/10', text: 'text-success', label: 'Tamamlandi' },
  PENDING: { bg: 'bg-warning/10', text: 'text-warning', label: 'Beklemede' },
  FAILED: { bg: 'bg-error/10', text: 'text-error', label: 'Basarisiz' },
}

export function TransactionStatusBadge({ status }: TransactionStatusBadgeProps) {
  const config = statusConfig[status]
  return (
    <span className={cn('inline-flex items-center rounded-full px-2 py-1 text-xs font-medium', config.bg, config.text)}>
      {config.label}
    </span>
  )
}
