# Architectural Decisions

## 2026-05-08 — OTP delivery via Kafka event, not direct HTTP

OTP codes are published as `OtpRequestedEvent` to Kafka topic `otp.request` rather than calling an OTP microservice directly via HTTP. `OtpSender` SPI exists but is unused; delivery is the responsibility of a downstream Kafka consumer.

**Why:** Decouples auth service from OTP delivery infrastructure; allows swapping delivery channel (email, SMS) without touching this service.

---

## 2026-05-08 — Redis for OTP storage

OTP codes stored in Redis (`otp:{email}`, TTL 5 min) rather than in PostgreSQL. Implemented as `RedisOtpAdapter` implementing both `OtpStore` and `OtpVerifier`.

**Why:** Short-lived data with TTL fits Redis better than a DB table; avoids migration complexity.

---

## 2026-05-08 — TOTP algorithm for OTP generation

OTP generation uses TOTP (RFC 6238): HmacSHA1, 6 digits, 30-second time step. Secret from `app.otp.secret-key`. Implemented directly in `OtpDomainService` which also implements `OtpGenerator`.

**Why:** Standard algorithm; self-contained, no external dependency for generation.

---

## 2026-05-08 — Manual @Bean wiring in AppConfig, no @Component on adapters

All adapter beans wired manually in `microservice/AppConfig` rather than using `@Component` + component scan.

**Why:** Keeps adapters free of Spring annotations; makes dependencies explicit and traceable; allows swapping impls (e.g. different OTP store) without code changes in adapter classes.
