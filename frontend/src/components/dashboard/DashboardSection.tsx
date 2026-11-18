export interface DashboardSectionProps {
  title: string
  subtitle?: string
  actions?: React.ReactNode
  children: React.ReactNode
}

export function DashboardSection({
  title,
  subtitle,
  actions,
  children,
}: DashboardSectionProps) {
  return (
    <div className="mb-8">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 className="text-lg font-bold text-text-primary">{title}</h2>
          {subtitle && <p className="text-sm text-text-secondary">{subtitle}</p>}
        </div>
        {actions && <div className="flex items-center gap-2">{actions}</div>}
      </div>
      <div className="rounded-xl border border-border bg-surface shadow-sm overflow-hidden">
        {children}
      </div>
    </div>
  )
}
