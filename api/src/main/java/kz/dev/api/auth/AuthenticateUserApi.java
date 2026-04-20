package kz.dev.api.auth;

import kz.dev.core.model.TokenPair;
import kz.dev.core.model.command.AuthenticateCommand;

public interface AuthenticateUserApi {
    TokenPair authenticate(AuthenticateCommand command);
}
