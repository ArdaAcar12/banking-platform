import { useLocation } from 'react-router-dom'
import { cn } from '@/lib/utils'

export interface QuickActionCardProps {
  title: string
  href?: string
  onClick?: () => void
  icon: React.ElementType
  disabled?: boolean
  demo?: boolean
}

export function QuickActionCard({
  title,
  href,
  onClick,
  icon: Icon,
  disabled = false,
  demo = false,
}: QuickActionCardProps) {
  const location = useLocation()
  const isActive =
    location.pathname === href ||
    (href && location.pathname.startsWith(href + '/'))

  const handleClick = (e: React.MouseEvent) => {
    if (disabled) {
      e.preventDefault()
      return
    }
    onClick?.()
  }

  return (
    <div
      onClick={handleClick}
      className={cn(
        'rounded-xl border border-border bg-surface p-5 cursor-pointer transition-colors hover:bg-accent/5 hover:border-accent/20',
        disabled ? 'opacity-60 cursor-not-allowed' : '',
        isActive ? 'ring-1 ring-accent' : '',
      )}
    >
      <div className="p-2.5 rounded-lg bg-accent/10">
        <Icon className="h-5 w-5 text-accent shrink-0" />
      </div>
      <p className="mt-3 text-sm font-medium text-text-primary">{title}</p>
      <p className="text-xs text-text-secondary mt-0.5">
        {disabled ? 'Yakında' : 'API entegrasyonu bekleniyor'}
      </p>
      {demo && (
        <span className="absolute top-2 right-2 text-[10px] px-1.5 py-0.5 rounded bg-warning/10 text-warning text-[10px] font-medium">
          DEMO
        </span>
      )}
    </div>
  )
}
