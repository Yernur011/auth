# CLAUDE.md

## Project
Auth entry-point microservice: JWT auth, OTP registration, token lifecycle.

## Stack
Java 25 · Spring Boot 4.0.5 · Spring Security · Spring Data JPA · PostgreSQL · Redis · Kafka · Lombok · MapStruct · SpringDoc OpenAPI 3.0.2

## Module map
| Module | Package | Role |
|---|---|---|
| `core` | `kz.dev.core` | Domain models, exceptions, events — zero framework deps |
| `api` | `kz.dev.api.auth` | In-port interfaces (driving ports) |
| `spi` | `kz.dev.spi` | Out-port interfaces (driven ports) |
| `use-case` | `kz.dev.usecase.auth` | Domain services (implement api, depend on spi) |
| `api-adapter-rest` | `kz.dev.api.adapter.rest` | REST controllers, DTOs, exception handler |
| `spi-adapter-jpa` | `kz.dev.spi.adapter.jpa` | JPA entities, repositories, MapStruct mappers |
| `spi-adapter-security` | `kz.dev.spi.adapter.security` | JWT, BCrypt, Spring Security filter |
| `spi-adapter-redis` | `kz.dev.spi.adapter.redis` | Redis OTP store + verifier |
| `spi-adapter-kafka` | `kz.dev.spi.adapter.kafka` | Kafka event publisher |
| `microservice` | `kz.dev.microservice` | Spring Boot main, manual `@Bean` wiring, SecurityConfig |

## Key files
```
microservice/src/main/java/kz/dev/microservice/config/AppConfig.java      # all @Bean wiring
microservice/src/main/java/kz/dev/microservice/config/SecurityConfig.java  # public/private paths
microservice/src/main/resources/application.properties                      # all config values
use-case/.../auth/AuthDomainService.java                                   # register/login/refresh/revoke
use-case/.../auth/OtpDomainService.java                                    # TOTP gen, Redis store, Kafka send
api-adapter-rest/.../AuthController.java                                   # REST endpoints
spi-adapter-jpa/.../UserRepositoryAdapter.java                             # User CRUD
spi-adapter-jpa/.../TokenRepositoryAdapter.java                            # Token upsert/revoke
microservice/src/main/resources/db/migration/V1__init.sql                  # DB schema
```

## Domain models (`core`)
- `User` — aggregate root: UUID id, username, email, passwordHash, roles (Set\<Role\>), active, createdAt
- `Token` — value object: value, userId, type, issuedAt, expiresAt, revoked; `isValid()`, `revoke()`
- `TokenPair` — record(accessToken, refreshToken)
- `Role` enum: USER, ADMIN, MODERATOR · `TokenType` enum: ACCESS, REFRESH
- `OtpRequestedEvent(email, otp)` — published to Kafka on OTP request

## In-ports (`api`)
| Interface | Signature |
|---|---|
| `RequestOtpApi` | `requestOtp(String email)` |
| `RegisterUserApi` | `register(RegisterCommand) → User` |
| `AuthenticateUserApi` | `authenticate(AuthenticateCommand) → TokenPair` |
| `RefreshTokenApi` | `refresh(String refreshToken) → TokenPair` |
| `RevokeTokenApi` | `revoke()`, `revokeAll()` |

## Out-ports (`spi`)
| Interface | Package | Impl |
|---|---|---|
| `UserRepository` | `spi.persistence` | `UserRepositoryAdapter` (JPA) |
| `TokenRepository` | `spi.persistence` | `TokenRepositoryAdapter` (JPA) |
| `PasswordEncoder` | `spi.auth` | `BcryptPasswordEncoderAdapter` |
| `TokenGenerator` | `spi.auth` | `JwtTokenGeneratorAdapter` (JJWT 0.12.6, HMAC-SHA256) |
| `ValidateTokenSpi` | `spi.auth` | `AuthDomainService` |
| `OtpGenerator` | `spi.otp` | `OtpDomainService` (TOTP/HmacSHA1, 6-digit, 30s step) |
| `OtpStore` | `spi.otp` | `RedisOtpAdapter` (key: `otp:{email}`, TTL 5 min) |
| `OtpVerifier` | `spi.otp` | `RedisOtpAdapter` |
| `OtpSender` | `spi.otp` | *(not yet wired — sending via Kafka event)* |
| `SendEventSpi` | `spi.event` | `KafkaEventAdapter` (topic: `otp.request`) |
| `SecurityContextPort` | `spi.security` | `SpringSecurityContextAdapter` |

## OTP flow (actual)
```
POST /auth/otp/send {email}
  → OtpDomainService.requestOtp()
      → UserRepository.existsByEmail()     throws UserAlreadyExistsException if taken
      → OtpDomainService.generate()        TOTP via HmacSHA1
      → OtpStore.save(email, otp, 5min)    Redis key otp:{email}
      → SendEventSpi.send(topic, OtpRequestedEvent(email,otp))  Kafka otp.request
← 204

POST /auth/register {username, email, password, otpCode}
  → AuthDomainService.register()
      → OtpVerifier.verify(email, code)    reads Redis; throws InvalidOtpException if wrong
      → UserRepository.existsByEmail/Username
      → PasswordEncoder.encode()
      → UserRepository.save()
← 201 UserResponse
```

## Endpoints
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/auth/otp/send` | public | `{email}` | 204 |
| POST | `/auth/register` | public | `{username,email,password,otpCode}` | 201 UserResponse |
| POST | `/auth/login` | public | `{email,password}` | 200 TokenPairResponse |
| POST | `/auth/refresh` | public | `{refreshToken}` | 200 TokenPairResponse |
| POST | `/auth/logout` | Bearer | — | 204 |

Swagger: `http://localhost:8080/swagger-ui.html`

## DB schema
```
users      (id UUID PK, username UNIQUE, email UNIQUE, password_hash, active BOOL, created_at)
user_roles (user_id FK→users, role)
tokens     (id UUID PK, value UNIQUE, user_id FK→users, type, issued_at, expires_at, revoked BOOL)
```
Flyway disabled in properties; schema managed via `ddl-auto=update`.

## Rules

### Architecture
- `core` — ZERO Spring/JPA/Lombok annotations (models are plain Java records/classes)
- Controllers inject in-ports only — never use-case classes directly
- JPA entities stay in `spi-adapter-jpa` — never leak into `core`
- Mappers between every layer boundary (MapStruct in JPA adapter)
- New SPIs → add interface in `spi`, impl in relevant `spi-adapter-*`, wire in `AppConfig`

### Code style
- Lombok (`@Data`, `@RequiredArgsConstructor`) on DTOs and JPA entities only
- Constructor injection only — no `@Autowired`
- No comments unless WHY is non-obvious
- No placeholders — complete working code only

### Docs
- Read @docs/architecture.md before any structural decision
- Append architectural decisions to @docs/decisions.md
- Update @docs/handoff.md at end of each session

### Response format
- Terse answers · file path before every code block · no preamble
