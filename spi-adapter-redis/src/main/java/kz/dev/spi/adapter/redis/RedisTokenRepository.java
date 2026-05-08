package kz.dev.spi.adapter.redis;

import kz.dev.core.model.Token;
import kz.dev.core.model.TokenType;
import kz.dev.spi.persistence.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class RedisTokenRepository implements TokenRepository {

    private static final String TOKEN_PREFIX = "token:";
    private static final String USER_TOKENS_PREFIX = "user_tokens:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public Token save(Token token) {
        String key = TOKEN_PREFIX + token.getValue();
        if (token.isRevoked()) {
            redisTemplate.delete(key);
            removeFromUserIndex(token.getUserId(), token.getValue());
            return token;
        }
        Duration ttl = Duration.between(LocalDateTime.now(), token.getExpiresAt());
        if (ttl.isNegative()) return token;

        redisTemplate.opsForValue().set(key, serialize(token), ttl);
        redisTemplate.opsForSet().add(USER_TOKENS_PREFIX + token.getUserId(), token.getValue());
        return token;
    }

    @Override
    public Optional<Token> findByValue(String value) {
        String data = redisTemplate.opsForValue().get(TOKEN_PREFIX + value);
        return Optional.ofNullable(data).map(d -> deserialize(value, d));
    }

    @Override
    public Optional<Token> findByValueAndType(String value, TokenType type) {
        return findByValue(value).filter(t -> t.getType() == type);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        String userKey = USER_TOKENS_PREFIX + userId;
        Set<String> values = redisTemplate.opsForSet().members(userKey);
        if (values != null) {
            values.forEach(v -> redisTemplate.delete(TOKEN_PREFIX + v));
        }
        redisTemplate.delete(userKey);
    }

    private void removeFromUserIndex(UUID userId, String value) {
        redisTemplate.opsForSet().remove(USER_TOKENS_PREFIX + userId, value);
    }

    // format: userId|type|issuedAt|expiresAt|revoked
    private String serialize(Token token) {
        return token.getUserId() + "|" + token.getType().name() + "|"
                + token.getIssuedAt() + "|" + token.getExpiresAt() + "|" + token.isRevoked();
    }

    private Token deserialize(String value, String data) {
        String[] p = data.split("\\|");
        return new Token(
                value,
                UUID.fromString(p[0]),
                TokenType.valueOf(p[1]),
                LocalDateTime.parse(p[2]),
                LocalDateTime.parse(p[3]),
                Boolean.parseBoolean(p[4])
        );
    }
}
