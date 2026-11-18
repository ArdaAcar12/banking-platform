export interface EmptyStateProps {
  title: string
  subtitle?: string
  icon: React.ElementType
  action?: React.ReactNode
}

export function EmptyState({
  title,
  subtitle,
  icon: Icon,
  action,
}: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-muted/50 mb-4">
        <Icon className="h-8 w-8 text-text-secondary" />
      </div>
      <h3 className="text-lg font-bold text-text-primary">{title}</h3>
      {subtitle && (
        <p className="mt-2 text-sm text-text-secondary max-w-sm">{subtitle}</p>
      )}
      {action && <div className="mt-6">{action}</div>}
    </div>
  )
}
