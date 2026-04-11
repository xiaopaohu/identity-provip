package com.datn.identityprovip.controller;

import com.datn.identityprovip.dto.request.*;
import com.datn.identityprovip.dto.response.ApiResponse;
import com.datn.identityprovip.dto.response.AuthenticationResponse;
import com.datn.identityprovip.dto.response.IntrospectResponse;
import com.datn.identityprovip.dto.response.VerificationResponse;
import com.datn.identityprovip.enums.VerificationType;
import com.datn.identityprovip.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/register")
    public ApiResponse<Object> register(@RequestBody @Valid RegisterRequest request) {
        return authenticationService.register(request);
    }

    @PostMapping("/verify")
    public ApiResponse<VerificationResponse> verify(@RequestBody @Valid VerifyRequest request) {
        VerificationResponse result = authenticationService.verify(request);
        return ApiResponse.success(result, "Xác thực thành công!");
    }

    @PostMapping("/resend")
    public ApiResponse<Object> resend(@RequestBody @Valid ResendRequest request) {
        // Dùng chung cho cả luồng đăng ký lỡ tay tắt máy, hoặc luồng quên mật khẩu
        return authenticationService.resendVerification(request.getIdentifier(), request.getType());
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        return ApiResponse.success(authenticationService.authenticate(request), "Đăng nhập thành công!");
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.success(authenticationService.refreshToken(request), "Làm mới token thành công!");
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody @Valid IntrospectRequest request) {
        return ApiResponse.success(authenticationService.introspect(request), "Token hợp lệ.");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.success(null, "Đăng xuất thành công!");
    }

    @PostMapping("/forgot-password/find-account")
    public ApiResponse<Object> findAccount(@RequestBody Map<String, String> request) {
        return authenticationService.forgotPassword(request.get("identifier"));
    }

    @PostMapping("/forgot-password/send-otp")
    public ApiResponse<Object> sendOtpForReset(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        String method = request.get("method");

        return authenticationService.sendOtpForReset(identifier, method);
    }

    @PostMapping("/forgot-password/reset")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ApiResponse.success(null, "Mật khẩu của bạn đã được thay đổi thành công!");
    }
}
