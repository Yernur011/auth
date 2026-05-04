package kz.dev.spi.adapter.security.configuration;

public record JwtProperties(
        String secret,
        long accessTtlSeconds,
        long refreshTtlSeconds) {
}
