package kz.dev.usecase.auth;

import kz.dev.api.auth.RequestOtpApi;
import kz.dev.core.exception.UserAlreadyExistsException;
import kz.dev.spi.otp.OtpPort;
import kz.dev.spi.persistence.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OtpDomainService implements RequestOtpApi {

    private final UserRepository userRepository;
    private final OtpPort otpPort;

    @Override
    public void requestOtp(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already in use: " + email);
        }
        otpPort.send(email);
    }
}
