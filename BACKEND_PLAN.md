# mulakatim.com — Backend Plan

**Stack:** Java 17 · Spring Boot 3.x · Spring Security · Spring Data JPA · PostgreSQL · Redis · JWT + Google OAuth2

---

## 1. Tech Stack & Bağımlılıklar

```xml
<!-- pom.xml -->
spring-boot-starter-web
spring-boot-starter-security
spring-boot-starter-data-jpa
spring-boot-starter-validation
spring-boot-starter-data-redis
spring-security-oauth2-resource-server   <!-- JWT doğrulama -->
spring-security-oauth2-client            <!-- Google OAuth2 -->
jjwt-api / jjwt-impl / jjwt-jackson     <!-- JWT üretim -->
postgresql
flyway-core                              <!-- DB migration -->
bucket4j-core / bucket4j-redis           <!-- Rate limiting -->
lombok
mapstruct
```

**Database:** PostgreSQL 15+
**Cache:** Redis (soru oturumları, kategori listesi, rate limit sayaçları)
**Migration:** Flyway
**Deployment:** Docker + Docker Compose

---

## 2. Proje Yapısı

```
src/main/java/com/mulakatim/
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── RedisConfig.java
│   ├── CorsConfig.java
│   └── RateLimitConfig.java
├── domain/
│   ├── user/
│   │   ├── User.java
│   │   ├── UserRepository.java
│   │   ├── UserService.java
│   │   └── UserController.java          (/api/users)
│   ├── auth/
│   │   ├── AuthController.java          (/api/auth)
│   │   ├── AuthService.java
│   │   ├── JwtService.java
│   │   ├── GoogleOAuthService.java
│   │   └── dto/
│   │       ├── LoginRequest.java
│   │       ├── RegisterRequest.java
│   │       └── AuthResponse.java
│   ├── category/
│   │   ├── Category.java
│   │   ├── CategoryRepository.java
│   │   ├── CategoryService.java
│   │   └── CategoryController.java      (/api/categories)
│   ├── quiz/
│   │   ├── QuizSession.java
│   │   ├── QuizAnswer.java
│   │   ├── QuizQuestion.java            (session'a bağlı soru sırası)
│   │   ├── QuizRepository.java
│   │   ├── QuizService.java
│   │   └── QuizController.java          (/api/quiz)
│   ├── question/                        (sadece admin yönetimi için)
│   │   ├── Question.java
│   │   ├── QuestionOption.java
│   │   ├── QuestionRepository.java
│   │   ├── QuestionService.java
│   │   └── admin/
│   │       └── AdminQuestionController.java  (/api/admin/questions)
│   └── feedback/
│       ├── Feedback.java
│       ├── FeedbackRepository.java
│       ├── FeedbackService.java
│       └── FeedbackController.java      (/api/feedback)
├── shared/
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ApiException.java
│   │   └── ErrorResponse.java
│   ├── ratelimit/
│   │   └── RateLimitFilter.java
│   ├── dto/
│   │   └── PageResponse.java
│   └── enums/
│       ├── Difficulty.java              (JUNIOR, MID, SENIOR)
│       ├── Language.java               (TR, EN)
│       └── CategoryType.java           (POSITION, TECHNICAL, LANGUAGE)
└── MulakatimApplication.java
```

---

## 3. Veritabanı Şeması

### `users`
```sql
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),           -- null → Google ile kayıtlı
    google_id     VARCHAR(255) UNIQUE,
    role          VARCHAR(20) DEFAULT 'USER',  -- USER, ADMIN
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);
```

