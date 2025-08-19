package org.example.be17pickcook.domain.user.mapper;

import org.example.be17pickcook.domain.user.model.UserDto;
import org.mapstruct.*;
import org.example.be17pickcook.domain.user.model.User;

import java.util.Map;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    // Entity → Response DTO 매핑 (자동 매핑 활용)
    UserDto.Response entityToResponse(User entity);

    // UpdateProfile DTO를 사용해서 기존 Entity 업데이트
    @Mapping(target = "idx", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "emailVerifyList", ignore = true)
    @Mapping(target = "passwordResetList", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    void updateEntityFromDto(@MappingTarget User entity, UserDto.UpdateProfile dto);

    // 회원가입 DTO → Entity 매핑 (자동 매핑 활용)
    @Mapping(target = "idx", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "emailVerifyList", ignore = true)
    @Mapping(target = "passwordResetList", ignore = true)
    User registerDtoToEntity(UserDto.Register dto);

    // Entity → AuthUser DTO 매핑 (필수 매핑만 명시)
    @Mapping(target = "attributes", ignore = true)
    UserDto.AuthUser entityToAuthUser(User entity);

    // OAuth2 사용자 생성용 매핑
    @Mapping(target = "idx", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "name", source = "nickname")        // nickname을 name으로 매핑
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "emailVerifyList", ignore = true)
    @Mapping(target = "passwordResetList", ignore = true)
    @Mapping(target = "zipCode", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "detailAddress", ignore = true)
    User createOAuth2User(String email, String nickname);

    // OAuth2 전용 매핑 (default 메서드로 구현)
    default UserDto.AuthUser entityToAuthUserWithAttributes(User entity, Map<String, Object> attributes) {
        UserDto.AuthUser authUser = entityToAuthUser(entity);
        return authUser.toBuilder()
                .attributes(attributes)
                .build();
    }

    // AuthUser → Response 매핑 (자동 매핑 활용)
    UserDto.Response authUserToResponse(UserDto.AuthUser authUser);
}