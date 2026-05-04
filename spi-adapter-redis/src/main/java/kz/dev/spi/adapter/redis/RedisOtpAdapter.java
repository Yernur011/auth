package kz.dev.spi.adapter.redis;

import kz.dev.spi.otp.OtpStore;
import kz.dev.spi.otp.OtpVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.function.Function;

@RequiredArgsConstructor
public class RedisOtpAdapter implements OtpVerifier, OtpStore {

    static final String KEY_PREFIX = "otp:";

    private final StringRedisTemplate redisTemplate;

    private final Function<String, String> getKey = s ->  KEY_PREFIX + s; // TODO to util class

    @Override
    public void save(String email, String code, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX + email, code, ttl);
    }

    @Override
    public void deleteByEmail(String email) {
        String key = getKey.apply(email);
        redisTemplate.delete(key);
    }

    @Override
    public void deleteByKey(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean verify(String email, String code) {
        String key = getKey.apply(email);
        String stored = redisTemplate.opsForValue().get(key);

        return stored != null && stored.equals(code);
    }
}
