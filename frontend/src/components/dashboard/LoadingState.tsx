import { cn } from '@/lib/utils'

export interface LoadingStateProps {
  className?: string
}

export function LoadingState({ className }: LoadingStateProps) {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center py-12',
        className,
      )}
    >
      <div className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-border border-t-accent animate-spin mb-4" />
      <p className="text-sm text-text-secondary">Yukleniyor...</p>
    </div>
  )
}
