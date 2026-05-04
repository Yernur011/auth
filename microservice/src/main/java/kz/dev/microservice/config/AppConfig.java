package kz.dev.microservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.dev.spi.adapter.kafka.KafkaEventAdapter;
import kz.dev.spi.adapter.redis.RedisOtpAdapter;
import kz.dev.spi.adapter.security.configuration.JwtAuthenticationFilter;
import kz.dev.spi.adapter.security.configuration.JwtProperties;
import kz.dev.spi.adapter.security.service.JwtTokenGeneratorAdapter;
import kz.dev.spi.auth.PasswordEncoder;
import kz.dev.spi.auth.TokenGenerator;
import kz.dev.spi.event.SendEventSpi;
import kz.dev.spi.persistence.TokenRepository;
import kz.dev.spi.auth.ValidateTokenSpi;
import kz.dev.spi.persistence.UserRepository;
import kz.dev.spi.security.SecurityContextPort;
import kz.dev.usecase.auth.AuthDomainService;
import kz.dev.usecase.auth.OtpDomainService;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.internals.Topic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.sql.DataSource;

@Configuration
@EntityScan("kz.dev")
@EnableJpaRepositories("kz.dev")
public class AppConfig {

    @Bean
    public AuthDomainService authDomainService(UserRepository userRepository,
                                               TokenRepository tokenRepository,
                                               PasswordEncoder passwordEncoder,
                                               TokenGenerator tokenGenerator,
                                               SecurityContextPort securityContextPort,
                                               RedisOtpAdapter otpPort) {
        return new AuthDomainService(userRepository, tokenRepository, passwordEncoder, tokenGenerator, securityContextPort, otpPort);
    }


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            ValidateTokenSpi validateTokenSpi,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        return new JwtAuthenticationFilter(validateTokenSpi, exceptionResolver);
    }
    @Bean
    public JwtProperties jwtProperties(@Value(value = "${app.jwt.secret}") String secret,
                                       @Value(value = "${app.jwt.access-ttl-seconds}") long accessTtlSeconds,
                                       @Value(value = "${app.jwt.refresh-ttl-seconds}") long refreshTtlSeconds) {
        return new JwtProperties(secret, accessTtlSeconds, refreshTtlSeconds);
    }
    @Bean
    public JwtTokenGeneratorAdapter jwtTokenGeneratorAdapter(JwtProperties jwtProperties) {
        return new JwtTokenGeneratorAdapter(jwtProperties);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SendEventSpi kafkaEventAdapter(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        return new KafkaEventAdapter(kafkaTemplate, objectMapper);
    }

    @Bean
    public OtpDomainService otpDomainService(UserRepository userRepository,
                                             SendEventSpi sendEventSpi,
                                             @Value("${app.kafka.topics.otp-request}") String otpTopic,
                                             RedisOtpAdapter otpRedisAdapter) {
        return new OtpDomainService(userRepository, sendEventSpi, otpRedisAdapter, otpTopic);
    }

    @Bean
    public NewTopic otpTopic(@Value("${app.kafka.topics.otp-request}") String otpTopic,
                          @Value("${app.kafka.topics.partitions}") Integer partitions,
                          @Value("${app.kafka.topics.replicas}") Integer replicas){

        return TopicBuilder.name(otpTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public RedisOtpAdapter otpRedisAdapter(StringRedisTemplate stringRedisTemplate) {
        return new RedisOtpAdapter(stringRedisTemplate);
    }
//
//    @Bean(initMethod = "migrate")
//    public Flyway flyway(DataSource dataSource) {
//        FluentConfiguration config = Flyway.configure()
//                .dataSource(dataSource)
//                .locations("classpath:db/migration")
////                .baselineOnMigrate(true)   // если база не пустая
////                .outOfOrder(false)         // запрет "прыжков" по версиям
//                .validateOnMigrate(true)   // проверка миграций
//                .cleanDisabled(true);     // запрет clean в проде
//
//        return new Flyway(config);
//    }

}
