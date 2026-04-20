package kz.dev.api.auth;

import kz.dev.core.model.TokenPair;

@FunctionalInterface
public interface RefreshTokenApi {
    TokenPair refresh(String refreshTokenValue);
}
