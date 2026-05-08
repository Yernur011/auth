package kz.dev.usecase.auth;

import kz.dev.api.auth.AuthenticateUserApi;
import kz.dev.api.auth.RefreshTokenApi;
import kz.dev.api.auth.RegisterUserApi;
import kz.dev.api.auth.RevokeTokenApi;
import kz.dev.spi.adapter.redis.RedisOtpAdapter;
import kz.dev.core.exception.InvalidCredentialsException;
import kz.dev.core.exception.InvalidOtpException;
import kz.dev.core.exception.InvalidTokenException;
import kz.dev.core.exception.UserAlreadyExistsException;
import kz.dev.core.model.Token;
import kz.dev.core.model.TokenPair;
import kz.dev.core.model.TokenType;
import kz.dev.core.model.User;
import kz.dev.core.model.command.AuthenticateCommand;
import kz.dev.core.model.command.RegisterCommand;
import kz.dev.spi.auth.PasswordEncoder;
import kz.dev.spi.auth.TokenGenerator;
import kz.dev.spi.persistence.TokenRepository;
import kz.dev.spi.persistence.UserRepository;
import kz.dev.spi.security.SecurityContextPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AuthDomainService implements RegisterUserApi, AuthenticateUserApi,
        RefreshTokenApi, RevokeTokenApi {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final SecurityContextPort securityContextPort;
    private final RedisOtpAdapter redisOtpAdapter;

    @Override
    @Transactional
    public User register(RegisterCommand command) {
        if (!redisOtpAdapter.verify(command.email(), command.otpCode())) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("Email already in use: " + command.email());
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new UserAlreadyExistsException("Username already taken: " + command.username());
        }
        String hash = passwordEncoder.encode(command.rawPassword());
        User user = User.create(command.username(), command.email(), hash);
        User save = userRepository.save(user);

        redisOtpAdapter.deleteByEmail(save.getEmail());
        return save;
    }

    @Override
    public TokenPair authenticate(AuthenticateCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) throw new InvalidCredentialsException();
        if (!passwordEncoder.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return issuePair(user);
    }

    @Override
    public TokenPair refresh(String refreshTokenValue) {
        Token refreshToken = tokenRepository
                .findByValueAndType(refreshTokenValue, TokenType.REFRESH)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("Token owner not found"));

        refreshToken.revoke();
        tokenRepository.save(refreshToken);

        return issuePair(user);
    }

    @Override
    public void revoke() {
        String tokenValue = securityContextPort.getCurrentToken();
        tokenRepository.findByValue(tokenValue).ifPresent(token -> {
            token.revoke();
            tokenRepository.save(token);
        });
    }

    @Override
    public void revokeAll() {
        tokenRepository.revokeAllByUserId(securityContextPort.getCurrentUserId());
    }

    private TokenPair issuePair(User user) {
        TokenPair pair = tokenGenerator.generatePair(user);
        tokenRepository.save(pair.accessToken());
        tokenRepository.save(pair.refreshToken());
        return pair;
    }
}
