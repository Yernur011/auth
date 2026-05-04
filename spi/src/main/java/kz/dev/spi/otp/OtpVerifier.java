package kz.dev.spi.otp;

public interface OtpVerifier {
    boolean verify(String email, String code);
}
