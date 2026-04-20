package kz.dev.spi.auth;

import kz.dev.core.model.User;

@FunctionalInterface
public interface ValidateTokenSpi {
    User validate(String tokenValue);
}
