import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'
import { Label } from './Label'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export interface InputProps
  extends React.InputHTMLAttributes<HTMLInputElement> {
  error?: boolean
  label?: string
}

export function Input({
  className,
  error,
  label,
  id,
  ...props
}: InputProps) {
  return (
    <div className="w-full">
      {label && (
        <Label htmlFor={id} className="mb-1.5 block text-sm font-medium text-text-secondary">
          {label}
        </Label>
      )}
      <input
        id={id}
        className={cn(
          'flex h-10 w-full rounded-md border border-border bg-surface px-4 py-2 text-sm shadow-sm transition-colors file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-text-secondary focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-accent focus-visible:border-accent disabled:cursor-not-allowed disabled:opacity-50',
          error
            ? 'border-error focus-visible:ring-error'
            : 'border-border',
          className,
        )}
        {...props}
      />
    </div>
  )
}