### `categories`
```sql
CREATE TABLE categories (
    id             VARCHAR(50) PRIMARY KEY,   -- 'frontend', 'backend', ...
    type           VARCHAR(20) NOT NULL,      -- POSITION, TECHNICAL, LANGUAGE
    slug_tr        VARCHAR(100) NOT NULL UNIQUE,
    slug_en        VARCHAR(100) NOT NULL UNIQUE,
    title_tr       VARCHAR(100) NOT NULL,
    title_en       VARCHAR(100) NOT NULL,
    description_tr TEXT,
    description_en TEXT,
    icon           VARCHAR(50),              -- lucide icon name
    sort_order     INT DEFAULT 0,
    active         BOOLEAN DEFAULT TRUE
);
```

### `questions`
```sql
CREATE TABLE questions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id   VARCHAR(50) REFERENCES categories(id),
    language      VARCHAR(5) NOT NULL,        -- 'tr', 'en'
    difficulty    VARCHAR(10) NOT NULL,       -- 'junior', 'mid', 'senior'
    question_text TEXT NOT NULL,
    explanation   TEXT,
    correct_index SMALLINT NOT NULL,         -- 0-3, ASLA frontend'e gönderilmez
    active        BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE question_options (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id  UUID REFERENCES questions(id) ON DELETE CASCADE,
    option_index SMALLINT NOT NULL,          -- 0, 1, 2, 3
    option_text  TEXT NOT NULL
);
```

### `quiz_sessions`
```sql
CREATE TABLE quiz_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id),  -- null → misafir (preview)
    category_id     VARCHAR(50) REFERENCES categories(id),
    language        VARCHAR(5) NOT NULL,
    difficulty      VARCHAR(10),               -- null → hepsi
    is_karma        BOOLEAN DEFAULT FALSE,
    is_guest        BOOLEAN DEFAULT FALSE,     -- login olmadan açılan preview oturumu
    current_index   SMALLINT DEFAULT 0,        -- kaçıncı soruda
    score           SMALLINT,
    total_questions SMALLINT NOT NULL,
    duration_sec    INT,
    status          VARCHAR(20) DEFAULT 'IN_PROGRESS',  -- IN_PROGRESS, COMPLETED, ABANDONED
    started_at      TIMESTAMP DEFAULT NOW(),
    completed_at    TIMESTAMP,
    expires_at      TIMESTAMP                  -- misafir oturumları 1 saat sonra silinir
);
```

### `quiz_session_questions`
```sql
-- Session'a ait soru sırası — shuffle edilmiş, sıra sabit
CREATE TABLE quiz_session_questions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id   UUID REFERENCES quiz_sessions(id) ON DELETE CASCADE,
    question_id  UUID REFERENCES questions(id),
    position     SMALLINT NOT NULL,           -- 0'dan başlar, sıra numarası
    UNIQUE (session_id, position)
);
```

### `quiz_answers`
```sql
CREATE TABLE quiz_answers (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id     UUID REFERENCES quiz_sessions(id) ON DELETE CASCADE,
    question_id    UUID REFERENCES questions(id),
    position       SMALLINT NOT NULL,
    selected_index SMALLINT NOT NULL,
    is_correct     BOOLEAN NOT NULL,
    answered_at    TIMESTAMP DEFAULT NOW()
);
```

### `feedback`
```sql
CREATE TABLE feedback (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES users(id),    -- null → misafirden gelirse
    type        VARCHAR(20) NOT NULL,         -- 'question', 'session', 'general'
    question_id UUID REFERENCES questions(id),
    session_id  UUID REFERENCES quiz_sessions(id),
    message     TEXT NOT NULL,
    status      VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, REVIEWED, RESOLVED
    created_at  TIMESTAMP DEFAULT NOW()
);
```

---

## 4. Soru Güvenliği & Session-Bound Delivery

### Temel Kural
```
Sorular toplu çekilemez. Her soru yalnızca aktif bir session üzerinden,
sırayla ve bir önceki cevap gönderildikten sonra alınabilir.
```

