package kz.dev.spi.security;

import java.util.UUID;

public interface SecurityContextPort {
    UUID getCurrentUserId();
    String getCurrentToken();
}
