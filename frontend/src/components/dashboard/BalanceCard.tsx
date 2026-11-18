export interface BalanceCardProps {
  totalBalance: number
  availableBalance: number
  currency: string
  onToggleVisibility?: () => void
  isVisible?: boolean
}

export function BalanceCard({
  totalBalance,
  availableBalance,
  currency,
  onToggleVisibility,
  isVisible = true,
}: BalanceCardProps) {
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value)
  }

  return (
    <div className="relative rounded-2xl border border-border bg-surface p-8 shadow-sm overflow-hidden">
      <div className="absolute top-0 right-0 p-4 opacity-10">
        <svg
          width="120"
          height="120"
          viewBox="0 0 120 120"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <rect
            width="100"
            height="100"
            fill="url(#blurGradient)"
            filter="url(#blur)"
          />
          <defs>
            <filter id="blur">
              <feGaussianBlur stdDeviation="20" />
            </filter>
            <linearGradient id="blurGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#16B89A" />
              <stop offset="100%" stopColor="#101B33" />
            </linearGradient>
          </defs>
        </svg>
      </div>

      <div className="relative z-10 flex flex-col lg:flex-row lg:items-start justify-between gap-6">
        <div className="flex-1">
          <p className="text-xs font-medium text-text-secondary uppercase tracking-wider">
            Toplam Varlık
          </p>
          <div className="flex items-baseline gap-1 mt-1">
            <span className="text-3xl font-bold text-text-primary">
              {isVisible ? formatCurrency(totalBalance) : '••••••••'}
            </span>
            <span className="text-sm font-medium text-text-secondary">
              {currency === 'TRY' ? '₺' : currency}
            </span>
          </div>
          <p className="text-xs text-text-secondary mt-2">
            {isVisible
              ? 'Kullanılabilir bakiye: ' + formatCurrency(availableBalance)
              : 'Görünürlüğü açmak için tıklayın'}
          </p>
        </div>
        <div className="flex items-end gap-4">
          <button
            onClick={onToggleVisibility}
            className="p-2 rounded-lg text-text-secondary hover:bg-accent/5 hover:text-text-primary transition-colors"
            aria-label={isVisible ? 'Bakiyeyi gizle' : 'Bakiyeyi göster'}
          >
            {isVisible ? '🙈' : '👁'}
          </button>
          <button className="px-4 py-2.5 text-sm font-medium text-accent-foreground bg-accent rounded-lg hover:bg-accent-hover transition-colors">
            Detaylar
          </button>
        </div>
      </div>

      <div className="relative z-10 mt-6 flex items-center gap-4 border-t border-border pt-6">
        <div className="flex items-center gap-2 text-xs text-text-secondary">
          <span className="h-2 w-2 rounded-full bg-success" />
          <span>Önceki haftaya göre +4.2%</span>
        </div>
        <div className="flex items-center gap-2 text-xs text-text-secondary">
          <span className="h-2 w-2 rounded-full bg-warning" />
          <span>3 bekleyen işlem</span>
        </div>
      </div>
    </div>
  )
}
