package com.datn.identityprovip.dto.response;

import com.datn.identityprovip.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {
    String accessToken;
    String refreshToken;
    boolean authenticated;
    @Builder.Default
    boolean mfaRequired = false;
    boolean isMfaVerified;
    UserStatus status;
    String userId;
    String email;
    String phone;
}
