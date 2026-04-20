package kz.dev.api.adapter.rest.client;

import jakarta.validation.Valid;
import kz.dev.api.adapter.rest.dto.LoginRequest;
import kz.dev.api.adapter.rest.dto.RefreshRequest;
import kz.dev.api.adapter.rest.dto.RegisterRequest;
import kz.dev.api.adapter.rest.dto.TokenPairResponse;
import kz.dev.api.adapter.rest.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(value = "/auth", contentType = MediaType.APPLICATION_JSON_VALUE)
public interface AuthClient {

    @PostExchange(value = "/register")
    default ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostExchange(value = "/login")
    default ResponseEntity<TokenPairResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostExchange(value = "/refresh")
    default ResponseEntity<TokenPairResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostExchange(value = "/logout")
    default ResponseEntity<Void> logout() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

}
