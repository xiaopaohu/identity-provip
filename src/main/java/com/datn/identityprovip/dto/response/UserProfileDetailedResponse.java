package com.datn.identityprovip.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileDetailedResponse {
    String email;
    String phoneNumber;
    List<String> roles;
    boolean isAccountLocked;
    LocalDateTime createdAt;
}
