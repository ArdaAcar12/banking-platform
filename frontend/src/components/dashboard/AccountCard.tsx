export interface AccountCardProps {
  name: string
  type: string
  maskedIban: string
  lastFour: string
  balance: number
  currency: string
  demo?: boolean
}

export function AccountCard({
  name,
  type,
  maskedIban,
  lastFour,
  balance,
  currency,
  demo = false,
}: AccountCardProps) {
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value)
  }

  const typeLabels: Record<string, string> = {
    CHECKING: 'Vadesiz Hesap',
    SAVINGS: 'Birikim Hesabi',
    DAILY_SPENDING: 'Gunluk Harcama',
  }

  return (
    <div className="relative rounded-xl border border-border bg-surface p-5 shadow-sm overflow-hidden group">
      <div className="absolute top-0 right-0 p-3 opacity-5">
        <svg
          width="80"
          height="80"
          viewBox="0 0 80 80"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <rect width="60" height="60" fill="url(#blurGradient)" filter="url(#blur)" />
          <defs>
            <filter id="blur">
              <feGaussianBlur stdDeviation="15" />
            </filter>
            <linearGradient id="blurGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#16B89A" />
              <stop offset="100%" stopColor="#101B33" />
            </linearGradient>
          </defs>
        </svg>
      </div>

      <div className="relative z-10 flex flex-col lg:flex-row justify-between items-start gap-4">
        <div className="flex-1">
          <div className="flex items-baseline gap-2 mb-1">
            <h3 className="text-sm font-bold text-text-primary">{name}</h3>
            {demo && (
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-warning/10 text-warning text-[10px] font-medium">
                DEMO
              </span>
            )}
          </div>
          <p className="text-xs text-text-secondary">
            {typeLabels[type] || type} • {maskedIban}
          </p>
        </div>
        <div className="text-right">
          <span className="text-lg font-bold text-text-primary">
            {formatCurrency(balance)}
          </span>
          <span className="text-xs text-text-secondary">
            {currency === 'TRY' ? '₺' : currency}
          </span>
        </div>
      </div>

      <div className="relative z-10 mt-4 pt-4 border-t border-border flex items-center justify-between">
        <div className="flex items-center gap-1.5 text-xs text-text-secondary">
          <span className="h-1.5 w-1.5 rounded-full bg-text-secondary" />
          <span>Son 4 hane: {lastFour}</span>
        </div>
      </div>
    </div>
  )
}
