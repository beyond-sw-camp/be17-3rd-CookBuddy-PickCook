package org.example.be17pickcook.domain.user.mapper;

import org.example.be17pickcook.domain.user.model.UserDto;
import org.mapstruct.*;
import org.example.be17pickcook.domain.user.model.User;

import java.util.Map;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE // 추가!
)
public interface UserMapper {

    // Entity → Response DTO 매핑
    @Mapping(target = "idx", source = "idx")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "profileImage", source = "profileImage")
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "detailAddress", source = "detailAddress")
    UserDto.Response entityToResponse(User entity);

    // UpdateProfile DTO를 사용해서 기존 Entity 업데이트
    @Mapping(target = "idx", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "emailVerifyList", ignore = true)
    @Mapping(target = "profileImage", ignore = true) // 추가: 프로필 이미지는 별도 처리
    @Mapping(target = "name", source = "name") // 추가: 명시적 매핑
    @Mapping(target = "nickname", source = "nickname") // 추가: 명시적 매핑
    @Mapping(target = "phone", source = "phone") // 추가: 명시적 매핑
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "detailAddress", source = "detailAddress")
    void updateEntityFromDto(@MappingTarget User entity, UserDto.UpdateProfile dto);

    // 회원가입 DTO → Entity 매핑
    @Mapping(target = "idx", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "emailVerifyList", ignore = true)
    @Mapping(target = "passwordResetList", ignore = true) // 🔧 추가
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "detailAddress", source = "detailAddress")
    User registerDtoToEntity(UserDto.Register dto);

    // 수정: Entity → AuthUser DTO 매핑 (모든 필드 포함)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "idx", source = "idx")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "name", source = "name")           // 추가: name 필드 매핑
    @Mapping(target = "phone", source = "phone")         // 추가: phone 필드 매핑
    @Mapping(target = "profileImage", source = "profileImage") // 추가: profileImage 필드 매핑
    @Mapping(target = "zipCode", source = "zipCode")           // 🔧 추가
    @Mapping(target = "address", source = "address")           // 🔧 추가
    @Mapping(target = "detailAddress", source = "detailAddress") // 🔧 추가
    @Mapping(target = "enabled", source = "enabled")
    UserDto.AuthUser entityToAuthUser(User entity);

    // OAuth2 사용자 생성용 매핑
    @Mapping(target = "idx", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "emailVerifyList", ignore = true)
    @Mapping(target = "passwordResetList", ignore = true) // 🔧 추가
    @Mapping(target = "zipCode", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "detailAddress", ignore = true)
    User createOAuth2User(String email, String nickname);

    // 수정: OAuth2 전용 매핑 - 명시적 소스 지정
    @Mapping(target = "idx", source = "entity.idx")
    @Mapping(target = "email", source = "entity.email")
    @Mapping(target = "password", source = "entity.password")
    @Mapping(target = "nickname", source = "entity.nickname")
    @Mapping(target = "name", source = "entity.name")                    // 수정: entity.name 사용
    @Mapping(target = "phone", source = "entity.phone")
    @Mapping(target = "profileImage", source = "entity.profileImage")
    @Mapping(target = "zipCode", source = "entity.zipCode")           // 🔧 추가: entity.zipCode 명시
    @Mapping(target = "address", source = "entity.address")           // 🔧 추가: entity.address 명시
    @Mapping(target = "detailAddress", source = "entity.detailAddress") // 🔧 추가: entity.detailAddress 명시
    @Mapping(target = "enabled", source = "entity.enabled")
    @Mapping(target = "attributes", source = "attributes")
    UserDto.AuthUser entityToAuthUserWithAttributes(User entity, Map<String, Object> attributes);

    // 수정: AuthUser → Response 매핑 (모든 필드 명시적 매핑)
    @Mapping(target = "idx", source = "idx")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "name", source = "name")           // 추가: name 필드 매핑
    @Mapping(target = "phone", source = "phone")         // 추가: phone 필드 매핑
    @Mapping(target = "profileImage", source = "profileImage") // 추가: profileImage 필드 매핑
    @Mapping(target = "zipCode", source = "zipCode")           // 🔧 추가
    @Mapping(target = "address", source = "address")           // 🔧 추가
    @Mapping(target = "detailAddress", source = "detailAddress") // 🔧 추가
    UserDto.Response authUserToResponse(UserDto.AuthUser authUser);
}