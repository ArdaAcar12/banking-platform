import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { Search, Bell, MessageCircle, ShieldCheck } from 'lucide-react'
import { Badge } from '@/components/ui/Badge'
import { cn } from '@/lib/utils'

interface HeaderProps {
  user: {
    id: string
    username: string
    email: string
    role: string
    firstName: string
    lastName: string
  }
}

export default function Header({ user }: HeaderProps) {
  const location = useLocation()
  const { logout } = useAuth()

  return (
    <header
      className="h-16 border-b border-border bg-surface/95 backdrop-blur flex items-center justify-between px-6 shrink-0 z-20"
    >
      <div className="flex items-center gap-1">
        <button className="md:hidden p-2 text-text-secondary hover:text-text-primary">
          <span className="h-5 w-5 rounded-full bg-accent/10 flex items-center justify-center text-accent">
            ☰
          </span>
        </button>
        <Link
          to="/dashboard"
          className={cn(
            'flex items-center gap-2 font-bold text-primary tracking-tight',
            location.pathname === '/dashboard' ? 'text-accent' : '',
          )}
        >
          <ShieldCheck className="h-5 w-5 text-accent" />
          <span className="text-sm">Banking Platform</span>
        </Link>
      </div>

      <div className="hidden md:flex items-center gap-6 flex-1 max-w-2xl mx-auto">
        <div className="relative w-full max-w-md">
          <Search
            className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-text-secondary"
          />
          <input
            type="search"
            placeholder="Hesap, işlem veya kart ara..."
            className="w-full h-9 pl-10 pr-4 rounded-lg border border-border bg-background text-sm text-text-primary focus:outline-none focus:ring-1 focus:ring-accent transition-colors"
          />
        </div>
      </div>

      <div className="flex items-center gap-4">
        <button
          className="hidden md:flex items-center gap-2 px-3 py-2 rounded-lg text-text-secondary hover:bg-accent/5 transition-colors"
        >
          <Bell className="h-5 w-5" />
        </button>
        <button
          className="hidden md:flex items-center gap-2 px-3 py-2 rounded-lg text-text-secondary hover:bg-accent/5 transition-colors"
        >
          <MessageCircle className="h-5 w-5" />
        </button>
        <div className="flex items-center gap-3 ml-4">
          <div className="hidden md:flex flex-col items-end">
            <span className="text-sm font-medium text-text-primary">
              {user.firstName} {user.lastName}
            </span>
            <Badge variant="outline" className="text-xs">
              {user.role}
            </Badge>
          </div>
          <button
            onClick={logout}
            className="flex h-8 w-8 items-center justify-center rounded-full bg-accent text-accent-foreground text-sm font-bold transition-colors hover:bg-accent-hover"
          >
            {user.firstName.charAt(0)}{user.lastName.charAt(0)}
          </button>
        </div>
      </div>
    </header>
  )
}
