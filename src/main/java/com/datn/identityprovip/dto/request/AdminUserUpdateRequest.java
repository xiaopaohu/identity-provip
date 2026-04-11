package com.datn.identityprovip.dto.request;

import com.datn.identityprovip.enums.UserStatus;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserUpdateRequest {
    @Size(min = 8, message = "PASSWORD_TOO_SHORT")
    String password;

    UserStatus status;

    Boolean twoFactorEnabled;
    Boolean isMfaVerified;

    Integer failedAttemptCount;
    Instant lockedUntil;
    String banReason;

    Set<String> roles;
}
