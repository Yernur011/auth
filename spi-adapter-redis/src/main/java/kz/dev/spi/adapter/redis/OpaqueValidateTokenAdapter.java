package kz.dev.spi.adapter.redis;

import kz.dev.core.exception.InvalidTokenException;
import kz.dev.core.model.Token;
import kz.dev.core.model.TokenType;
import kz.dev.core.model.User;
import kz.dev.spi.auth.ValidateTokenSpi;
import kz.dev.spi.persistence.TokenRepository;
import kz.dev.spi.persistence.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpaqueValidateTokenAdapter implements ValidateTokenSpi {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Override
    public User validate(String tokenValue) {
        Token token = tokenRepository.findByValueAndType(tokenValue, TokenType.ACCESS)
                .orElseThrow(() -> new InvalidTokenException("Access token not found"));

        if (!token.isValid()) {
            throw new InvalidTokenException("Access token is expired or revoked");
        }

        return userRepository.findById(token.getUserId())
                .orElseThrow(() -> new InvalidTokenException("Token owner not found"));
    }
}
