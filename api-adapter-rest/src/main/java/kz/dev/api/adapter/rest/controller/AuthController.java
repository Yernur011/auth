package kz.dev.api.adapter.rest.controller;

import kz.dev.api.adapter.rest.client.AuthClient;
import kz.dev.api.adapter.rest.dto.LoginRequest;
import kz.dev.api.adapter.rest.dto.RefreshRequest;
import kz.dev.api.adapter.rest.dto.RegisterRequest;
import kz.dev.api.adapter.rest.dto.TokenPairResponse;
import kz.dev.api.adapter.rest.dto.UserResponse;
import kz.dev.api.auth.AuthenticateUserApi;
import kz.dev.api.auth.RefreshTokenApi;
import kz.dev.api.auth.RegisterUserApi;
import kz.dev.api.auth.RevokeTokenApi;
import kz.dev.core.model.command.AuthenticateCommand;
import kz.dev.core.model.command.RegisterCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthController implements AuthClient {

    private final RegisterUserApi registerUserApi;
    private final AuthenticateUserApi authenticateUserApi;
    private final RefreshTokenApi refreshTokenApi;
    private final RevokeTokenApi revokeTokenApi;

    public AuthController(RegisterUserApi registerUserApi,
                          AuthenticateUserApi authenticateUserApi,
                          RefreshTokenApi refreshTokenApi,
                          RevokeTokenApi revokeTokenApi) {
        this.registerUserApi = registerUserApi;
        this.authenticateUserApi = authenticateUserApi;
        this.refreshTokenApi = refreshTokenApi;
        this.revokeTokenApi = revokeTokenApi;
    }

    @Override
    public ResponseEntity<UserResponse> register(RegisterRequest request) {
        var user = registerUserApi.register(new RegisterCommand(request.username(), request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @Override
    public ResponseEntity<TokenPairResponse> login(LoginRequest request) {
        var pair = authenticateUserApi.authenticate(new AuthenticateCommand(request.email(), request.password()));
        return ResponseEntity.ok(TokenPairResponse.from(pair));
    }

    @Override
    public ResponseEntity<TokenPairResponse> refresh(RefreshRequest request) {
        var pair = refreshTokenApi.refresh(request.refreshToken());
        return ResponseEntity.ok(TokenPairResponse.from(pair));
    }

    @Override
    public ResponseEntity<Void> logout() {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        revokeTokenApi.revokeAll(userId);
        return ResponseEntity.noContent().build();
    }
}
