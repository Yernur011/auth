package kz.dev.api.adapter.rest.controller;

import kz.dev.api.adapter.rest.client.AuthClient;
import kz.dev.api.adapter.rest.dto.LoginRequest;
import kz.dev.api.adapter.rest.dto.RefreshRequest;
import kz.dev.api.adapter.rest.dto.RegisterRequest;
import kz.dev.api.adapter.rest.dto.SendOtpRequest;
import kz.dev.api.adapter.rest.dto.TokenPairResponse;
import kz.dev.api.adapter.rest.dto.UserResponse;
import kz.dev.api.auth.AuthenticateUserApi;
import kz.dev.api.auth.RefreshTokenApi;
import kz.dev.api.auth.RegisterUserApi;
import kz.dev.api.auth.RequestOtpApi;
import kz.dev.api.auth.RevokeTokenApi;
import kz.dev.core.model.command.AuthenticateCommand;
import kz.dev.core.model.command.RegisterCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthClient {

    private final RequestOtpApi requestOtpApi;
    private final RegisterUserApi registerUserApi;
    private final AuthenticateUserApi authenticateUserApi;
    private final RefreshTokenApi refreshTokenApi;
    private final RevokeTokenApi revokeTokenApi;

    @Override
    public ResponseEntity<Void> sendOtp(SendOtpRequest request) {
        requestOtpApi.requestOtp(request.email());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserResponse> register(RegisterRequest request) {
        var command = new RegisterCommand(request.username(), request.email(), request.password(), request.otpCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(registerUserApi.register(command)));
    }

    @Override
    public ResponseEntity<TokenPairResponse> login(LoginRequest request) {
        var pair = authenticateUserApi.authenticate(new AuthenticateCommand(request.email(), request.password()));
        return ResponseEntity.ok(TokenPairResponse.from(pair));
    }

    @Override
    public ResponseEntity<TokenPairResponse> refresh(RefreshRequest request) {
        return ResponseEntity.ok(TokenPairResponse.from(refreshTokenApi.refresh(request.refreshToken())));
    }

    @Override
    public ResponseEntity<Void> logout() {
        revokeTokenApi.revokeAll();
        return ResponseEntity.noContent().build();
    }
}
