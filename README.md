# Banking Platform

Banking platform altyapı projesi (Phase 0 — Altyapı Hazırlığı).

Bu proje, **IntelliJ IDEA Free (Community Edition)** ile geliştirilmeye uygun şekilde
Standart Java, Maven ve Spring Boot özellikleri kullanılarak oluşturulmuştur.
Ultimate sürümü veya ücretli eklentiler gerektirmez.

## Klasör Yapısı

```
banking-platform/
├── pom.xml                    # Backend Maven yapılandırması (Spring Boot 4.1.2, Java 21)
├── mvnw / mvnw.cmd            # Maven Wrapper
├── .mvn/wrapper/              # Maven Wrapper ayarları
├── src/                       # Backend kaynak kodu (Spring Boot)
├── frontend/                  # Vue 3 + TypeScript + Vite (Vite production build)
├── docker-compose.yml         # Altyapı hizmetleri (PostgreSQL, RabbitMQ, Redis, MailHog, Prometheus, Grafana)
├── .env.example               # Ortam değişkenleri örneği (gerçek değerler buraya yazılmaz)
├── .gitignore                 # Sürüm kontrolünden dışlanacak dosyalar
├── README.md                  # Bu dosya
├── DEVELOPMENT_STATUS.md      # Geliştirme durumu raporu
├── CHANGELOG.md               # Değişiklık günlüğü
├── HELP.md                    # Spring Initializr tarafından oluşturulan yardımcı dosya
└── target/                    # Derleme çıktısı (git dışı)
```

## Teknoloji Yığını

### Backend
- **Java 21** (LTS, Eclipse Adoptium/Temurin)
- **Spring Boot 4.1.2** (stabil, `start.spring.io` default sürümü)
- **Apache Maven** (Maven Wrapper, `mvnw` / `mvnw.cmd`; kurulu Maven gerektirmez)
- Spring Web MVC, Spring Data JPA, Hibernate, Flyway
- Spring Security, Validation
- Spring AMQP (RabbitMQ)
- Spring Boot Actuator, Mail, DevTools
- Lombok
- **PostgreSQL** (runtime), **H2** (testler)

### Frontend
- **Vue 3**
- **TypeScript**
- **Vite** (production build)

### Altyapı (Docker Compose)
- PostgreSQL 16
- RabbitMQ 4 (Management)
- Redis 7
- MailHog
- Prometheus
- Grafana

## Kurulum ve Çalıştırma

### Önkoşullar
- Java 21 JDK
- Node.js 20+ ve npm
- Docker Desktop (altyapı hizmetleri için)
- IntelliJ IDEA Free (önerilir, zorunlu değil)

### 1. Altyapı Hizmetlerini Başlatma (Docker Compose)

Önce `.env.example` dosyasının kopyasını alıp `.env` olarak kaydedin ve gerekli değerleri doldurun:

```cmd
copy .env.example .env
```

Docker Compose ile hizmetleri başlatın:

```cmd
docker compose up -d
```

Hizmet portları:

| Hizmet | Port | Açıklama |
|--------|------|----------|
| PostgreSQL | 5432 | Veritabanı |
| RabbitMQ (AMQP) | 5672 | Mesaj broker |
| RabbitMQ (Management) | 15672 | Yönetim arayüzü |
| Redis | 6379 | Önbellek |
| MailHog (SMTP) | 1025 | E-posta test sunucusu |
| MailHog (UI) | 8025 | E-posta web arayüzü |
| Prometheus | 9090 | Metrik toplama |
| Grafana | 3000 | Görselleştirme |

Yapılandırmayı doğrulamak için:

```cmd
docker compose config
```

### 2. Backend (Spring Boot)

Derleme:

```cmd
mvnw.cmd compile
```

Testler:

```cmd
mvnw.cmd test
```

Uygulamayı çalıştırma:

```cmd
mvnw.cmd spring-boot:run
```

Tüm komutlar IntelliJ IDEA Free terminalinde çalıştırılabilir.

### 3. Frontend (Vue 3 + Vite)

Bağımlılıkları yükleme:

```cmd
cd frontend
npm install
```

Geliştirme sunucusu:

```cmd
npm run dev
```

Production build:

```cmd
npm run build
```

Build çıktısı `frontend/dist/` klasörüne çıkar.

## Yapılandırma

Backend yapılandırması `src/main/resources/application.properties` dosyasındadır.
Ortama özgü gizli bilgiler (veritabanı şifreleri, e-posta kimlik bilgileri vb.)
`.env` dosyası veya ortam değişkenleri üzerinden sağlanmalı ve **asla commit edilmemelidir**.
Örnek değerler `.env.example` dosyasındadır. `.env` dosyası `.gitignore` ile dışlanmıştır.

## IDE Uyumluluğu

Proje standart bir Maven projesidir ve **IntelliJ IDEA Free** ile açıp geliştirilebilir.
Herhangi bir Ultimate-only özellik veya ücretli eklenti gerektirmez.
Ultimate'a özgü işlemler için terminal komutları kullanılmalıdır.

## Sürüm Kontrolü

- `main`: stabil ve doğrulanmış sürüm
- `develop`: enteilasyon dalı
- `feature/*`: bireysel özellikler
- `fix/*`: hata düzeltmeleri
- `docs/*`: dokümantasyon

Commitler [Conventional Commits](https://www.conventionalcommits.org/) standardına uygun olarak yazılır.

## Geliştirme Durumu

Sprint geçmişi ve mevcut durum için `DEVELOPMENT_STATUS.md` dosyasına bakınız.
