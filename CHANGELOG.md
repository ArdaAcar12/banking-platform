# Changelog

Bu dosya proje üzerindeki tüm önemli değişiklikleri kaydeder.

Biçim: [Keep a Changelog](https://keepachangelog.com/tr/1.0.0/) uyarlanması.

## [0.1.0] - 2026-11-11 — Phase 0: Altyapı Hazırlığı

### Eklenenler

- Spring Boot 4.1.2 + Java 21 backend projesi (`start.spring.io` şablonu)
  - Spring Web MVC, Spring Data JPA, Hibernate, Flyway
  - Spring Security, Validation
  - Spring AMQP (RabbitMQ)
  - Spring Boot Actuator, Mail, DevTools
  - PostgreSQL (runtime), H2 (testler)
  - Lombok
- Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`)
- `frontend/`: Vue 3 + TypeScript + Vite projesi
- `docker-compose.yml`: PostgreSQL, RabbitMQ Management, Redis, MailHog, Prometheus, Grafana
- `.env.example`: Ortam değişkenleri örneği (gizli bilgi yok)
- `.gitignore`: Maven, IDE (IntelliJ/STS/NetBeans/VS Code), frontend, gizli dosyalar, build çıktıları
- `README.md`: Kapsamlı proje dokümantasyonu
- `DEVELOPMENT_STATUS.md`: Geliştirme durumu raporu
- `CHANGELOG.md`: Bu dosya

### Değiştirilenler

- `pom.xml`: Test kapsamında `com.h2database:h2` eklendi (varsayılan test veritabanı için)
- `.gitignore`: Frontend özel girişleri eklendi (`node_modules/`, `dist/`, `.env*`)

### Notlar

- Spring Boot 4.1.2, `start.spring.io` tarafından varsayılan stabil sürüm olarak belirtilmiştir; sürüm sabitleme yapılmamıştır (`spring-boot-starter-parent` üzerinden yönetilir).
- Backend derleme ve testleri Maven Wrapper ile başarıyla geçmiştir (`mvnw.cmd test` → BUILD SUCCESS).
- Frontend production build başarılıdır (`npm run build`).
- Docker henüz ortamda kurulu olmadığı için `docker compose config` doğrulaması ertelendi.

### Güvenlik

- Gerçek şifre veya gizli bilgi commit edilmemiştir.
- `.env` dosyası `.gitignore` ile dışlanmıştır.
