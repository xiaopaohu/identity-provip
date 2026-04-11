package com.datn.identityprovip.service;

import com.datn.identityprovip.dto.request.*;
import com.datn.identityprovip.dto.response.ApiResponse;
import com.datn.identityprovip.dto.response.AuthenticationResponse;
import com.datn.identityprovip.dto.response.IntrospectResponse;
import com.datn.identityprovip.dto.response.VerificationResponse;
import com.datn.identityprovip.enums.VerificationType;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {
    ApiResponse<Object> register(RegisterRequest request);
//    AuthenticationResponse verify(VerifyRequest request);
    VerificationResponse verify(VerifyRequest request);
    ApiResponse<Object> resendVerification(String identifier, VerificationType type);

    // --- LUỒNG ĐĂNG NHẬP & TOKEN ---
    AuthenticationResponse authenticate(AuthenticationRequest request);
    AuthenticationResponse refreshToken(RefreshTokenRequest request);
    IntrospectResponse introspect(IntrospectRequest request);
    void logout(LogoutRequest request) throws ParseException, JOSEException;

    // --- LUỒNG QUÊN MẬT KHẨU (FORGOT PASSWORD) ---
//    void forgotPassword(String identifier);
//    String verifyResetCode(VerifyRequest request);
//    void resetPassword(ResetPasswordRequest request);

    ApiResponse<Object> forgotPassword(String identifier);
    ApiResponse<Object> sendOtpForReset(String identifier, String method);
    void resetPassword(ResetPasswordRequest request);
}
