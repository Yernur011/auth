package kz.dev.spi.otp;

public interface OtpSender {
    void send(String email);
}
