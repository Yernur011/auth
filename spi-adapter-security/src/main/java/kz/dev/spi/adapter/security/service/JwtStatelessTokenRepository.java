package kz.dev.spi.adapter.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kz.dev.core.model.Token;
import kz.dev.core.model.TokenType;
import kz.dev.spi.adapter.security.configuration.JwtProperties;
import kz.dev.spi.persistence.TokenRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class JwtStatelessTokenRepository implements TokenRepository {

    private final SecretKey signingKey;

    public JwtStatelessTokenRepository(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Token save(Token token) {
        return token;
    }

    @Override
    public Optional<Token> findByValue(String value) {
        return parse(value);
    }

    @Override
    public Optional<Token> findByValueAndType(String value, TokenType type) {
        return parse(value).filter(t -> t.getType() == type);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        // stateless — tokens expire naturally
    }

    private Optional<Token> parse(String value) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(value)
                    .getPayload();

            return Optional.of(new Token(
                    value,
                    UUID.fromString(claims.getSubject()),
                    TokenType.valueOf(claims.get("type", String.class)),
                    toLocalDateTime(claims.getIssuedAt()),
                    toLocalDateTime(claims.getExpiration()),
                    false
            ));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
    }
}