### Misafir (Login Olmadan) Akışı
```
POST /api/quiz/preview/start  →  session açılır (is_guest=true, expires_at=+1saat)
GET  /api/quiz/{sessionId}/current  →  1. soru gelir
POST /api/quiz/{sessionId}/answer   →  cevap gönderilir, 2. soru için kapı açılır
GET  /api/quiz/{sessionId}/current  →  2. soru gelir
POST /api/quiz/{sessionId}/answer   →  cevap gönderilir, 3. soru için kapı açılır
GET  /api/quiz/{sessionId}/current  →  3. soru gelir
POST /api/quiz/{sessionId}/answer   →  HTTP 401 döner → { "code": "LOGIN_REQUIRED" }
                                       (4. soruya geçmek için login gerekiyor)
```

### Giriş Yapılmış Kullanıcı Akışı
```
POST /api/quiz/start                →  session açılır (is_guest=false)
GET  /api/quiz/{sessionId}/current  →  mevcut soru gelir
POST /api/quiz/{sessionId}/answer   →  cevap kaydedilir, sonraki soruya geçilir
  ...tekrar...
POST /api/quiz/{sessionId}/answer   →  son cevap → status=COMPLETED → sonuç döner
```

### Neden Güvenli?
- Kullanıcı aynı anda yalnızca **bir** soruyu görebilir
- Sonraki soruya geçmek için mevcut sorunun cevabını **sunucuya göndermek** şart
- `correct_index` asla response'a dahil edilmez; yalnızca `answer` endpoint'inde DB'de kontrol edilir
- Session'a bağlı olmayan herhangi bir soru ID'si ile soru metni çekilemez

---

## 5. API Endpoints

### 5.1 Auth — `/api/auth`

| Method | Path | Ne Yapar | Auth |
|--------|------|----------|------|
| POST | `/api/auth/register` | Yeni kullanıcı kaydı oluşturur. Şifreyi bcrypt ile hashler, JWT döner. | — |
| POST | `/api/auth/login` | E-posta + şifre doğrular, JWT döner. Hatalı girişte 401. | — |
| POST | `/api/auth/google` | Google idToken'ı doğrular. Kullanıcı yoksa otomatik kayıt yapar, JWT döner. | — |
| POST | `/api/auth/refresh` | Refresh token geçerliyse yeni access token üretir. | refresh token |
| POST | `/api/auth/logout` | Refresh token'ı Redis'ten siler (geçersiz kılar). | Bearer |

**POST /api/auth/register**
```json
// Request
{ "name": "Ali Veli", "email": "ali@mail.com", "password": "Secret123" }

// Response 201
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "user": { "id": "uuid", "name": "Ali Veli", "email": "ali@mail.com" }
}

// Hata — e-posta zaten kayıtlı
// Response 409
{ "code": "EMAIL_ALREADY_EXISTS", "message": "Bu e-posta ile zaten bir hesap mevcut." }
```

**POST /api/auth/login**
```json
// Request
{ "email": "ali@mail.com", "password": "Secret123" }

// Response 200
{ "accessToken": "eyJ...", "refreshToken": "eyJ...", "user": { ... } }

// Hata — şifre yanlış
// Response 401
{ "code": "INVALID_CREDENTIALS", "message": "E-posta veya şifre hatalı." }
```

**POST /api/auth/google**
```json
// Request — Google Sign-In'den alınan idToken
{ "idToken": "eyJhbGciOiJSUzI1NiIs..." }

// Response 200 (giriş) veya 201 (yeni kayıt)
{ "accessToken": "eyJ...", "refreshToken": "eyJ...", "user": { ... }, "isNewUser": true }
```

**POST /api/auth/refresh**
```json
// Request
{ "refreshToken": "eyJ..." }

// Response 200
{ "accessToken": "eyJ..." }

// Hata — token süresi dolmuş veya geçersiz
// Response 401
{ "code": "REFRESH_TOKEN_EXPIRED" }
```

---

### 5.2 Kategoriler — `/api/categories`

