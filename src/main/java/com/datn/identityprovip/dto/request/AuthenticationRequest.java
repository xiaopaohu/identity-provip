package com.datn.identityprovip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @NotBlank(message = "IDENTIFIER_REQUIRED")
    String identifier;

    @NotBlank(message = "PASSWORD_REQUIRED")
    String password;

    boolean rememberMe;
}
