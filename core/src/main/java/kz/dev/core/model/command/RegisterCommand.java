package kz.dev.core.model.command;

public record RegisterCommand(String username, String email, String rawPassword, String otpCode) {}
