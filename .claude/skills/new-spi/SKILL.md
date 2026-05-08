---
name: new-spi
description: Scaffold a new SPI (out-port) interface and its adapter implementation for this hexagonal auth microservice. Creates the interface in the spi module, the adapter in the correct spi-adapter-* module, and the @Bean wiring in AppConfig. Use when adding a new external dependency (cache, messaging, external API, etc.).
argument-hint: "<InterfaceName> in <spi-adapter-module> — <description>  e.g. EmailSender in spi-adapter-kafka — sends transactional emails"
---

Scaffold a new SPI for this hexagonal auth microservice. Argument: `$ARGUMENTS`

## Existing SPI structure
!`find spi/src -name "*.java" | sort`

## Existing adapters
!`find spi-adapter-*/src -name "*.java" | sort`

## AppConfig (current wiring)
!`cat microservice/src/main/java/kz/dev/microservice/config/AppConfig.java`

## Steps — write complete code for each file

### 1. SPI interface (`spi` module)
- Package: `kz.dev.spi.<sub-package>` — pick the right sub-package:
  - `persistence` — database/storage operations
  - `auth` — authentication/token operations
  - `otp` — OTP generation/storage/verification
  - `event` — async event publishing
  - `security` — Spring Security context
  - or create a new sub-package if none fit
- Plain Java interface — zero Spring annotations
- Method signatures use only domain types from `core`

### 2. Adapter implementation (`spi-adapter-*` module)
- Package: `kz.dev.spi.adapter.<module-name>`
- Implement the SPI interface
- No `@Component` — will be wired manually in AppConfig
- Use `@RequiredArgsConstructor` (Lombok) for dependencies
- Depend on framework/library classes here (Spring, Kafka, Redis, etc.)

### 3. AppConfig wiring (`microservice`)
Add a `@Bean` method to `AppConfig`:
```java
@Bean
public <InterfaceName> <beanName>(<Dependencies>) {
    return new <AdapterClass>(<args>);
}
```

### 4. Inject into use-case (if needed)
- Add the new SPI to the constructor of the relevant domain service in `use-case`
- Update the `@Bean` factory method in `AppConfig` to pass it

## Rules reminder
- SPI interface: zero annotations, plain Java
- Adapter: no `@Component`, constructor injection, Lombok allowed
- File path before every code block
