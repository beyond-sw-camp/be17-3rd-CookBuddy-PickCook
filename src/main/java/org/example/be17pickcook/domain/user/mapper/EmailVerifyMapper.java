package org.example.be17pickcook.domain.user.mapper;

import org.example.be17pickcook.domain.user.model.EmailVerify;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.example.be17pickcook.domain.user.model.User;

@Mapper(componentModel = "spring")
public interface EmailVerifyMapper {

    @Mapping(target = "idx", ignore = true)
        // expiresAt은 Entity의 @Builder.Default로 자동 처리되므로 제거 가능
    EmailVerify createEmailVerify(String uuid, User user);

}
