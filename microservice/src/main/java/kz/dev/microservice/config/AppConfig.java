package kz.dev.microservice.config;

import kz.dev.spi.adapter.security.configuration.JwtAuthenticationFilter;
import kz.dev.spi.adapter.security.configuration.JwtProperties;
import kz.dev.spi.adapter.security.service.JwtTokenGeneratorAdapter;
import kz.dev.spi.auth.PasswordEncoder;
import kz.dev.spi.auth.TokenGenerator;
import kz.dev.spi.otp.OtpPort;
import kz.dev.spi.persistence.TokenRepository;
import kz.dev.spi.auth.ValidateTokenSpi;
import kz.dev.spi.persistence.UserRepository;
import kz.dev.spi.security.SecurityContextPort;
import kz.dev.usecase.auth.AuthDomainService;
import kz.dev.usecase.auth.OtpDomainService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EntityScan("kz.dev")
@EnableJpaRepositories("kz.dev")
@EnableConfigurationProperties(JwtProperties.class)
public class AppConfig {

    @Bean
    public OtpDomainService otpDomainService(UserRepository userRepository, OtpPort otpPort) {
        return new OtpDomainService(userRepository, otpPort);
    }

    @Bean
    public AuthDomainService authDomainService(UserRepository userRepository,
                                               TokenRepository tokenRepository,
                                               PasswordEncoder passwordEncoder,
                                               TokenGenerator tokenGenerator,
                                               SecurityContextPort securityContextPort,
                                               OtpPort otpPort) {
        return new AuthDomainService(userRepository, tokenRepository, passwordEncoder, tokenGenerator, securityContextPort, otpPort);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            ValidateTokenSpi validateTokenSpi,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        return new JwtAuthenticationFilter(validateTokenSpi, exceptionResolver);
    }

    @Bean
    public JwtTokenGeneratorAdapter jwtTokenGeneratorAdapter(JwtProperties jwtProperties) {
        return new JwtTokenGeneratorAdapter(jwtProperties);
    }
}
