package kz.dev.core.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class User {
    private final UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private final Set<Role> roles;
    private boolean active;
    private final LocalDateTime createdAt;

    public User(UUID id, String username, String email, String passwordHash,
                Set<Role> roles, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = roles.isEmpty() ? EnumSet.of(Role.USER) : EnumSet.copyOf(roles);
        this.active = active;
        this.createdAt = createdAt;
    }

    public static User create(String username, String email, String passwordHash) {
        return new User(
                UUID.randomUUID(),
                username,
                email,
                passwordHash,
                EnumSet.of(Role.USER),
                true,
                LocalDateTime.now()
        );
    }

    public void assignRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        if (role == Role.USER) throw new IllegalArgumentException("Cannot remove base USER role");
        roles.remove(role);
    }

    public void deactivate() {
        this.active = false;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }

    public boolean isActive() { return active; }

    public String getPasswordHash() { return passwordHash; }
}
