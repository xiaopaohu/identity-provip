package com.datn.identityprovip.dto.response;

import com.datn.identityprovip.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicProfileResponse {
    String nickname;
    Gender gender;
    String avatarUrl;
    String membershipLevel;
}
