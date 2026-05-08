# Architecture

## Style: Hexagonal (Ports & Adapters) — 10 Maven modules

```
core ← api ← use-case → spi → spi-adapter-{jpa,security,redis,kafka}
                    ↑               ↑
              api-adapter-rest   microservice (wires everything)
```

---

## Domain layer (`core`) — zero framework dependencies

### Models (`kz.dev.core.model`)
- `User` — aggregate root: UUID id, username, email, passwordHash, Set\<Role\> roles, boolean active, LocalDateTime createdAt
- `Token` — value object: value, userId, TokenType, issuedAt, expiresAt, revoked; `isExpired()`, `isRevoked()`, `isValid()`, `revoke()`
- `TokenPair` — record(Token accessToken, Token refreshToken)
- `Role` enum: USER, ADMIN, MODERATOR
- `TokenType` enum: ACCESS, REFRESH
- `OtpRequestedEvent(String email, String otp)` — published to Kafka

### Commands (`kz.dev.core.model`)
- `RegisterCommand(username, email, rawPassword, otpCode)`
- `AuthenticateCommand(email, rawPassword)`

### Exceptions (`kz.dev.core.exception`) — thrown from use-case, caught at REST adapter
- `UserAlreadyExistsException`
- `InvalidCredentialsException`
- `InvalidTokenException`
- `InvalidOtpException`

---

## In-ports (`api` module — `kz.dev.api.auth`)
| Interface | Method |
|---|---|
| `RequestOtpApi` | `requestOtp(String email)` |
| `RegisterUserApi` | `register(RegisterCommand) → User` |
| `AuthenticateUserApi` | `authenticate(AuthenticateCommand) → TokenPair` |
| `RefreshTokenApi` | `refresh(String refreshToken) → TokenPair` |
| `RevokeTokenApi` | `revoke()`, `revokeAll()` |

---

## Out-ports (`spi` module — `kz.dev.spi`)
| Interface | Sub-pkg | Impl class | Module |
|---|---|---|---|
| `UserRepository` | `persistence` | `UserRepositoryAdapter` | `spi-adapter-jpa` |
| `TokenRepository` | `persistence` | `TokenRepositoryAdapter` | `spi-adapter-jpa` |
| `PasswordEncoder` | `auth` | `BcryptPasswordEncoderAdapter` | `spi-adapter-security` |
| `TokenGenerator` | `auth` | `JwtTokenGeneratorAdapter` | `spi-adapter-security` |
| `ValidateTokenSpi` | `auth` | `AuthDomainService` | `use-case` |
| `OtpGenerator` | `otp` | `OtpDomainService` (TOTP/HmacSHA1) | `use-case` |
| `OtpStore` | `otp` | `RedisOtpAdapter` | `spi-adapter-redis` |
| `OtpVerifier` | `otp` | `RedisOtpAdapter` | `spi-adapter-redis` |
| `OtpSender` | `otp` | *(not wired)* | — |
| `SendEventSpi` | `event` | `KafkaEventAdapter` | `spi-adapter-kafka` |
| `SecurityContextPort` | `security` | `SpringSecurityContextAdapter` | `spi-adapter-security` |

---

## Use-case layer (`use-case` — `kz.dev.usecase.auth`)

### `OtpDomainService` implements `RequestOtpApi`, `OtpGenerator`
- `requestOtp(email)` → check email free → generate TOTP → store in Redis (5 min TTL) → publish `OtpRequestedEvent` to Kafka
- `generate()` → TOTP: HmacSHA1, 6 digits, 30-second step, secret from `app.otp.secret-key`

### `AuthDomainService` implements `RegisterUserApi`, `AuthenticateUserApi`, `RefreshTokenApi`, `ValidateTokenSpi`, `RevokeTokenApi`
- `register()` → `OtpVerifier.verify()` → check uniqueness → encode password → save User
- `authenticate()` → load user → verify password → `TokenGenerator.generatePair()` → save both tokens
- `refresh()` → validate refresh token → revoke old → generate new pair
- `revoke()` / `revokeAll()` → mark tokens revoked via `SecurityContextPort`

Both services: no Spring/JPA annotations; constructor injection; `@Transactional` from Spring is acceptable.

---

## Adapters

### `spi-adapter-security` (`kz.dev.spi.adapter.security`)
| Class | Role |
|---|---|
| `JwtTokenGeneratorAdapter` | `TokenGenerator` — JJWT 0.12.6, HMAC-SHA256, reads `JwtProperties` |
| `BcryptPasswordEncoderAdapter` | `PasswordEncoder` — delegates to Spring BCrypt |
| `JwtAuthenticationFilter` | `OncePerRequestFilter` — validates Bearer token, sets `UserPrincipal` in `SecurityContextHolder` |
| `SpringSecurityContextAdapter` | `SecurityContextPort` — reads `SecurityContextHolder` |
| `UserPrincipal` | `UserDetails` impl — wraps domain `User` |
| `UserDetailsServiceAdapter` | `UserDetailsService` — loads via `UserRepository` |
| `JwtProperties` | `record(secret, accessTtlSeconds, refreshTtlSeconds)` — wired manually in `AppConfig` |

