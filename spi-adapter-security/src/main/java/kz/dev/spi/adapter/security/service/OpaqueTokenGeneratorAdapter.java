package kz.dev.spi.adapter.security.service;

import kz.dev.core.model.Token;
import kz.dev.core.model.TokenPair;
import kz.dev.core.model.TokenType;
import kz.dev.core.model.User;
import kz.dev.spi.auth.TokenGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

public class OpaqueTokenGeneratorAdapter implements TokenGenerator {

    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public OpaqueTokenGeneratorAdapter(long accessTtlSeconds, long refreshTtlSeconds) {
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    @Override
    public TokenPair generatePair(User user) {
        LocalDateTime now = LocalDateTime.now();
        Token access = new Token(
                UUID.randomUUID().toString(), user.getId(), TokenType.ACCESS,
                now, now.plusSeconds(accessTtlSeconds)
        );
        Token refresh = new Token(
                UUID.randomUUID().toString(), user.getId(), TokenType.REFRESH,
                now, now.plusSeconds(refreshTtlSeconds)
        );
        return new TokenPair(access, refresh);
    }
}
