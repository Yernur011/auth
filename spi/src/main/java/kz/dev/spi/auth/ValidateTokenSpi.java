package kz.dev.spi.auth;

import kz.dev.core.model.User;

public interface ValidateTokenSpi {
    User validate(String tokenValue);
}