### `spi-adapter-jpa` (`kz.dev.spi.adapter.jpa`)
| Class | Role |
|---|---|
| `UserRepositoryAdapter` | `UserRepository` impl |
| `TokenRepositoryAdapter` | `TokenRepository` impl — upsert on `value`, patch `revoked` |
| `UserJpaEntity` / `TokenJpaEntity` | JPA entities — never leave this module |
| `UserPersistenceMapper` / `TokenPersistenceMapper` | MapStruct mappers (entity ↔ domain) |
| `UserJpaRepository` / `TokenJpaRepository` | Spring Data JPA interfaces |

### `spi-adapter-redis` (`kz.dev.spi.adapter.redis`)
| Class | Role |
|---|---|
| `RedisOtpAdapter` | `OtpStore` + `OtpVerifier`; key pattern `otp:{email}`; TTL set on save |

### `spi-adapter-kafka` (`kz.dev.spi.adapter.kafka`)
| Class | Role |
|---|---|
| `KafkaEventAdapter` | `SendEventSpi`; serializes payload to JSON via Jackson, sends to given topic |

### `api-adapter-rest` (`kz.dev.api.adapter.rest`)
| Class | Role |
|---|---|
| `AuthController` | `@RestController` implementing `AuthClient`; injects in-ports only |
| `AuthClient` | `@HttpExchange` interface — server contract + HTTP client proxy |
| `GlobalExceptionHandler` | `@RestControllerAdvice` — domain exceptions → RFC 9457 `ProblemDetail` |
| DTOs | `SendOtpRequest`, `RegisterRequest`, `LoginRequest`, `RefreshRequest`, `TokenPairResponse`, `UserResponse` |

---

## Entry point (`microservice`)
| Class | Role |
|---|---|
| `MicroserviceApplication` | `@SpringBootApplication(scanBasePackages="kz.dev")` |
| `AppConfig` | All manual `@Bean` wiring — domain services, adapters, Kafka topic, Redis template |
| `SecurityConfig` | Stateless, CSRF off; public: `/auth/**`, Swagger paths |
| `OpenApiConfig` | `bearerAuth` HTTP security scheme for Swagger UI |

---

## Endpoints
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/auth/otp/send` | public | `{email}` | 204 |
| POST | `/auth/register` | public | `{username,email,password,otpCode}` | 201 UserResponse |
| POST | `/auth/login` | public | `{email,password}` | 200 TokenPairResponse |
| POST | `/auth/refresh` | public | `{refreshToken}` | 200 TokenPairResponse |
| POST | `/auth/logout` | Bearer | — | 204 |

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Flows

### OTP + Registration
```
POST /auth/otp/send {email}
  OtpDomainService.requestOtp()
    UserRepository.existsByEmail()          → UserAlreadyExistsException if found
    OtpDomainService.generate()             TOTP (HmacSHA1, 6-digit, 30s step)
    OtpStore.save(email, otp, Duration.ofMinutes(5))   Redis otp:{email}
    SendEventSpi.send("otp.request", OtpRequestedEvent(email, otp))   Kafka
← 204

POST /auth/register {username,email,password,otpCode}
  AuthDomainService.register()
    OtpVerifier.verify(email, code)         Redis lookup → InvalidOtpException if wrong
    UserRepository.existsByEmail/Username   → UserAlreadyExistsException
    PasswordEncoder.encode(rawPassword)
    UserRepository.save(user)
← 201 UserResponse
```

### Login
```
POST /auth/login {email,password}
  AuthDomainService.authenticate()
    UserRepository.findByEmail()            → InvalidCredentialsException if not found
    PasswordEncoder.matches()               → InvalidCredentialsException if wrong
    TokenGenerator.generatePair(user)       JWT access (15 min) + refresh (7 days)
    TokenRepository.save() × 2
← 200 {accessToken, refreshToken}
```

---

## DB schema
```
users      (id UUID PK, username TEXT UNIQUE, email TEXT UNIQUE, password_hash TEXT, active BOOL DEFAULT true, created_at TIMESTAMP)
user_roles (user_id UUID FK→users.id, role TEXT)
tokens     (id UUID PK, value TEXT UNIQUE, user_id UUID FK→users.id, type TEXT, issued_at TIMESTAMP, expires_at TIMESTAMP, revoked BOOL DEFAULT false)
```
Schema: `microservice/src/main/resources/db/migration/V1__init.sql`
Flyway disabled; `spring.jpa.hibernate.ddl-auto=update` in dev.

---

## Adding a new SPI
1. Define interface in `spi` module under appropriate sub-package
2. Implement in an `spi-adapter-*` module (create new module if needed)
3. Register `@Bean` in `AppConfig`
4. Inject into use-case via constructor (update `AppConfig` factory method)
