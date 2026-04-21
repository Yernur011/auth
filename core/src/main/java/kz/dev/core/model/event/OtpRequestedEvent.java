package kz.dev.core.model.event;

public record OtpRequestedEvent(String email, String otp) {
}
