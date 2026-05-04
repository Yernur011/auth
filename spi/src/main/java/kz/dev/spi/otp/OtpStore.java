package kz.dev.spi.otp;

import java.time.Duration;

public interface OtpStore {
    void save(String email, String code, Duration ttl);
}
