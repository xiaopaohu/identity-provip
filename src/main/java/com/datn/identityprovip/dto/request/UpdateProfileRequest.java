package com.datn.identityprovip.dto.request;

import com.datn.identityprovip.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProfileRequest {
    String fullName;
    String nickname;
    Gender gender;
    LocalDate dob;
    String avatarUrl;
}
