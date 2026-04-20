package kz.dev.spi.auth;

import kz.dev.core.model.Token;
import kz.dev.core.model.TokenType;

import java.util.Optional;
import java.util.UUID;

public interface TokenRepository {
    Token save(Token token);
    Optional<Token> findByValue(String value);
    Optional<Token> findByValueAndType(String value, TokenType type);
    void revokeAllByUserId(UUID userId);
}
