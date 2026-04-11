package com.datn.identityprovip.mapper;
import com.datn.identityprovip.dto.response.AuthenticationResponse;
import com.datn.identityprovip.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthenticationMapper {

    @Mapping(target = "accessToken", source = "token")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "authenticated", source = "isAuthenticated")
    @Mapping(target = "mfaRequired", source = "user.twoFactorEnabled")
//    @Mapping(target = "isMfaVerified", source = "isMfaVerified")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "status", source = "user.status")
    AuthenticationResponse toAuthenticationResponse(
            User user,
            String token,
            String refreshToken,
            boolean isAuthenticated
    );
}