| Method | Path | Ne Yapar | Auth |
|--------|------|----------|------|
| GET | `/api/categories` | Tüm aktif kategorileri döner. Her kategori için soru dağılımı (junior/mid/senior) dahildir. Redis'ten servis edilir. | — |
| GET | `/api/categories/{id}` | Tek kategori detayı. SEO sayfaları için kullanılır. | — |

**GET /api/categories?lang=tr**
```json
[
  {
    "id": "frontend",
    "type": "POSITION",
    "title": "Frontend Developer",
    "description": "React, JavaScript, CSS ve modern web geliştirme...",
    "icon": "Monitor",
    "questionCount": { "junior": 4, "mid": 3, "senior": 3, "total": 10 }
  }
]
```

---

### 5.3 Quiz — `/api/quiz`

Tüm endpoint'ler session-bound çalışır. Sorular toplu çekilemez.

| Method | Path | Ne Yapar | Auth |
|--------|------|----------|------|
| POST | `/api/quiz/preview/start` | Misafir session başlatır. Kategori + dil + zorluk alır, sorular shuffle edilip `quiz_session_questions`'a yazılır. Session `is_guest=true`, `expires_at=+1saat`. | — |
| POST | `/api/quiz/start` | Login kullanıcı için session başlatır. Aynı logic, `is_guest=false`. | Bearer |
| GET | `/api/quiz/{sessionId}/current` | Session'ın mevcut sorusunu döner (`current_index`). Soru metni ve seçenekler gelir, `correct_index` asla gelmez. Session süresi dolmuşsa 404. | — / Bearer |
| POST | `/api/quiz/{sessionId}/answer` | Mevcut sorunun cevabını kaydeder. Misafir ise ve `current_index >= PREVIEW_LIMIT (3)` ise `401 LOGIN_REQUIRED` döner. Doğruysa `current_index++`. Son soruysa `status=COMPLETED` ve sonuç döner. | — / Bearer |
| GET | `/api/quiz/{sessionId}/result` | Tamamlanmış session'ın sonucunu döner. Tüm sorular, kullanıcının cevapları, doğru cevaplar ve açıklamalar bu noktada açıklanır. | Bearer |
| GET | `/api/quiz/history` | Giriş yapmış kullanıcının geçmiş testlerini döner. Sayfalanmış. | Bearer |
| GET | `/api/quiz/history/{sessionId}` | Geçmiş bir testin detayını döner. Sonuçlar + soru bazlı cevap analizi. | Bearer |

**POST /api/quiz/preview/start** (misafir)
```json
// Request
{
  "categoryId": "frontend",
  "language": "tr",
  "difficulty": null       // null → hepsi, "junior" / "mid" / "senior"
}

// Response 201
{
  "sessionId": "uuid",
  "totalQuestions": 10,
  "previewLimit": 3,       // misafir bu kadar soru görebilir
  "startedAt": "2025-05-02T10:00:00Z",
  "expiresAt": "2025-05-02T11:00:00Z"
}
```

**POST /api/quiz/start** (login kullanıcı)
```json
// Request — normal kategori testi
{
  "categoryId": "frontend",
  "language": "tr",
  "difficulty": "junior",
  "isKarma": false
}

// Request — karma mülakat
{
  "categoryIds": ["frontend", "backend", "algorithms"],
  "language": "tr",
  "difficulty": null,
  "isKarma": true,
  "questionLimit": 20      // karma modda toplam soru sayısı
}

// Response 201
{
  "sessionId": "uuid",
  "totalQuestions": 10,
  "startedAt": "2025-05-02T10:00:00Z"
}
```

