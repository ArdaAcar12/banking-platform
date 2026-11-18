import React from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { X, ChevronRight, LogOut, ShieldCheck } from 'lucide-react'
import { cn } from '@/lib/utils'

interface SidebarProps {
  user: {
    id: string
    username: string
    email: string
    role: string
    firstName: string
    lastName: string
  }
}

interface NavItem {
  name: string
  href: string
  icon: React.ElementType
  demo?: boolean
}

const navItems: NavItem[] = [
  { name: 'Genel Bakış', href: '/dashboard', icon: ShieldCheck },
  { name: 'Hesaplarım', href: '/accounts', icon: ShieldCheck, demo: true },
  { name: 'Para Transferi', href: '/transfers', icon: ShieldCheck, demo: true },
  { name: 'İşlem Geçmişi', href: '/transactions', icon: ShieldCheck, demo: true },
  { name: 'Kartlarım', href: '/cards', icon: ShieldCheck, demo: true },
  { name: 'Analizler', href: '/analytics', icon: ShieldCheck, demo: true },
  { name: 'Profilim', href: '/profile', icon: ShieldCheck },
  { name: 'Güvenlik', href: '/security', icon: ShieldCheck },
]

export default function Sidebar({ user }: SidebarProps) {
  const { logout } = useAuth()
  const location = useLocation()
  const [collapsed, setCollapsed] = React.useState(false)
  const [mobileOpen, setMobileOpen] = React.useState(false)

  return (
    <>
      {/* Mobile Backdrop */}
      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/40 backdrop-blur-sm md:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-50 w-64 bg-surface border-r border-border flex flex-col transition-all duration-300 md:relative md:translate-x-0',
          collapsed && '-translate-x-full md:translate-x-0',
          mobileOpen && '-translate-x-full md:translate-x-0',
        )}
      >
        <div className="flex items-center justify-between h-16 px-6 bg-surface border-b border-border shrink-0">
          <div className="flex items-center gap-2">
            <ShieldCheck className="h-6 w-6 text-accent" />
            <span className={cn('font-bold text-primary text-sm tracking-tight', collapsed && 'hidden')}>
              Banking
            </span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setCollapsed(!collapsed)}
              className="p-2 text-text-secondary hover:text-text-primary rounded-lg hover:bg-accent/5 transition-colors"
              aria-label="Menüyü daralt"
              title={collapsed ? 'Menüyü aç' : 'Menüyü daralt'}
            >
              {collapsed ? (
                <X className="h-5 w-5" />
              ) : (
                <ChevronRight className="h-5 w-5" />
              )}
            </button>
            {collapsed ? null : (
              <button
                onClick={() => setMobileOpen(true)}
                className="md:hidden p-2 text-text-secondary hover:text-text-primary rounded-lg hover:bg-accent/5 transition-colors"
                aria-label="Mobil menüyü aç"
              >
                <span className="h-5 w-5">☰</span>
              </button>
            )}
          </div>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
          {navItems.map((item) => {
            const Icon = item.icon
            const isActive =
              location.pathname === item.href ||
              location.pathname.startsWith(item.href + '/')
            return (
              <Link
                key={item.name}
                to={item.href}
                onClick={() => setMobileOpen(false)}
                className={cn(
                  'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors group',
                  isActive
                    ? 'bg-primary/10 text-primary border-r-2 border-primary'
                    : 'text-text-secondary hover:bg-accent/5 hover:text-text-primary',
                  item.demo ? 'opacity-70' : '',
                )}
              >
                <Icon className={cn('h-4 w-4 shrink-0', isActive ? 'text-accent' : 'text-text-secondary/60', 'group-hover:text-text-primary')} />
                <span className={cn(collapsed && 'hidden', isActive ? 'font-semibold' : 'font-normal')}>
                  {item.name}
                </span>
                {item.demo && (
                  <span className="ml-auto text-[10px] px-1.5 py-0.5 rounded bg-warning/10 text-warning text-[10px] font-medium">
                    DEMO
                  </span>
                )}
                <ChevronRight
                  className="h-3.5 w-3.5 ml-auto text-text-secondary/40 shrink-0 group-hover:text-text-primary transition-colors"
                />
              </Link>
            )
          })}
        </nav>

        <div className="p-4 border-t border-border">
          <div className="mb-4 flex flex-col items-end">
            <span className={cn('text-sm font-medium text-text-primary', collapsed && 'hidden')}>
              {user.firstName} {user.lastName}
            </span>
            <span className={cn('text-xs text-text-secondary', collapsed && 'hidden')}>
              {user.email}
            </span>
          </div>
          <button
            onClick={logout}
            className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-error hover:bg-error/5 transition-colors"
          >
            <LogOut className="h-4 w-4 shrink-0" />
            <span className={cn(collapsed && 'hidden')}>Çıkış Yap</span>
          </button>
        </div>
      </aside>

      {/* Mobile Sidebar Drawer */}
      {mobileOpen && (
        <aside
          className="fixed inset-y-0 left-0 z-50 w-64 bg-surface border-r border-border flex flex-col transform transition-transform duration-300 -translate-x-full md:translate-x-0"
          aria-label="Mobil navigasyon"
        >
          <div className="flex items-center justify-between h-16 px-6 bg-surface border-b border-border shrink-0">
            <div className="flex items-center gap-2">
              <ShieldCheck className="h-6 w-6 text-accent" />
              <span className="font-bold text-primary text-sm tracking-tight">
                Banking
              </span>
            </div>
            <button
              onClick={() => setMobileOpen(false)}
              className="p-2 text-text-secondary hover:text-text-primary rounded-lg hover:bg-accent/5 transition-colors"
              aria-label="Menüyü kapat"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
            {navItems.map((item) => {
              const Icon = item.icon
              const isActive =
                location.pathname === item.href ||
                location.pathname.startsWith(item.href + '/')
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  onClick={() => setMobileOpen(false)}
                  className={cn(
                    'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors group',
                    isActive
                      ? 'bg-primary/10 text-primary border-r-2 border-primary'
                      : 'text-text-secondary hover:bg-accent/5 hover:text-text-primary',
                    item.demo ? 'opacity-70' : '',
                  )}
                >
                  <Icon className={cn('h-4 w-4 shrink-0', isActive ? 'text-accent' : 'text-text-secondary/60', 'group-hover:text-text-primary')} />
                  <span className={isActive ? 'font-semibold' : 'font-normal'}>
                    {item.name}
                  </span>
                  {item.demo && (
                    <span className="ml-auto text-[10px] px-1.5 py-0.5 rounded bg-warning/10 text-warning text-[10px] font-medium">
                      DEMO
                    </span>
                  )}
                  <ChevronRight className="h-3.5 w-3.5 ml-auto text-text-secondary/40 shrink-0 group-hover:text-text-primary transition-colors" />
                </Link>
              )
            })}
          </nav>

          <div className="p-4 border-t border-border">
            <div className="mb-4 flex flex-col items-end">
              <span className="text-sm font-medium text-text-primary">
                {user.firstName} {user.lastName}
              </span>
              <span className="text-xs text-text-secondary">{user.email}</span>
            </div>
            <button
              onClick={logout}
              className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-error hover:bg-error/5 transition-colors"
            >
              <LogOut className="h-4 w-4 shrink-0" />
              Çıkış Yap
            </button>
          </div>
        </aside>
      )}
    </>
  )
}
