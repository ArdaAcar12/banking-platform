import { Outlet } from 'react-router-dom'
import { useAuth } from '@/features/auth/hooks/useAuth'
import Sidebar from '@/components/layout/Sidebar'
import Header from '@/components/layout/Header'

export function AppLayout() {
  const { user } = useAuth()

  return (
    <div className="flex h-screen w-full bg-background overflow-hidden">
      {user ? (
        <>
          <Sidebar user={user} />
          <div className="flex-1 flex flex-col overflow-hidden">
            <Header user={user} />
            <main className="flex-1 overflow-y-auto bg-background p-6 md:p-8">
              <Outlet />
            </main>
          </div>
        </>
      ) : (
        <main className="flex-1 overflow-y-auto bg-background p-6 md:p-8">
          <Outlet />
        </main>
      )}
    </div>
  )
}
