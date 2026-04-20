package kz.dev.spi.adapter.jpa.mapper;

import kz.dev.core.model.Token;
import kz.dev.spi.adapter.jpa.entity.TokenJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TokenPersistenceMapper {

    default Token toDomain(TokenJpaEntity entity) {
        return new Token(
                entity.getValue(),
                entity.getUserId(),
                entity.getType(),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.isRevoked()
        );
    }

    TokenJpaEntity toEntity(Token token);
}
