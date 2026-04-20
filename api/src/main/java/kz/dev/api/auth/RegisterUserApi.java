package kz.dev.api.auth;

import kz.dev.core.model.User;
import kz.dev.core.model.command.RegisterCommand;

public interface RegisterUserApi {
    User register(RegisterCommand command);
}
