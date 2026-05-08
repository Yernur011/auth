package kz.dev.spi.adapter.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kz.dev.core.exception.InvalidTokenException;
import kz.dev.core.model.TokenType;
import kz.dev.core.model.User;
import kz.dev.spi.adapter.security.configuration.JwtProperties;
import kz.dev.spi.auth.ValidateTokenSpi;
import kz.dev.spi.persistence.UserRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class JwtValidateTokenAdapter implements ValidateTokenSpi {

    private final SecretKey signingKey;
    private final UserRepository userRepository;

    public JwtValidateTokenAdapter(JwtProperties properties, UserRepository userRepository) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.userRepository = userRepository;
    }

    @Override
    public User validate(String tokenValue) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(tokenValue)
                    .getPayload();
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or expired JWT: " + e.getMessage());
        }

        if (!TokenType.ACCESS.name().equals(claims.get("type", String.class))) {
            throw new InvalidTokenException("Token is not an access token");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new InvalidTokenException("User not found or inactive"));
    }
}
