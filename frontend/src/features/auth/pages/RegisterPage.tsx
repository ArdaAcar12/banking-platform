import { ShieldCheck, Mail, Lock, Sparkles } from 'lucide-react'
import { RegisterForm } from '@/features/auth/components/RegisterForm'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/Card'
import { CardDescription } from '@/components/ui/Card'
import { CardHeader } from '@/components/ui/Card'
import { CardTitle } from '@/components/ui/Card'
import { Link } from 'react-router-dom'

export function RegisterPage() {
  return (
    <div className="flex min-h-screen w-full bg-background">
      {/* Left: Brand & Marketing Area */}
      <div className="hidden md:flex w-1/2 bg-primary flex-col justify-between p-10">
        <div className="flex flex-col gap-4">
          <div className="flex items-center gap-2">
            <ShieldCheck className="h-6 w-6 text-accent" />
            <span className="text-2xl font-bold text-white tracking-tight">
              Banking
            </span>
            <span className="text-2xl font-bold text-white tracking-tight">
              Platform
            </span>
          </div>
          <h1 className="text-3xl md:text-4xl font-bold text-white leading-tight">
            Hesabınızı oluşturun
          </h1>
          <p className="text-secondary-foreground/90 text-lg leading-relaxed max-w-md">
            Modern, şeffaf ve güvenli bir bankacılık deneyimi için
            hesabınızı oluşturun. Kayıt işlemi 2 dakikayı bulmaz.
          </p>
          <div className="flex items-center gap-3 text-sm text-secondary-foreground/80">
            <Mail className="h-4 w-4" />
            <span>Hızlı kurulum, 2 dakikada hesabınız hazır</span>
          </div>
          <div className="flex items-center gap-3 text-sm text-secondary-foreground/80 mt-2">
            <Lock className="h-4 w-4" />
            <span>End-to-end şifreleme ile veri koruması</span>
          </div>
          <div className="flex items-center gap-3 text-sm text-secondary-foreground/80 mt-2">
            <Sparkles className="h-4 w-4" />
            <span>7/24 destek ve anlık bildirimler</span>
          </div>
        </div>
        <div className="mt-12 flex items-center justify-between gap-4 text-xs text-secondary-foreground/70">
          <div className="flex items-center gap-2">
            <span>2026 Banking Platform</span>
          </div>
          <div className="flex items-center gap-2">
            <span>© Tüm hakları saklıdır</span>
          </div>
        </div>
      </div>

      {/* Right: Register Form */}
      <div className="flex flex-1 items-center justify-center bg-background p-8 md:p-0">
        <Card className="w-full max-w-md shadow-xl border-border">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold text-text-primary">
              Yeni hesap oluştur
            </CardTitle>
            <CardDescription className="text-text-secondary">
              Kayıt sırasında varsayılan rolünüz CUSTOMER olarak atanır.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <RegisterForm />
          </CardContent>
          <CardDescription className="p-6 pt-0 text-center">
            <p className="text-sm text-text-secondary">
              Zaten hesabınız var mı?{' '}
              <Link to="/login" className="font-medium text-primary hover:text-primary-hover">
                Giriş yapın
              </Link>
            </p>
          </CardDescription>
        </Card>
      </div>
    </div>
  )
}
