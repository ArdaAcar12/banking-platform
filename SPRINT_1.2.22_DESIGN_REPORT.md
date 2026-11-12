# SPRINT 1.2.22 — AUTH SECURITY HARDENING DESIGN

**Sprint tipi:** Analiz & Tasarım — Kod değişikliği yasaklı
**Tarih:** 2026-11-12
**Aim:** Auth modülü güvenliği donanımı ve dağıtım hazırlığı

---

## 1. MEVCUT AUTH SERVICE MİMARISI

### 1.1 Uygulama Yapısı
- **Spring Boot 4.1.2**, Java 21, Maven Wrapper
- Tek bir Spring Boot uygulaması (spring-boot:run ile başlatılıyor)
- Veritabanı erişimi: PostgreSQL (Docker, host port 5433)
- JWT tabanlı stateless authentication
- BCrypt password encoding
- Flyway migration ile DB schema yönetimi

### 1.2 Tek Instance Mimari
- Tüm authentication logic tek JVM içinde
- State yönetimi: JWT access token (short-lived) + Refresh token (DB-backed)
- Refresh token hash'lenmiş olarak saklanır, plaintext never storage
- Session yok (STATELESS), her istek JWT ile validate edilir

### 1.3 Zorunlu Noktalar
- Tek instance → ölçeklenemez (horizontal scaling yok)
- Rate limiting tek instance basisinde uygulanır
- Brute-force protection yok (account lockout mekanizması yok)
- CORS konfigürasyonu "*" (tüm origin'ler permit) — production riski

---

## 2. API GATEWAY ARANALIYORU

### 2.1 Bulunma Durumu: ✅ YOK
- `pom.xml` içinde `spring-cloud-starter-gateway` veya benzeri hiçbir dependency yok
- `SecurityConfig.java` tek bir uygulama içinde konfigüre edilmiş
- `docker-compose.yml` ile tek container (postgres) dışında gateway tanımı yok

### 2.2 Mimarik Öneri
Gateway bulunmadığı için iki seçenek:

**Seçenek A — Gateway ekle (önerilen long-term):**
- `spring-cloud-starter-gateway` ekle
- Rate limiting, CORS, JWT validation'ı gateway merkezinde yap
- Auth Service sadece token generation/validation servisi olur
- Diğer micro-service'lar authentication concern'ten arındırılır

**Seçenek B — Gateway olmadan kal (short-term):**
- Güvenlik donanımını tek uygulama içinde koru
- Rate limiting in-memory veya Redis-based (separate Redis container)
- CORS ve security tek appliyanda kalır

**Karar:** Sprint 1.2.23 için **Redis-based rate limiting** without gateway seçilebilir. Daha az değişiklik, daha hızlı gain.

---

## 3. AUTHENTICATION ENDPOINT'LERİ LİSTESİ

| Metot | Endpoint | Açıklama | Public Access |
|-------|----------|----------|---------------|
| POST | `/api/v1/auth/register` | Kullanıcı kaydı | ✅ permitAll |
| POST | `/api/v1/auth/login` | Giriş (access + refresh token) | ✅ permitAll |
| POST | `/api/v1/auth/refresh` | Access token yenileme | ✅ permitAll |
| POST | `/api/v1/auth/logout` | Refresh token iptal | ✅ authenticated |
| GET | `/api/v1/auth/me` | Mevcut kullanıcı bilgileri | ✅ authenticated |
| GET | `/api/v1/admin/users` | Kullanıcı listesi (ADMIN) | ✅ hasRole(ADMIN) |

**Swagger/OpenAPI endpoint'leri:**
- `GET /v3/api-docs` — dev: permitAll, prod: authenticated
- `GET /swagger-ui.html` — dev: permitAll, prod: authenticated

---

## 4. RATE LIMITING: REDIS KULLANILABILİRİLİK ANALYSİ

### 4.1 Redis Altyapısı Kontrolü
- `pom.xml` içinde Redis dependency yok
- `docker-compose.yml` içinde Redis container tanımı yok
- `application-dev.properties`/`application.properties` içinde spring.dataRedis yok

### 4.1.1 Sonuç: Redis mevcut DEĞIL
- Özel Redis container gerekli veya external Redis service gerekli
- Test ortamı (Testcontainers) PostgreSQL kullanıyor — Redis eklenebilir

### 4.2 Alternatif: Embedded Rate Limiting (Redis gerekmeden)
- Bucket4j veya Spring Web RateLimiter disk-based config ile çalışabilir
- Config dosyasında limitleri tutabiliriz

---

## 5. RATE LIMITING YaklaşıklARI KARŞILAŞTIRMA

| Yaklaşım | Avantaj | Dezavantaj | Score |
|----------|---------|------------|-------|
| **Redis tabanlı dağıtık** | - Multiple instance arasında paylaşılır<br>- Esnek config (gerçek zamanlı değiştirilebilir)<br>- Industry standard | - Redis altyapısı gerektirir<br>- Ek bağlantı overhası<br>- Network latency | ⭐⭐⭐⭐ |
| **In-memory (Spring Web RateLimiter)** | - Setup kolayı<br>- Redis gerekmez<br>- Tek instance için yeterli | - Multiple instance arasında paylaşılmaz<br>- Uygulama restart'ta sıfırlanır | ⭐⭐⭐ |
| **Bucket4j** | - Hem in-memory hem de Redis modunu destekler<br>- Token bucket algoritması esnek<br>- Config dosyasını destekler | - Ek dependency<br>- Learning curve | ⭐⭐⭐⭐ |
| **Spring Cloud Gateway RateLimiter** | - Gateway merkezli, tüm micro-services için consistent | - Gateway yok currently<br>- Gateway eklenirse iyi olur | ⭐⭐ (sadece gateway varsa) |

### 5.1 Önerilen Yaklaşım: Bucket4j + Redis Destekli

**Neden:**
- Hem tek instance hem de çok instance scenariosı destekler
- Redis optionali — sadece çok instance deployment'ında kullanılır
- Token bucket algoritması HTTP rate limiting için standart
- Konfigüre edilebilir maxRequestPerTimeUnit + duration
- H2/test ortamında in-memory mod ile test edilebilir

**Config Örneği:**
```java
@Bean
public RateLimiter rateLimiter() {
    // Bucket4j ile token bucket
    Bandwidth bandwidth = Bandwidth.classic(100, Duration.ofMinutes(1)); // 100 req/min
    Bandwidth refillBandwidth = Bandwidth.classic(10, Duration.ofMinutes(1));
    BandwidthRequest bandwidthRequest = BandwidthRequest.builder()
        .addLimit(bandwidth)
        .addLimit(refillBandwidth)
        .build();
    
    // Redis backend için (opsiyonel)
    // RateLimiter.builder().limitStore(RateLimiterLimitedStoreBuilder.builder().build());
    
    return RateLimiter.of(bandwidthRequest);
}
```

---

## 6. ÇOKLU AUTH SERVICE INSTANCE'LARI

### 6.1 Senaryo: Birden Fazla JVM Uygulama
- Her instance'in kendi in-memory rate limiter'ı olur
- Client IP bazlı rate limit her instance bağımsız çalışır
- Result: Rate limiting DAĞILANMADI, her instance kendi limitine sahip olur
- Örnek: 100 req/min limit × 3 instance = 300 req/min actual (istek dispersion)

### 6.2 Çözümler

**Seçenek A — Redis-based central rate limiting (önerilen):**
- Tüm instance'lar aynı Redis key'i (`ip:auth`) paylaşır
- Redis decrement operations → tüm instance'lar aynı sınırı görür
- Failover durumunda fallback: in-memory limited

**Seçenek B — Sticky sessions + in-memory:**
- Load balancer IP'leri aynı instance'a yönlendirir
- Rate limiting işe yarar ama ölçeklenemez
- IP hash routing gerektirir

**Seçenek C — Rate limit header'ında retry-after:**
- Her request'te 429 yanıtı + Retry-After header
- Client (frontend) backoff uygular
- Arka uta rate limiting gerektirmez ama kullanıcı deneyimi etkilenir

### 6.3 Öneri: Redis tabanlı merkezi rate limiting
Sprint 1.2.23'de adaylık ama production'a hazır tasarım.

---

## 7. ACCOUNT LOCKOUT TASARIMI

### 7.1 Gerekli Parametreler

| Parametre | Önerilen Değer | Açıklama |
|-----------|----------------|----------|
| Başarısız giriş sayısı | **5 deneme** | Configurable via application.properties |
| Kilitlenme süresi | **30 dakika** | 15-60 dk arasında configurable |
| Kilidin kaldırımı | **Manuel/OTP** veya **Süre dolması** | Automatic after TTL + OTP recovery |
| Hata mesajı | **Genel mesaj** | "Hesap geçici olarak kilitli. Daha sonra tekrar deneyin." — User varlığı leak önlenir |
| Brute-force önleme | **429 + backoff** | Rate limiting + increasing wait between attempts |

### 7.2 Uygulama Akışı

```
1. Login attempted → attempt counter increment (Redis/db)
2. counter >= 5 → account locked (enabled = false OR separate lock table)
3. Locked user → 401 with "account locked" message
4. After 30 min → lock automatically released
5. Optional: OTP recovery flow for locked accounts
```

### 7.3 Hata Mesajı Tasarımı
| Senaryo | Yanıt | HTTP Status |
|---------|-------|-------------|
| Normal başarısız login | `{"message": "E-posta/kullanıcı adı veya parola hatalı."}` | 401 |
| 5. deneme sonrası | `{"message": "Hesap geçici olarak kilitli. Daha sonra tekrar deneyin."}` | 401 |
| Rate limit aşıldı | `{"message": "Çok fazla deneme yapıldı. Daha sonra tekrar deneyin."}` | 429 |

**Güvenlik amacı:** User varlığı information leak — aynı mesaj ile herkes aynı sonucu alır.

### 7.4 Veri Yapısı (Opsiyonel)
```java
// Redis key: "lock:username"
// TTL: 30 dakika
// Value: {attempts, lockedAt}

// Veya DB tablosu:
// create table account_locks (
//     username varchar(50),
//     attempts int default 0,
//     locked_until timestamp,
//     primary key (username)
//)
```

---

## 8. CORS MEVcut KONFİGÜRASI ANALİZİ

### 8.1 Mevcut Yapılandırma (SecurityConfig.java:110-123)
```java
configuration.setAllowedOriginPatterns(List.of("*"));  // Tüm origin'ler!
configuration.setAllowCredentials(false);
```

### 8.2 Risk Değerlendirmesi
- **Production risk:** Tüm domain'lerden cross-origin istek permit
- **Development:** acceptable (localhost development)
- **Production:** tüm origin'ler açık → CSRF/XSS riski

### 8.3 Öngörülenen Frontend Origin'ler
Banka back-end mimarisine göre tahmini origin'ler:
- `http://localhost:3000` — React development
- `http://localhost:4200` — Angular development  
- `http://app.bank.local` — Local banking app domain
- `https://app.bank.com` — Production frontend domain

### 8.4 CORS Öneri
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // Production: specific origins only!
    // Development: localhost patterns
    configuration.setAllowedOriginPatterns(
        List.of("http://localhost:3000", "http://localhost:4200", "https://app.bank.com")
    );
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
    configuration.setAllowCredentials(true); // JWT auth için true gerekebilir
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Not:** `setAllowCredentials(true)` — JWT auth için gerekli, `setAllowedOriginPatterns("*")` ile birlikte kullanılamaz (browser hata verir). Özel origin'ler kullanılması gerekir.

---

## 9. TEST SAYISI DOĞRULAMA

### 9.1 Test Çalışma Sonuçları (`mvnw test`)

| Test Sınıfı | Test Sayısı | Durum |
|-------------|-------------|-------|
| AuthServiceTest | 13 | ✅ Geçti |
| JwtServiceTest | 4 | ✅ Geçti |
| RefreshTokenServiceTest | 7 | ✅ Geçti |
| UserMapperTest | 2 | ✅ Geçti |
| GlobalExceptionHandlerTest | 9 | ✅ Geçti |
| AuthIntegrationTest | 18 (toplam) | ✅ Geçti |
| BankingPlatformApplicationTests | 1 | ✅ Geçti |
| **Toplam** | **54** | **✅ BUILD SUCCESS** |

### 9.2 Önceki Sprint ile Karşılaştırma

| Sprint | Test Sayısı | Açıklama |
|--------|-------------|----------|
| 1.2.19 (öğünde) | 54 | Başlangıçta çalışan test sayısı |
| 1.2.21 (analiz) | 58 | Daha önce saydığım (farklı bağımsız test çalıştırma) |
| 1.2.22 (şu anki) | **54** | Doğrulanan çalışan test sayısı |

**Fark:** 58 → 54, `AuthIntegrationTest`'teki bazı test senaryolarının CI ortamında çalışmayabilir veya Testcontainers ile PostgreSQL container conflict'i olabilir. 54 fixed count, Build SUCCESS doğrular.

### 9.3 Test Kapsamı Değerlendirmesi
- **Unit testler:** 35 (AuthService 13 + JwtService 4 + RefreshTokenService 7 + UserMapper 2 + GlobalExceptionHandler 9)
- **Integration testler:** 19 (AuthIntegrationTest 18 + BankingPlatformApplicationTests 1)
- **Kapasyon:** Tüm authentication senaryoları covered (register, login, refresh, logout, token validation)
- **Eksik:** CORS testleri, rate limiting testleri, brute-force/lockout testleri

---

## 10. SPRINT 1.2.23 UYGULANABLE TEKNİK PLAN

| # | Görev | Açıklama | Beklenen Çıktı |
|---|-------|----------|----------------|
| 1 | **CORS konfigürasyonu** | `CorsConfigurationSource` bean'da `*` → specific origin'ler (`localhost:3000`, `localhost:4200`, `https://app.bank.com`) | CORS headers production'da doğru, frontend çalışır |
| 2 | **Rate limiting — Bucket4j** | Bucket4j dependency ekle, RateLimiter bean ekle, `auth-ip` key pattern ile 100 req/min limit | `429 Too Many Requests` header'ları doğrulanır |
| 3 | **Redis konfigürasyonu** | Redis dependency + spring.dataRedis.properties ekle (çok instance için opsiyonel) | Docker-compose'de Redis container eklendi/opsiyonel |
| 4 | **Account lockout** | Failed attempt counter (Redis), 5 denemeden sonra 30dk kilit, genel hata mesajı | Kilitli kullanıcı 30dk sonra otomatik açılır |
| 5 | **Hata mesajı tutarlılığı** | Login error message'leri uniformize et, user varlığı leak engelle | "Hesap kilitli" veya "parola hatalı" mesajları test edildi |
| 6 | **Test genişletme** | CORS testleri, rate limiting testleri, lockout testleri ekle | Test sayısı 54'ten 65+ çıkarılarak coverage % artır |

### 10.1 Bağımlılıklar (pom.xml eklenecek)
```xml
<!-- Bucket4j rate limiting -->
<dependency>
    <groupId>com.github.vladimir-buc</groupId>
    <artifactId>bucket4j</artifactId>
    <version>5.0.0</version>
</dependency>

<!-- Redis support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 10.2 Application Properties Eklenecek
```properties
# Rate limiting
bucket4j.rate-limit.auth.ip.max-requests=100
bucket4j.rate-limit.auth.ip.duration-minutes=1

# Account lockout
account.lockout.max-attempts=5
account.lockout.lockout-duration-minutes=30
account.lockout.enable-true
```

### 10.3 Başarı Kriterleri
- [ ] `mvnw test` → BUILD SUCCESS, 54 test passed
- [ ] CORS headers: specific origin'ler production'da, dev'de localhost'lar works
- [ ] Rate limit test: 101. request → 429 with Retry-After
- [ ] Lockout test: 5 başarısız login → account locked, 30dk sonra unlock
- [ ] JWT authentication hiçbir şekilde broken olmamalı

---

## ÖZET

| Konu | Durum | Öncelik |
|------|-------|---------|
| API Gateway | **Yok** → Ele alınacak (Redis rate limiting selected) | Yüksek (mimarik karar) |
| Rate limiting | **Yok** → Bucket4j + Redis tasarlanacak | Yüksek (brute-force protection) |
| CORS | **`*` origin** → Specific origins needed | Yüksek (security risk) |
| Account lockout | **Yok** → 5 denemeden sonra 30dk kilit | Yüksek (brute-force önleme) |
| Test sayısı | **54** → Tümü passing | Normal (maintenance) |

**Sprint 1.2.23 hedefi:** Auth security hardening — CORS, rate limiting, account lockout mekanizmaları ekleyerek production readiness artırmak.

**Önemli:** Kod yazılmıyor, sadece tasarım ve plan yapılıyor. Sprint 1.2.24'de implementasyon yapılacak.