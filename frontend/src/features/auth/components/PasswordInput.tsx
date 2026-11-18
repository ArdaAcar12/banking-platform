import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

interface PasswordInputProps {
  value: string
  onChange: (value: string) => void
  placeholder?: string
  disabled?: boolean
  error?: boolean
}

export function PasswordInput({
  value,
  onChange,
  placeholder,
  disabled,
  error,
}: PasswordInputProps) {
  const [showPassword, setShowPassword] = React.useState(false)

  return (
    <div className="relative">
      <input
        type={showPassword ? 'text' : 'password'}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder || 'Şifre'}
        disabled={disabled}
        className={`w-full h-10 rounded-md border border-border bg-surface px-4 py-2 pr-10 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-accent focus-visible:border-accent disabled:cursor-not-allowed disabled:opacity-50 ${
          error
            ? 'border-error focus-visible:ring-error'
            : 'border-border'
        }`}
      />
      <button
        type="button"
        onClick={() => setShowPassword(!showPassword)}
        className="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary hover:text-text-primary"
        aria-label={showPassword ? 'Şifreyi gizle' : 'Şifreyi göster'}
      >
        {showPassword ? '🙈' : '👁'}
      </button>
    </div>
  )
}

import React from 'react'
