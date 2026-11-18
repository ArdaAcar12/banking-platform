import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { PasswordInput } from './PasswordInput'
import { Alert } from '@/components/ui/Alert'
import { Link, useNavigate } from 'react-router-dom'

const registerSchema = z
  .object({
    email: z.string().min(1, 'E-posta zorunludur.').email('Geçerli bir e-posta giriniz.'),
    username: z
      .string()
      .min(3, 'Kullanıcı adı en az 3 karakter olmalıdır.')
      .max(30, 'Kullanıcı adı en fazla 30 karakter olabilir.')
      .regex(
        /^[a-zA-Z0-9_.-]+$/,
        'Kullanıcı adı sadece harf, rakam, alt çizgi, nokta ve tire içerebilir.',
      ),
    password: z.string().min(8, 'Parola en az 8 karakter olmalıdır.'),
    firstName: z.string().min(1, 'İsim zorunludur.').max(100, 'İsim en fazla 100 karakter olabilir.'),
    lastName: z.string().min(1, 'Soyisim zorunludur.').max(100, 'Soyisim en fazla 100 karakter olabilir.'),
  })

export function RegisterForm() {
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const form = useForm<z.infer<typeof registerSchema>>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      email: '',
      username: '',
      password: '',
      firstName: '',
      lastName: '',
    },
  })

  const onSubmit = async (data: z.infer<typeof registerSchema>) => {
    setError(null)
    setIsSubmitting(true)
    try {
      await register({
        email: data.email,
        username: data.username,
        password: data.password,
        firstName: data.firstName,
        lastName: data.lastName,
      })
      navigate('/login?registered=true')
    } catch (err: any) {
      if (err.response?.status === 409) {
        setError('Bu e-posta adresi veya kullanıcı adı zaten kayıtlı.')
      } else if (err.response?.status === 400) {
        setError('Lütfen formdaki hataları düzeltin.')
      } else {
        setError('Kayıt sırasında bir hata oluştu. Lütfen tekrar deneyin.')
      }
      setIsSubmitting(false)
    }
  }

  return (
    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
      {error && (
        <Alert variant="error">{error}</Alert>
      )}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        <div className="space-y-3">
          <Input
            {...form.register('firstName')}
            type="text"
            placeholder="Ad"
            label="Ad"
            error={form.formState.errors.firstName ? true : false}
          />
          {form.formState.errors.firstName && (
            <p className="text-xs text-error mt-1">
              {form.formState.errors.firstName.message}
            </p>
          )}
        </div>
        <div className="space-y-3">
          <Input
            {...form.register('lastName')}
            type="text"
            placeholder="Soyad"
            label="Soyad"
            error={form.formState.errors.lastName ? true : false}
          />
          {form.formState.errors.lastName && (
            <p className="text-xs text-error mt-1">
              {form.formState.errors.lastName.message}
            </p>
          )}
        </div>
      </div>
      <div className="space-y-3">
        <Input
          {...form.register('email')}
          type="email"
          placeholder="E-posta"
          label="E-posta"
          error={form.formState.errors.email ? true : false}
        />
        {form.formState.errors.email && (
          <p className="text-xs text-error mt-1">{form.formState.errors.email.message}</p>
        )}
      </div>
      <div className="space-y-3">
        <Input
          {...form.register('username')}
          type="text"
          placeholder="Kullanıcı adı"
          label="Kullanıcı adı"
          error={form.formState.errors.username ? true : false}
        />
        {form.formState.errors.username && (
          <p className="text-xs text-error mt-1">
            {form.formState.errors.username.message}
          </p>
        )}
      </div>
      <div className="relative">
        <PasswordInput
          value={form.watch('password') || ''}
          onChange={(val) => form.setValue('password', val)}
          placeholder="Parola (min. 8 karakter)"
          disabled={isSubmitting}
          error={form.formState.errors.password ? true : false}
        />
        {form.formState.errors.password && (
          <p className="text-xs text-error mt-1">
            {form.formState.errors.password.message}
          </p>
        )}
        <div className="absolute bottom-0 left-0 w-full text-xs text-text-secondary">
          En az 8 karakter
        </div>
      </div>
      <Button
        type="submit"
        variant="accent"
        className="w-full"
        disabled={isSubmitting}
      >
        {isSubmitting ? 'Hesap Oluşturuluyor...' : 'Hesap Oluştur'}
      </Button>
      <p className="text-sm text-text-secondary text-center">
        Zaten hesabınız var mı?{' '}
        <Link
          to="/login"
          className="font-medium text-primary hover:text-primary-hover"
        >
          Giriş yapın
        </Link>
      </p>
    </form>
  )
}
