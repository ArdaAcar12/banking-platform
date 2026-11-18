import { cn } from '@/lib/utils'

export interface StatCardProps {
  title: string
  value: string | number
  subtitle?: string
  icon: React.ElementType
  variant?: 'default' | 'accent' | 'outline' | 'secondary'
  trend?: {
    value: string
    positive: boolean
  }
}

export function StatCard({
  title,
  value,
  subtitle,
  icon: Icon,
  variant = 'default',
  trend,
}: StatCardProps) {
  const variantConfigs: Record<string, { bg: string; text: string; border?: string }> = {
    default: { bg: 'bg-surface', text: 'text-text-primary' },
    accent: { bg: 'bg-accent/10', text: 'text-accent', border: 'border-accent/20' },
    outline: { bg: 'bg-surface', text: 'text-text-secondary', border: 'border-border' },
    secondary: { bg: 'bg-surface', text: 'text-text-secondary' },
  }
  const config = variantConfigs[variant]

  return (
    <div className="rounded-xl border border-border bg-surface p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium {config.text}">{title}</p>
          {subtitle && (
            <p className="text-xs text-text-secondary mt-1">{subtitle}</p>
          )}
          <div className="mt-2">
            <span className="text-xl font-bold {config.text}">{value}</span>
          </div>
        </div>
        <div className={`p-2.5 rounded-lg ${config.bg} ${config.border || ''}`}>
          <Icon className={`h-5 w-5 ${config.text}`} />
        </div>
      </div>
      {trend && (
        <div className="mt-4 flex items-center gap-1">
          <span className={cn('text-xs font-medium', trend.positive ? 'text-success' : 'text-error')}>
            {trend.positive ? '▲' : '▼'} {trend.value}
          </span>
          <span className="text-xs text-text-secondary">önceki ay</span>
        </div>
      )}
    </div>
  )
}
