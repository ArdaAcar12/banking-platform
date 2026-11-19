import { Navigate, Routes, Route } from 'react-router-dom'
import { AppLayout } from '@/layouts/AppLayout'
import { PrivateRoute } from '@/routes/PrivateRoute'
import { AdminRoute } from '@/routes/AdminRoute'
import { GuestRoute } from '@/routes/GuestRoute'
import { AuthRedirect } from '@/routes/AuthRedirect'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { RegisterPage } from '@/features/auth/pages/RegisterPage'
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage'
import { ProfilePage } from '@/features/profile/pages/ProfilePage'
import { UserListPage } from '@/features/admin/pages/UserListPage'
import { AccountsPage } from '@/features/dashboard/pages/AccountsPage'
import { TransactionHistoryPage } from '@/features/dashboard/pages/TransactionHistoryPage'
import { ComingSoonPage } from '@/features/dashboard/pages/ComingSoonPage'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<AppLayout />}>
        {/* Kök rotaya geldiğinde kimlik durumuna göre yönlendir */}
        <Route index element={<AuthRedirect />} />

        {/* Public auth routes (giriş yapılmışsa dashboard'a yönlendirir) */}
        <Route
          path="login"
          element={<GuestRoute><LoginPage /></GuestRoute>}
        />
        <Route
          path="register"
          element={<GuestRoute><RegisterPage /></GuestRoute>}
        />

        {/* Protected routes */}
        <Route
          path="dashboard"
          element={<PrivateRoute><DashboardPage /></PrivateRoute>}
        />
        <Route
          path="profile"
          element={<PrivateRoute><ProfilePage /></PrivateRoute>}
        />

        {/* Coming soon modules (protected) */}
        <Route
          path="accounts"
          element={<PrivateRoute><AccountsPage /></PrivateRoute>}
        />
        <Route
          path="transactions"
          element={<PrivateRoute><TransactionHistoryPage /></PrivateRoute>}
        />
        <Route
          path="transfers"
          element={
            <PrivateRoute>
              <ComingSoonPage
                moduleName="Para Transferi"
                description="Hesaplar arası para transferi ve havale işlemleri yakında eklenecektir."
              />
            </PrivateRoute>
          }
        />
        <Route
          path="cards"
          element={
            <PrivateRoute>
              <ComingSoonPage
                moduleName="Kartlarım"
                description="Bankkartları, kredi kartları ve sanal kart yönetimi yakında eklenecektir."
              />
            </PrivateRoute>
          }
        />
        <Route
          path="payments"
          element={
            <PrivateRoute>
              <ComingSoonPage
                moduleName="Ödemeler"
                description="Fatura ödemeleri ve otomatik ödeme işlemleri yakında eklenecektir."
              />
            </PrivateRoute>
          }
        />
        <Route
          path="analytics"
          element={
            <PrivateRoute>
              <ComingSoonPage
                moduleName="Analizler"
                description="Finansal analizler, harcamalarınızla ilgili raporlar yakında eklenecektir."
              />
            </PrivateRoute>
          }
        />
        <Route
          path="security"
          element={
            <PrivateRoute>
              <ComingSoonPage
                moduleName="Güvenlik"
                description="Şifre değiştirme, iki aşamalı doğrulama ve oturum güvenliği yakında eklenecektir."
              />
            </PrivateRoute>
          }
        />

        {/* Admin routes */}
        <Route path="admin" element={<Navigate to="/admin/users" replace />} />
        <Route
          path="admin/users"
          element={
            <AdminRoute>
              <UserListPage />
            </AdminRoute>
          }
        />
      </Route>
    </Routes>
  )
}
