import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

type ButtonVariant = 'default' | 'secondary' | 'accent' | 'outline' | 'ghost' | 'danger'
type ButtonSize = 'default' | 'sm' | 'lg'

const variantClasses: Record<ButtonVariant, string> = {
  default: 'bg-primary text-primary-foreground hover:bg-primary-hover shadow-sm',
  secondary:
    'bg-secondary text-secondary-foreground hover:bg-secondary-hover shadow-sm',
  accent: 'bg-accent text-accent-foreground hover:bg-accent-hover shadow-sm',
  outline: 'border border-border bg-background hover:bg-accent hover:text-accent-foreground shadow-sm',
  ghost: 'hover:bg-accent hover:text-accent-foreground shadow-sm',
  danger: 'bg-error text-error-foreground hover:bg-error-hover shadow-sm',
}

const sizeClasses: Record<ButtonSize, string> = {
  default: 'h-10 px-4 py-2',
  sm: 'h-9 rounded-md px-3',
  lg: 'h-11 rounded-md px-8',
}

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  size?: ButtonSize
}

export function Button({
  className,
  variant = 'default',
  size = 'default',
  ...props
}: ButtonProps) {
  return (
    <button
      className={cn(
        'inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
        variantClasses[variant],
        sizeClasses[size],
        className,
      )}
      {...props}
    />
  )
}
