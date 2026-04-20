package kz.dev.api.adapter.rest.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.dev.api.adapter.rest.dto.LoginRequest;
import kz.dev.api.adapter.rest.dto.RefreshRequest;
import kz.dev.api.adapter.rest.dto.RegisterRequest;
import kz.dev.api.adapter.rest.dto.SendOtpRequest;
import kz.dev.api.adapter.rest.dto.TokenPairResponse;
import kz.dev.api.adapter.rest.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@Tag(name = "Auth")
@HttpExchange("/auth")
public interface AuthClient {

    @Operation(summary = "Send OTP to email (step 1 of registration)")
    @PostExchange(value = "/otp/send", contentType = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Void> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "Register with OTP verification (step 2 of registration)")
    @PostExchange(value = "/register", contentType = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "Login — returns access + refresh token pair")
    @PostExchange(value = "/login", contentType = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<TokenPairResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "Refresh token pair (rotates refresh token)")
    @PostExchange(value = "/refresh", contentType = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<TokenPairResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "Logout — revokes all tokens for current user")
    @SecurityRequirement(name = "bearerAuth")
    @PostExchange("/logout")
    default ResponseEntity<Void> logout() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
