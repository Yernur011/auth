package kz.dev.core.model.command;

public record AuthenticateCommand(String email, String rawPassword) {}
