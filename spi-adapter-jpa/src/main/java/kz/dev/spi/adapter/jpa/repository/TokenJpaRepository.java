package kz.dev.spi.adapter.jpa.repository;

import kz.dev.core.model.TokenType;
import kz.dev.spi.adapter.jpa.entity.TokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TokenJpaRepository extends JpaRepository<TokenJpaEntity, UUID> {
    Optional<TokenJpaEntity> findByValue(String value);
    Optional<TokenJpaEntity> findByValueAndType(String value, TokenType type);

    @Modifying
    @Query("UPDATE TokenJpaEntity t SET t.revoked = true WHERE t.userId = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId);
}
