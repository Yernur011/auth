package kz.dev.spi.adapter.jpa.adapter;

import jakarta.transaction.Transactional;
import kz.dev.core.model.Token;
import kz.dev.core.model.TokenType;
import kz.dev.spi.adapter.jpa.entity.TokenJpaEntity;
import kz.dev.spi.adapter.jpa.mapper.TokenPersistenceMapper;
import kz.dev.spi.adapter.jpa.repository.TokenJpaRepository;
import kz.dev.spi.persistence.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryAdapter implements TokenRepository {

    private final TokenJpaRepository jpaRepository;
    private final TokenPersistenceMapper mapper;

    @Override
    public Token save(Token token) {
        TokenJpaEntity entity = jpaRepository.findByValue(token.getValue())
                .map(existing -> {
                    existing.setRevoked(token.isRevoked());
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(token));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Token> findByValue(String value) {
        return jpaRepository.findByValue(value).map(mapper::toDomain);
    }

    @Override
    public Optional<Token> findByValueAndType(String value, TokenType type) {
        return jpaRepository.findByValueAndType(value, type).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        jpaRepository.revokeAllByUserId(userId);
    }
}