**GET /api/quiz/{sessionId}/current**
```json
// Response 200
{
  "sessionId": "uuid",
  "position": 0,            // 0-indexed, kaçıncı soru
  "totalQuestions": 10,
  "isLastQuestion": false,
  "isGuest": true,
  "remainingPreview": 2,    // misafir için kaç soru kaldı (null → login kullanıcı)
  "question": {
    "id": "uuid",
    "difficulty": "junior",
    "questionText": "React'ta useState hook'u ne işe yarar?",
    "options": [
      { "index": 0, "text": "State'i kalıcı olarak depolar." },
      { "index": 1, "text": "Fonksiyonel bileşende yerel state yönetir." },
      { "index": 2, "text": "Props'u yönetir." },
      { "index": 3, "text": "Side effect çalıştırır." }
    ]
    // correctIndex YOK
  }
}

// Hata — session bulunamadı veya süresi dolmuş
// Response 404
{ "code": "SESSION_NOT_FOUND" }

// Hata — session tamamlanmış
// Response 409
{ "code": "SESSION_ALREADY_COMPLETED" }
```

**POST /api/quiz/{sessionId}/answer**
```json
// Request
{
  "selectedIndex": 1,
  "durationSec": 28         // bu soruya harcanan süre (opsiyonel, analitik için)
}

// Response 200 — soru kaydedildi, sonraki soru var
{
  "accepted": true,
  "hasNext": true,
  "nextPosition": 1
}

// Response 200 — son soruydu, test tamamlandı
{
  "accepted": true,
  "hasNext": false,
  "completed": true,
  "sessionId": "uuid"       // /result endpoint'ine yönlendir
}

// Hata — misafir, preview limiti aşıldı
// Response 401
{
  "code": "LOGIN_REQUIRED",
  "message": "Teste devam etmek için giriş yapmanız gerekiyor.",
  "previewLimit": 3
}

// Hata — yanlış position (sıra dışı cevap)
// Response 409
{ "code": "WRONG_QUESTION_POSITION" }
```

**GET /api/quiz/{sessionId}/result**
```json
// Response 200 — doğru cevaplar artık açıklanır
{
  "sessionId": "uuid",
  "categoryTitle": "Frontend Developer",
  "language": "tr",
  "score": 7,
  "total": 10,
  "percentage": 70,
  "totalDurationSec": 312,
  "completedAt": "2025-05-02T10:05:12Z",
  "results": [
    {
      "position": 0,
      "questionText": "React'ta useState hook'u ne işe yarar?",
      "options": ["...", "...", "...", "..."],
      "selectedIndex": 1,
      "correctIndex": 1,          // artık açıklanabilir
      "isCorrect": true,
      "explanation": "useState, fonksiyonel bileşenlerde..."
    }
  ]
}
```

**GET /api/quiz/history?page=0&size=10**
```json
{
  "content": [
    {
      "sessionId": "uuid",
      "categoryId": "frontend",
      "categoryTitle": "Frontend Developer",
      "language": "tr",
      "difficulty": "junior",
      "isKarma": false,
      "score": 7,
      "total": 10,
      "percentage": 70,
      "durationSec": 312,
      "completedAt": "2025-05-02T10:05:12Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 23,
  "totalPages": 3
}
```

**GET /api/quiz/history/{sessionId}**
```json
// Geçmiş testin tüm detayı — /result ile aynı format
{
  "sessionId": "uuid",
  "score": 7,
  "total": 10,
  ...
  "results": [ ... ]
}
```

---

### 5.4 Feedback — `/api/feedback`

| Method | Path | Ne Yapar | Auth |
|--------|------|----------|------|
| POST | `/api/feedback` | Geri bildirim gönderir. `type` alanına göre soru hatası, test sonu değerlendirme veya genel görüş olabilir. | Bearer |
| GET | `/api/feedback` | Admin: tüm geri bildirimleri listeler, `status` ve `type`'a göre filtrelenebilir. | ADMIN |
| PATCH | `/api/feedback/{id}/status` | Admin: feedback durumunu günceller (PENDING → REVIEWED → RESOLVED). | ADMIN |

