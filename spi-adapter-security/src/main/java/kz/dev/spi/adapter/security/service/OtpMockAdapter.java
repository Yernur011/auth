package kz.dev.spi.adapter.security.service;

import kz.dev.spi.otp.OtpPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpMockAdapter implements OtpPort {

    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Override
    public void send(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(email, code);
        System.out.printf("Mock OTP sent to {%s}: {%s}%n", email, code);
    }

    @Override
    public boolean verify(String email, String code) {
        String stored = store.get(email);
        if (stored != null && stored.equals(code)) {
            store.remove(email);
            return true;
        }
        return false;
    }
}
