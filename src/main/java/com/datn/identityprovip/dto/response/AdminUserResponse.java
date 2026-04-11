package com.datn.identityprovip.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserResponse extends UserResponse {
    int failedAttemptCount;
    Instant lockedUntil;
    boolean isMfaVerified;
    Instant updatedAt;
    Instant deletedAt;
    boolean deletedByAdmin;
    String banReason;
}
