package kz.dev.microservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.dev.spi.adapter.security.service.JwtStatelessTokenRepository;
import kz.dev.spi.adapter.kafka.KafkaEventAdapter;
import kz.dev.spi.adapter.redis.OpaqueValidateTokenAdapter;
import kz.dev.spi.adapter.redis.RedisOtpAdapter;
import kz.dev.spi.adapter.redis.RedisTokenRepository;
import kz.dev.spi.adapter.security.configuration.JwtAuthenticationFilter;
import kz.dev.spi.adapter.security.configuration.JwtProperties;
import kz.dev.spi.adapter.security.service.JwtTokenGeneratorAdapter;
import kz.dev.spi.adapter.security.service.JwtValidateTokenAdapter;
import kz.dev.spi.adapter.security.service.OpaqueTokenGeneratorAdapter;
import kz.dev.spi.auth.PasswordEncoder;
import kz.dev.spi.auth.TokenGenerator;
import kz.dev.spi.auth.ValidateTokenSpi;
import kz.dev.spi.event.SendEventSpi;
import kz.dev.spi.persistence.TokenRepository;
import kz.dev.spi.persistence.UserRepository;
import kz.dev.spi.security.SecurityContextPort;
import kz.dev.usecase.auth.AuthDomainService;
import kz.dev.usecase.auth.OtpDomainService;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EntityScan("kz.dev")
@EnableJpaRepositories("kz.dev")
public class AppConfig {

    // ── Token strategy: jwt (stateless) or opaque (Redis) ───────────────────

    @Bean
    @Profile("jwt")
    public TokenGenerator jwtTokenGenerator(JwtProperties jwtProperties) {
        return new JwtTokenGeneratorAdapter(jwtProperties);
    }

    @Bean
    @Profile("jwt")
    public TokenRepository jwtTokenRepository(JwtProperties jwtProperties) {
        return new JwtStatelessTokenRepository(jwtProperties);
    }

    @Bean
    @Profile("jwt")
    public ValidateTokenSpi jwtValidateToken(JwtProperties jwtProperties,
                                              UserRepository userRepository) {
        return new JwtValidateTokenAdapter(jwtProperties, userRepository);
    }

    @Bean
    @Profile("opaque")
    public TokenGenerator opaqueTokenGenerator(
            @Value("${app.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${app.jwt.refresh-ttl-seconds}") long refreshTtlSeconds) {
        return new OpaqueTokenGeneratorAdapter(accessTtlSeconds, refreshTtlSeconds);
    }

    @Bean
    @Profile("opaque")
    public TokenRepository redisTokenRepository(StringRedisTemplate redisTemplate) {
        return new RedisTokenRepository(redisTemplate);
    }

    @Bean
    @Profile("opaque")
    public ValidateTokenSpi opaqueValidateToken(TokenRepository tokenRepository,
                                                 UserRepository userRepository) {
        return new OpaqueValidateTokenAdapter(tokenRepository, userRepository);
    }

    // ── Domain services ──────────────────────────────────────────────────────

    @Bean
    public AuthDomainService authDomainService(UserRepository userRepository,
                                               TokenRepository tokenRepository,
                                               PasswordEncoder passwordEncoder,
                                               TokenGenerator tokenGenerator,
                                               SecurityContextPort securityContextPort,
                                               RedisOtpAdapter otpPort) {
        return new AuthDomainService(userRepository, tokenRepository, passwordEncoder,
                tokenGenerator, securityContextPort, otpPort);
    }

    @Bean
    public OtpDomainService otpDomainService(UserRepository userRepository,
                                             SendEventSpi sendEventSpi,
                                             @Value("${app.kafka.topics.otp-request}") String otpTopic,
                                             RedisOtpAdapter otpRedisAdapter) {
        return new OtpDomainService(userRepository, sendEventSpi, otpRedisAdapter, otpTopic);
    }

    // ── Security ─────────────────────────────────────────────────────────────

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            ValidateTokenSpi validateTokenSpi,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        return new JwtAuthenticationFilter(validateTokenSpi, exceptionResolver);
    }

    @Bean
    public JwtProperties jwtProperties(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${app.jwt.refresh-ttl-seconds}") long refreshTtlSeconds) {
        return new JwtProperties(secret, accessTtlSeconds, refreshTtlSeconds);
    }

    // ── Infrastructure ───────────────────────────────────────────────────────

    @Bean
    public RedisOtpAdapter otpRedisAdapter(StringRedisTemplate redisTemplate) {
        return new RedisOtpAdapter(redisTemplate);
    }

    @Bean
    public SendEventSpi kafkaEventAdapter(KafkaTemplate<String, String> kafkaTemplate,
                                          ObjectMapper objectMapper) {
        return new KafkaEventAdapter(kafkaTemplate, objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public NewTopic otpTopic(@Value("${app.kafka.topics.otp-request}") String otpTopic,
                             @Value("${app.kafka.topics.partitions}") Integer partitions,
                             @Value("${app.kafka.topics.replicas}") Integer replicas) {
        return TopicBuilder.name(otpTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
