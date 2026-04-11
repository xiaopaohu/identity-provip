package com.datn.identityprovip.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationResponse {
    boolean verified;
    String resetToken;
    AuthenticationResponse auth;
    Map<String, Object> metadata;
}
