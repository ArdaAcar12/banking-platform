import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { PasswordInput } from './PasswordInput'
import { Alert } from '@/components/ui/Alert'
import { Link } from 'react-router-dom'

const loginSchema = z.object({
  identifier: z
    .string()
    .min(1, 'E-posta veya kullanıcı adı zorunludur.')
    .max(255, '255 karakterden uzun olamaz.'),
  password: z.string().min(1, 'Parola zorunludur.'),
})

export function LoginForm() {
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const navigate = useNavigate()
  const { login } = useAuth()

  const form = useForm<z.infer<typeof loginSchema>>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      identifier: '',
      password: '',
    },
  })

  const onSubmit = async (data: z.infer<typeof loginSchema>) => {
    setError(null)
    setIsSubmitting(true)
    try {
      await login({ identifier: data.identifier, password: data.password })
      navigate('/dashboard', { replace: true })
    } catch (err: any) {
      setError('E-posta/kullanıcı adı veya parola hatalı.')
      setIsSubmitting(false)
    }
  }

  return (
    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
      {error && (
        <Alert variant="error">{error}</Alert>
      )}
      <div className="space-y-3">
        <div className="relative">
          <Input
            {...form.register('identifier')}
            type="email"
            placeholder="E-posta veya kullanıcı adı"
            label="E-posta veya kullanıcı adı"
            error={form.formState.errors.identifier ? true : false}
          />
          {form.formState.errors.identifier && (
            <p className="text-xs text-error mt-1">
              {form.formState.errors.identifier.message}
            </p>
          )}
        </div>
        <div className="relative">
          <PasswordInput
            value={form.watch('password') || ''}
            onChange={(val) => form.setValue('password', val)}
            placeholder="Parola"
            disabled={isSubmitting}
            error={form.formState.errors.password ? true : false}
          />
          {form.formState.errors.password && (
            <p className="text-xs text-error mt-1">
              {form.formState.errors.password.message}
            </p>
          )}
        </div>
        <div className="flex items-center justify-between">
          <label className="flex items-center gap-2 text-sm text-text-secondary cursor-pointer">
            <input
              type="checkbox"
              className="rounded border-border text-accent focus:ring-accent"
            />
            Beni hatırla
          </label>
          <Link
            to="/password-reset"
            className="text-xs text-primary hover:text-primary-hover underline underline-offset-4"
          >
            Şifremi unuttum
          </Link>
        </div>
        <Button
          type="submit"
          variant="accent"
          className="w-full"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Giriş yapılıyor...' : 'Giriş Yap'}
        </Button>
      </div>
      <p className="text-sm text-text-secondary text-center">
        Henüz hesabınız yok mu?{' '}
        <Link
          to="/register"
          className="font-medium text-primary hover:text-primary-hover"
        >
          Kayıt olun
        </Link>
      </p>
    </form>
  )
}

import { useNavigate } from 'react-router-dom'
