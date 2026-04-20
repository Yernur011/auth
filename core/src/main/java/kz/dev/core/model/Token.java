package kz.dev.core.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Token {

    private final String value;
    private final UUID userId;
    private final TokenType type;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private boolean revoked;

    public Token(String value, UUID userId, TokenType type,
                 LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this(value, userId, type, issuedAt, expiresAt, false);
    }

    public Token(String value, UUID userId, TokenType type,
                 LocalDateTime issuedAt, LocalDateTime expiresAt, boolean revoked) {
        this.value = value;
        this.userId = userId;
        this.type = type;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public String getValue() { return value; }
    public UUID getUserId() { return userId; }
    public TokenType getType() { return type; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
}
