package kz.dev.spi.adapter.security.configuration;

import kz.dev.spi.adapter.security.model.UserPrincipal;
import kz.dev.spi.security.SecurityContextPort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SpringSecurityContextAdapter implements SecurityContextPort {

    @Override
    public UUID getCurrentUserId() {
        var principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getId();
    }

    @Override
    public String getCurrentToken() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }
}