**POST /api/feedback**
```json
// Soru hatası bildirimi
{
  "type": "question",
  "questionId": "uuid",
  "message": "B seçeneği yanlış, doğrusu şöyle olmalı..."
}

// Test sonrası değerlendirme
{
  "type": "session",
  "sessionId": "uuid",
  "message": "Sorular çok zordu ama kaliteliydi."
}

// Genel görüş
{
  "type": "general",
  "message": "TypeScript kategorisi eklenebilir mi?"
}

// Response 201
{ "id": "uuid", "status": "PENDING" }
```

---

### 5.5 Kullanıcı — `/api/users`

| Method | Path | Ne Yapar | Auth |
|--------|------|----------|------|
| GET | `/api/users/me` | Giriş yapmış kullanıcının profil bilgilerini döner (id, name, email, created_at). | Bearer |
| PATCH | `/api/users/me` | İsim günceller. E-posta ve şifre değişikliği ayrı endpoint'lerle yapılabilir. | Bearer |
| DELETE | `/api/users/me` | Hesabı siler. Tüm session ve feedback kayıtları cascade silinir. | Bearer |

**GET /api/users/me**
```json
{
  "id": "uuid",
  "name": "Ali Veli",
  "email": "ali@mail.com",
  "createdAt": "2025-04-01T08:00:00Z",
  "stats": {
    "totalSessions": 23,
    "averageScore": 72,
    "bestCategory": "frontend"
  }
}
```

---

### 5.6 Admin — `/api/admin`

| Method | Path | Ne Yapar | Auth |
|--------|------|----------|------|
| GET | `/api/admin/questions` | Tüm soruları listeler (correct_index dahil). | ADMIN |
| POST | `/api/admin/questions` | Yeni soru ekler. | ADMIN |
| PUT | `/api/admin/questions/{id}` | Soru günceller. | ADMIN |
| DELETE | `/api/admin/questions/{id}` | Soruyu pasife çeker (`active=false`). | ADMIN |
| GET | `/api/admin/feedback` | Tüm feedback listesi. `?status=PENDING` ile filtrelenebilir. | ADMIN |
| PATCH | `/api/admin/feedback/{id}/status` | Feedback durumunu günceller. | ADMIN |
| GET | `/api/admin/stats` | Platform geneli istatistikler (aktif kullanıcı, toplam test, kategori dağılımı). | ADMIN |

---

## 6. Güvenlik Mimarisi

### JWT Flow
```
Login / Register / Google
  → AuthService
  → access_token  (15 dakika, her istekte header'da)
  → refresh_token (7 gün, Redis'te whitelist'te tutulur)

access_token süresi dolunca:
  → POST /api/auth/refresh { refreshToken }
  → yeni access_token döner

logout:
  → refresh_token Redis'ten silinir → geçersiz kalır
```

### Google OAuth2 Flow
```
1. Frontend: Google Sign-In butonu → Google idToken alınır
2. POST /api/auth/google { idToken }
3. Backend: GoogleIdTokenVerifier.verify(idToken)
   - Token geçersizse 401
   - Geçerliyse email + googleId alınır
4. DB'de bu email var mı?
   - Varsa: JWT üret, dön
   - Yoksa: user oluştur (password_hash=null), JWT üret, dön
```

### Spring Security Config
```java
http
  .authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers(GET, "/api/categories/**").permitAll()
    .requestMatchers(GET, "/api/quiz/*/current").permitAll()   // misafir soru okuyabilir
    .requestMatchers(POST, "/api/quiz/preview/start").permitAll()
    .requestMatchers(POST, "/api/quiz/*/answer").permitAll()   // 401 logic içeride
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
  )
  .oauth2ResourceServer(oauth2 -> oauth2.jwt(...))
  .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
  .csrf(AbstractHttpConfigurer::disable);
```

> **Not:** Misafir answer endpoint'i Spring Security seviyesinde engellenmez.
> İçeride `QuizService.answer()` metodu `session.isGuest() && position >= PREVIEW_LIMIT`
> kontrolü yapar ve `LoginRequiredException` fırlatır → 401 döner.

