package kz.dev.spi.auth;

import kz.dev.core.model.TokenPair;
import kz.dev.core.model.User;

public interface TokenGenerator {
    TokenPair generatePair(User user);
}
