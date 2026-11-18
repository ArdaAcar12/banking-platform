import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export interface AlertProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'info' | 'success' | 'warning' | 'error'
}

const alertVariants: Record<'info' | 'success' | 'warning' | 'error', string> = {
  info: 'bg-blue-50 border-blue-200 text-blue-900',
  success: 'bg-success/10 border-success/20 text-success',
  warning: 'bg-warning/10 border-warning/20 text-warning',
  error: 'bg-error/10 border-error/20 text-error',
}

export function Alert({
  className,
  variant = 'info',
  ...props
}: AlertProps) {
  return (
    <div
      role="alert"
      className={cn(
        'rounded-lg border p-4 text-sm flex items-start gap-3',
        alertVariants[variant],
        className,
      )}
      {...props}
    />
  )
}
