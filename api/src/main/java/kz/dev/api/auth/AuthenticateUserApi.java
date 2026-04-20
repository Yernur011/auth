package kz.dev.api.auth;

import kz.dev.core.model.TokenPair;
import kz.dev.core.model.command.AuthenticateCommand;

@FunctionalInterface
public interface AuthenticateUserApi {
    TokenPair authenticate(AuthenticateCommand command);
}
