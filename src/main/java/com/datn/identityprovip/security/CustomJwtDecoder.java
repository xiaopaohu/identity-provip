package com.datn.identityprovip.security;

import com.datn.identityprovip.dto.request.IntrospectRequest;
import com.datn.identityprovip.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

@Slf4j
@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    @Lazy
    private AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        log.info("Token received for decoding: {}", token);
        String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        var response = authenticationService.introspect(
                IntrospectRequest.builder()
                        .token(actualToken)
                        .build()
        );

        if (!response.isValid()) {
            throw new BadJwtException("Token invalid or revoked");
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS256");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
        }

        try {
            return nimbusJwtDecoder.decode(actualToken);
        } catch (Exception e) {
            throw new BadJwtException("Decode failed: " + e.getMessage());
        }
    }
}