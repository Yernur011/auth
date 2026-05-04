package kz.dev.usecase.auth;

import kz.dev.api.auth.RequestOtpApi;
import kz.dev.core.exception.UserAlreadyExistsException;
import kz.dev.core.model.event.OtpRequestedEvent;
import kz.dev.spi.event.SendEventSpi;
import kz.dev.spi.otp.OtpGenerator;
import kz.dev.spi.otp.OtpStore;
import kz.dev.spi.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@RequiredArgsConstructor
public class OtpDomainService implements RequestOtpApi, OtpGenerator {

    private final UserRepository userRepository;
    private final SendEventSpi sendEventSpi;
    private final OtpStore otpStore;
    private final String otpTopic;
    private static final String ALGORITHM = "HmacSHA1";
    private static final int DIGITS = 6;
    private static final int TIME_STEP = 30;

    @Value("${app.otp.secret-key}")
    String secretKey;

    @Override
    @Transactional
    public void requestOtp(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already in use: " + email);
        }
        String otp = generate();

        otpStore.save(email, otp, Duration.ofMinutes(5)); //TODO нужно сделть конфигурацию времени хранения отп
        sendEventSpi.send(otpTopic, new OtpRequestedEvent(email, otp));
    }

    @Override
    public String generate() {
        long counter = System.currentTimeMillis() / 1000L / TIME_STEP;

        byte[] data = ByteBuffer.allocate(8).putLong(counter).array();

        SecretKeySpec signKey = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Mac mac = null;
        try {
            mac = Mac.getInstance(ALGORITHM);
            mac.init(signKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] hash = mac.doFinal(data);
        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7F) << 24) |
                ((hash[offset + 1] & 0xFF) << 16) |
                ((hash[offset + 2] & 0xFF) << 8) |
                (hash[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, DIGITS);

        return String.format("%0" + DIGITS + "d", otp);
    }
}
