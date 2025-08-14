package org.example.be17pickcook.domain.user.mapper;

import org.example.be17pickcook.domain.user.model.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.example.be17pickcook.domain.user.model.User;

import java.util.Map;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface UserMapper {

    // 회원가입 DTO → Entity 매핑
    @Mapping(target = "idx", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "emailVerifyList", ignore = true)
    // enabled, role은 Entity의 @Builder.Default로 처리됨
    User registerDtoToEntity(UserDto.Register dto);

    // Entity → AuthUser DTO 매핑
    @Mapping(target = "attributes", ignore = true)
    UserDto.AuthUser entityToAuthUser(User entity);

    // OAuth2 사용자 생성용 매핑
    @Mapping(target = "idx", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "emailVerifyList", ignore = true)
    User createOAuth2User(String email, String nickname);

    @Mapping(target = "idx", source = "entity.idx")
    @Mapping(target = "email", source = "entity.email")
    @Mapping(target = "password", source = "entity.password")
    @Mapping(target = "nickname", source = "entity.nickname")
    @Mapping(target = "enabled", source = "entity.enabled")
    @Mapping(target = "attributes", source = "attributes")
    UserDto.AuthUser entityToAuthUserWithAttributes(User entity, Map<String, Object> attributes);

    UserDto.Response authUserToResponse(UserDto.AuthUser authUser);
}
