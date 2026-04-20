package kz.dev.api.adapter.rest.dto;

import kz.dev.core.model.Role;
import kz.dev.core.model.User;

import java.util.Set;
import java.util.UUID;

public record UserResponse(UUID id, String username, String email, Set<Role> roles) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles());
    }
}
