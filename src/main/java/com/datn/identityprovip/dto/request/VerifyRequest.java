package com.datn.identityprovip.dto.request;

import com.datn.identityprovip.enums.VerificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyRequest {

    @NotBlank(message = "IDENTIFIER_REQUIRED")
    String identifier;

    @NotBlank(message = "CODE_REQUIRED")
    String code;

    @NotNull(message = "VERIFICATION_TYPE_REQUIRED")
    VerificationType type;
}