### Şifre & Veri Güvenliği
- BCrypt (strength 12)
- `correct_index` DB'de tutulur, hiçbir GET response'unda yer almaz
- Admin endpoint'leri `ROLE_ADMIN` ile korunur
- Misafir session'ları `expires_at` ile TTL'lanır, scheduled job ile temizlenir

---

## 7. Rate Limiting (Bucket4j + Redis)

| Endpoint | Limit | Pencere | Hedef |
|----------|-------|---------|-------|
| `POST /api/auth/register` | 3 istek | IP başına / saat | Bot kayıt önleme |
| `POST /api/auth/login` | 10 istek | IP başına / 15 dk | Brute force önleme |
| `POST /api/auth/google` | 10 istek | IP başına / 15 dk | Token replay önleme |
| `POST /api/quiz/preview/start` | 5 istek | IP başına / saat | Misafir scraping önleme |
| `POST /api/quiz/start` | 15 istek | Kullanıcı başına / gün | Toplu test açmayı önleme |
| `POST /api/quiz/*/answer` | 2 istek | Session + sn başına | Otomatik cevap önleme |
| `POST /api/feedback` | 10 istek | Kullanıcı başına / gün | Spam önleme |

Limit aşılınca → `HTTP 429 Too Many Requests` + `Retry-After` header

---

## 8. Redis Cache Stratejisi

| Key Pattern | TTL | İçerik |
|-------------|-----|--------|
| `categories:all:tr` | 1 saat | Tüm kategori listesi (TR) |
| `categories:all:en` | 1 saat | Tüm kategori listesi (EN) |
| `refresh_token:{userId}` | 7 gün | Refresh token whitelist |
| `rate:{ip}:{endpoint}` | pencere süresi | Bucket4j sayaçları |
| `guest_session:{sessionId}` | 1 saat | Misafir session metadata |

> Soru metinleri Redis'te tutulmaz. Session'a bağlı soru sırası DB'de `quiz_session_questions` tablosunda durur.

---

## 9. Flyway Migration Dosyaları

```
resources/db/migration/
├── V1__create_users.sql
├── V2__create_categories.sql
├── V3__create_questions.sql
├── V4__create_quiz_sessions.sql
├── V5__create_quiz_session_questions.sql
├── V6__create_quiz_answers.sql
├── V7__create_feedback.sql
├── V8__seed_categories.sql            -- kategori başlıkları + slug'lar
├── V9__seed_questions_tr.sql          -- mevcut TR soruların taşınması
└── V10__seed_questions_en.sql         -- mevcut EN soruların taşınması
```

---

## 10. Frontend Entegrasyon Değişiklikleri

| Mevcut (Frontend) | Yeni (Backend) |
|-------------------|----------------|
| `src/data/questions.ts` static import | Sorular artık static dosyadan okunmaz |
| `src/data/questions.en.ts` static import | Kaldırılır |
| Zustand quizStore'da tüm sorular | Store sadece `sessionId` + `currentQuestion` tutar |
| Sorular başlangıçta yüklenir | `GET /api/quiz/{sessionId}/current` ile teker teker çekilir |
| Cevap client'ta hesaplanır | `POST /api/quiz/{sessionId}/answer` → backend hesaplar |
| Mock authStore (localStorage) | JWT + `POST /api/auth/*` gerçek backend'e bağlanır |
| Sonuçlar Zustand store'da | `GET /api/quiz/{sessionId}/result` ile çekilir |
| Geçmiş test yok | `GET /api/quiz/history` → `/gecmis-testlerim` sayfası |
| Preview limiti frontend'de (PREVIEW_COUNT=2) | Backend `LOGIN_REQUIRED` 401 döner, frontend bu kodu yakalar |

