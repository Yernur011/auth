package kz.dev.api.auth;

import java.util.UUID;

public interface RevokeTokenApi {
    void revoke(String tokenValue);
    void revokeAll(UUID userId);
}
