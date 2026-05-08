# Handoff Log

## 2026-05-08

### Done
- Rewrote `CLAUDE.md` — now a dense quick-reference with all 10 modules, exact package paths, key file locations, SPI→impl mapping, correct OTP flow (TOTP/HmacSHA1 + Redis + Kafka), complete endpoint table
- Updated `docs/architecture.md` — synced with actual code: removed phantom `OtpMockAdapter`, added `RedisOtpAdapter` + `KafkaEventAdapter`, split OTP SPI into `OtpGenerator/Store/Verifier`, added `SendEventSpi`, documented TOTP algorithm details, corrected Flyway/ddl-auto state
- Replaced broken symlinks (`handoff.md`, `decisions.md`) with real files
- Created Claude Code memory files (`project_context.md`, `user_profile.md`, `feedback_code_style.md`)

### Current state
All 10 modules compile and wire correctly. OTP flow: TOTP generated in `OtpDomainService`, stored in Redis (`otp:{email}`, 5 min TTL), published to Kafka topic `otp.request` as `OtpRequestedEvent`. Flyway is disabled; schema managed via `ddl-auto=update`.

### Next / open items
- `OtpSender` SPI exists but is not wired — OTP delivery is handled via Kafka consumer (external service)
- `OtpStore.deleteByKey(String key)` and `deleteByEmail(String email)` — OTP cleanup after successful registration not yet called in `AuthDomainService.register()`
- Flyway should be re-enabled before production (`spring.flyway.enabled=false` in properties)
- `app.jwt.secret` and `app.otp.secret-key` use placeholder values — must be replaced before deploy
- `OtpDomainService` has a `@Value("${app.otp.secret-key}")` field injection (not constructor) — inconsistent with project style
