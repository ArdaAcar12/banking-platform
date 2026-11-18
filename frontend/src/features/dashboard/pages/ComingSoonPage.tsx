import { ShieldCheck } from 'lucide-react'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/Card'
import { CardDescription } from '@/components/ui/Card'
import { CardHeader } from '@/components/ui/Card'
import { CardTitle } from '@/components/ui/Card'
import { Alert } from '@/components/ui/Alert'

interface ComingSoonPageProps {
  moduleName: string
  description?: string
}

export function ComingSoonPage({ moduleName, description }: ComingSoonPageProps) {
  return (
    <div className="max-w-2xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-text-primary">{moduleName}</h1>
        <p className="text-text-secondary mt-1">
          {description ?? 'Bu modul icin gercek API entegrasyonu yakinda eklenecektir.'}
        </p>
      </div>

      <Alert variant="info">
        Bu sayfa guvenlik acisindan korunmustur. Giris yapmis bir kullanici olarak
        erisebilirsiniz, ancak islevsellik henuz gelistirilmediginden demo olarak
        goruntulenmektedir.
      </Alert>

      <Card>
        <CardHeader>
          <CardTitle>Modul Bilgileri</CardTitle>
          <CardDescription>
            {moduleName} modulu icin planlanan ozellikler ve gelistirme durumu.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center gap-3">
            <ShieldCheck className="h-5 w-5 text-accent" />
            <span className="text-sm text-text-secondary">
              Kimlik dogrulama gereklidir
            </span>
          </div>
          <div className="flex items-center gap-3">
            <span className="h-5 w-5 rounded-full bg-warning/10 text-warning flex items-center justify-center">
              <span className="text-[10px] font-bold">DEMO</span>
            </span>
            <span className="text-sm text-text-secondary">
              Demo modu - gercek veri yok
            </span>
          </div>
          <div className="flex items-center gap-3">
            <span className="h-5 w-5 rounded-full bg-text-secondary/10 flex items-center justify-center text-text-secondary">
              <span className="text-[10px] font-bold">!</span>
            </span>
            <span className="text-sm text-text-secondary">
              Gercek API entegrasyonu bekleniyor
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
