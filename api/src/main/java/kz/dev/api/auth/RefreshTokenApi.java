package kz.dev.api.auth;

import kz.dev.core.model.TokenPair;

public interface RefreshTokenApi {
    TokenPair refresh(String refreshTokenValue);
}
