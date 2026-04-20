package kz.dev.spi.adapter.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTtlSeconds,
        long refreshTtlSeconds
) {}
