package kz.dev.spi.otp;

public interface OtpPort {
    void send(String email);
    boolean verify(String email, String code);
}