### Axios Instance (Frontend)
```ts
// src/api/client.ts
const client = axios.create({ baseURL: '/api' });

// Request interceptor — her isteğe token ekle
client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response interceptor — 401 gelince token yenile
client.interceptors.response.use(null, async (error) => {
  if (error.response?.status === 401 && error.response?.data?.code !== 'LOGIN_REQUIRED') {
    const newToken = await refreshAccessToken();
    if (newToken) {
      error.config.headers.Authorization = `Bearer ${newToken}`;
      return client(error.config);
    }
    useAuthStore.getState().logout();
  }
  return Promise.reject(error);
});
```

---

## 11. Docker Compose

```yaml
services:
  app:
    build: .
    ports: ["8080:8080"]
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/mulakatim
      SPRING_DATA_REDIS_HOST: redis
      JWT_SECRET: ${JWT_SECRET}
      GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
    depends_on: [db, redis]

  db:
    image: postgres:15
    environment:
      POSTGRES_DB: mulakatim
      POSTGRES_USER: mulakatim
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes: ["pgdata:/var/lib/postgresql/data"]

  redis:
    image: redis:7-alpine

volumes:
  pgdata:
```

---

## 12. Geliştirme Fazları

### Faz 1 — Auth + Kategoriler
- [ ] User entity + BCrypt + JWT üretimi (`JwtService`)
- [ ] `POST /api/auth/register`, `/login`, `/refresh`, `/logout`
- [ ] Google idToken doğrulama (`GoogleIdTokenVerifier`)
- [ ] `POST /api/auth/google`
- [ ] Category entity + seed migration
- [ ] `GET /api/categories` (Redis cache dahil)

### Faz 2 — Soru Altyapısı
- [ ] Question + QuestionOption entity
- [ ] V9 + V10 migration — TR ve EN soruların import edilmesi
- [ ] `QuizSessionQuestion` ile shuffle mantığı
- [ ] Admin CRUD endpoint'leri (`/api/admin/questions`)

### Faz 3 — Quiz Session Flow
- [ ] `POST /api/quiz/preview/start` (misafir)
- [ ] `POST /api/quiz/start` (login)
- [ ] `GET /api/quiz/{sessionId}/current`
- [ ] `POST /api/quiz/{sessionId}/answer` (preview limit kontrolü dahil)
- [ ] `GET /api/quiz/{sessionId}/result`
- [ ] Karma mülakat desteği

### Faz 4 — Geçmiş & Feedback
- [ ] `GET /api/quiz/history` + `/{sessionId}`
- [ ] `POST /api/feedback`
- [ ] Admin feedback endpoint'leri
- [ ] Misafir session temizleme (scheduled job)

### Faz 5 — Rate Limiting & Güvenlik Sıkılaştırma
- [ ] Bucket4j + Redis entegrasyonu
- [ ] Tüm endpoint'lere rate limit uygulanması
- [ ] `GET /api/admin/stats`

### Faz 6 — Frontend Entegrasyonu
- [ ] Axios client + token refresh interceptor
- [ ] Static `questions.ts` → session-bound API akışına geçiş
- [ ] Auth store → gerçek JWT backend
- [ ] `/gecmis-testlerim` sayfası
- [ ] `LOGIN_REQUIRED` 401 yakalama → giriş modalı

---

## 13. Ortam Değişkenleri

```properties
# DB
spring.datasource.url=jdbc:postgresql://localhost:5432/mulakatim
spring.datasource.username=mulakatim
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true

# JWT
jwt.secret=${JWT_SECRET}                    # min 256-bit random string
jwt.access-token-expiry=900                 # 15 dakika (saniye)
jwt.refresh-token-expiry=604800             # 7 gün (saniye)

# Google OAuth2
google.client-id=${GOOGLE_CLIENT_ID}

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Quiz
quiz.preview-limit=3                        # login olmadan görülebilecek soru sayısı
quiz.guest-session-ttl=3600                 # misafir session TTL (saniye)

# CORS
cors.allowed-origins=https://mulakatim.com,http://localhost:5173
```
