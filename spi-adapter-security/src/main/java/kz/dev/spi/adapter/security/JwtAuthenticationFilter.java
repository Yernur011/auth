package kz.dev.spi.adapter.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.dev.core.model.User;
import kz.dev.spi.auth.ValidateTokenSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ValidateTokenSpi validateTokenUseCase;
    private final HandlerExceptionResolver exceptionResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                User user = validateTokenUseCase.validate(token);
                var authorities = user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                        .toList();
                var auth = new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                exceptionResolver.resolveException(request, response, null, e);
            }
        }
        chain.doFilter(request, response);
    }
}
