package kz.dev.spi.adapter.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kz.dev.core.model.Token;
import kz.dev.core.model.TokenPair;
import kz.dev.core.model.TokenType;
import kz.dev.core.model.User;
import kz.dev.spi.auth.TokenGenerator;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.stream.Collectors;

@Component

public class JwtTokenGeneratorAdapter implements TokenGenerator {

    private final SecretKey signingKey;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtTokenGeneratorAdapter(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = properties.accessTtlSeconds();
        this.refreshTtlSeconds = properties.refreshTtlSeconds();
    }

    @Override
    public TokenPair generatePair(User user) {
        LocalDateTime now = LocalDateTime.now();
        Token accessToken = buildToken(user, TokenType.ACCESS, now, now.plusSeconds(accessTtlSeconds));
        Token refreshToken = buildToken(user, TokenType.REFRESH, now, now.plusSeconds(refreshTtlSeconds));
        return new TokenPair(accessToken, refreshToken);
    }

    private Token buildToken(User user, TokenType type, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        String roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        String jwt = Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", type.name())
                .claim("roles", roles)
                .claim("username", user.getUsername())
                .issuedAt(toDate(issuedAt))
                .expiration(toDate(expiresAt))
                .signWith(signingKey)
                .compact();

        return new Token(jwt, user.getId(), type, issuedAt, expiresAt);
    }

    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.toInstant(ZoneOffset.UTC));
    }
}
