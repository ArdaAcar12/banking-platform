import * as React from 'react'
import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export interface IconProps extends React.SVGProps<SVGSVGElement> {
  size?: number | string
}

export function Icon({ size = 20, className, ...props }: IconProps) {
  // This is a wrapper that delegates to lucide-react icons
  // Usage: <Icon component={LucideUser} />
  const Component = (props as any).component
  if (!Component) {
    return null
  }
  return (
    <Component
      size={size}
      className={cn('shrink-0', className)}
      {...(props as any)}
    />
  )
}
