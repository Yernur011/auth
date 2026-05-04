package kz.dev.spi.adapter.redis;

import kz.dev.spi.otp.OtpStore;
import kz.dev.spi.otp.OtpVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@RequiredArgsConstructor
public class RedisOtpAdapter implements OtpVerifier, OtpStore {

    static final String KEY_PREFIX = "otp:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String email, String code, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX + email, code, ttl);
    }

    @Override
    public boolean verify(String email, String code) {
        String key = KEY_PREFIX + email;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored != null && stored.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
