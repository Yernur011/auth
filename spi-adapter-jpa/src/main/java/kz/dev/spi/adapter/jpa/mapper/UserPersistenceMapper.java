package kz.dev.spi.adapter.jpa.mapper;

import kz.dev.core.model.User;
import kz.dev.spi.adapter.jpa.entity.UserJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserPersistenceMapper {

    User toDomain(UserJpaEntity entity);

    UserJpaEntity toEntity(User user);

}
