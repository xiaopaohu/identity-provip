package com.datn.identityprovip.service.impl;

import com.datn.identityprovip.dto.event.NotificationEvent;
import com.datn.identityprovip.dto.payload.SecurityCodePayload;
import com.datn.identityprovip.dto.request.*;
import com.datn.identityprovip.dto.response.ApiResponse;
import com.datn.identityprovip.dto.response.AuthenticationResponse;
import com.datn.identityprovip.dto.response.IntrospectResponse;
import com.datn.identityprovip.dto.response.VerificationResponse;
import com.datn.identityprovip.entity.*;
import com.datn.identityprovip.enums.UserStatus;
import com.datn.identityprovip.enums.VerificationType;
import com.datn.identityprovip.exception.AppException;
import com.datn.identityprovip.exception.ErrorCode;
import com.datn.identityprovip.mapper.AuthenticationMapper;
import com.datn.identityprovip.mapper.UserMapper;
import com.datn.identityprovip.repository.*;
import com.datn.identityprovip.service.AuthenticationService;
import com.datn.identityprovip.service.JwtProvider;
import com.datn.identityprovip.service.OtpLockoutManager;
import com.datn.identityprovip.validator.RegisterValidator;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    VerificationOtpCodeRepository verificationOtpCodeRepository;
    VerificationTokenRepository verificationTokenRepository;
    RefreshTokenRepository refreshTokenRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    RegisterValidator registerValidator;

    UserMapper userMapper;
    AuthenticationMapper authenticationMapper;
    PasswordEncoder passwordEncoder;
    JwtProvider jwtProvider;
    ApplicationEventPublisher eventPublisher;

    OtpLockoutManager otpLockoutManager;
//    PromotionService promotionService;

    @Override
    @Transactional
    public ApiResponse<Object> register(RegisterRequest request) {
        registerValidator.validate(request);

        String identifier = request.getIdentifier().trim();
        boolean isEmail = identifier.contains("@");
        if (isEmail) {
            if (userRepository.existsByEmail(identifier))
                throw new AppException(ErrorCode.EMAIL_EXISTED);
        } else {
            if (userRepository.existsByPhone(identifier))
                throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (isEmail) {
            user.setEmail(identifier);
        } else {
            user.setPhone(identifier);
        }

        user.setStatus(UserStatus.UNVERIFIED);
        Role role = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        user.addRole(role);
        userRepository.save(user);

        SecurityCodePayload payload = generateSecurityCode(user, identifier, VerificationType.REGISTER);

        eventPublisher.publishEvent(NotificationEvent.builder()
                .identifier(identifier)
                .code(payload.code())
                .type(VerificationType.REGISTER)
                .build());
        return ApiResponse.<Object>success(null, "Đăng ký thành công. Vui lòng kiểm tra mã xác thực.")
                .toBuilder()
                .metadata(payload.metadata())
                .build();
    }

    @Override
    public ApiResponse<Object> forgotPassword(String identifier) {
        String cleanId = identifier.trim();
        User user = userRepository.findByEmailOrPhone(cleanId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Map<String, String> options = new HashMap<>();
        if (user.getEmail() != null) options.put("email", maskEmail(user.getEmail()));
        if (user.getPhone() != null) options.put("phone", maskPhone(user.getPhone()));

        return ApiResponse.success(options, "Vui lòng chọn phương thức nhận mã xác thực.");
    }

    @Override
    @Transactional
    public ApiResponse<Object> sendOtpForReset(String identifier, String method) {
        User user = userRepository.findByEmailOrPhone(identifier)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String target = method.equalsIgnoreCase("EMAIL") ? user.getEmail() : user.getPhone();
        if (target == null) throw new AppException(ErrorCode.INVALID_RECOVERY_METHOD);

        // 3. Gọi hàm generate thần thánh của mày
        // Nó sẽ tự động rơi vào nhánh "else" (OTP) vì type không phải là REGISTER
        SecurityCodePayload payload = generateSecurityCode(user, target, VerificationType.FORGOT_PASSWORD);

        // 4. Bắn Event để hệ thống gửi Mail/SMS thực tế
        eventPublisher.publishEvent(NotificationEvent.builder()
                .identifier(target)
                .code(payload.code())
                .type(VerificationType.FORGOT_PASSWORD)
                .build());

        return ApiResponse.success(null, "Mã xác thực đã được gửi.")
                .toBuilder()
                .metadata(payload.metadata())
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        VerificationToken tokenEntity = verificationTokenRepository.findByToken(request.getResetToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (tokenEntity.getType() != VerificationType.FORGOT_PASSWORD) throw new AppException(ErrorCode.INVALID_TOKEN);
        if (tokenEntity.getExpiryAt().isBefore(Instant.now())) {
            verificationTokenRepository.delete(tokenEntity);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = tokenEntity.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        verificationTokenRepository.delete(tokenEntity);
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    public VerificationResponse verify(VerifyRequest request) {
        String identifier = request.getIdentifier().trim();
        String code = request.getCode().trim();
        VerificationType type = request.getType();
        Instant now = Instant.now();

        User user = userRepository.findByEmailOrPhone(identifier)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (type == VerificationType.REGISTER && user.getStatus() == UserStatus.ACTIVE) {
            return VerificationResponse.builder()
                    .verified(true)
                    .auth(generateAuthenticationResponse(user))
                    .build();
        }

        boolean isEmailRegister = (type == VerificationType.REGISTER && identifier.contains("@"));

        if (isEmailRegister) {
            VerificationToken verificationToken = verificationTokenRepository.findByToken(code)
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

            if (!verificationToken.getUser().getId().equals(user.getId())) throw new AppException(ErrorCode.INVALID_TOKEN);

            if (verificationToken.getExpiryAt().isBefore(now)) {
                verificationTokenRepository.delete(verificationToken);
                throw new AppException(ErrorCode.TOKEN_EXPIRED);
            }

            handleSuccessVerification(user, type);
            verificationTokenRepository.delete(verificationToken);
        } else {
            VerificationOtpCode latestOtp = verificationOtpCodeRepository
                    .findTopByUserAndTypeOrderByCreatedAtDesc(user, type)
                    .orElseThrow(() -> new AppException(ErrorCode.NO_ACTIVE_OTP_FOUND));

            if (latestOtp.isUsed() || latestOtp.getExpiryAt().isBefore(now)) throw new AppException(ErrorCode.OTP_EXPIRED);

            if (latestOtp.getAttemptCount() >= 5) {
                latestOtp.setUsed(true);
                verificationOtpCodeRepository.save(latestOtp);
                throw new AppException(ErrorCode.OTP_MAX_ATTEMPTS_REACHED);
            }

            if (!latestOtp.getOtpCode().equals(code)) {
                int currentAttempts = otpLockoutManager.recordFailedAttempt(latestOtp);

                int remaining = Math.max(0, 5 - currentAttempts);

                if (currentAttempts >= 5) {
                    throw new AppException(ErrorCode.OTP_MAX_ATTEMPTS_REACHED, Map.of("remainingAttempts", 0));
                }
                throw new AppException(ErrorCode.INVALID_OTP, Map.of("remainingAttempts", remaining));
            }

            otpLockoutManager.markOtpUsed(latestOtp);
            handleSuccessVerification(user, type);
        }

        VerificationResponse.VerificationResponseBuilder responseBuilder = VerificationResponse.builder().verified(true);

        // PHÂN LUỒNG TRẢ VỀ THEO Ý MÀY
        if (type == VerificationType.FORGOT_PASSWORD) {
            String resetToken = UUID.randomUUID().toString();
            verificationTokenRepository.save(VerificationToken.builder()
                    .token(resetToken)
                    .user(user)
                    .type(VerificationType.FORGOT_PASSWORD)
                    .expiryAt(now.plus(15, ChronoUnit.MINUTES))
                    .build());

            return responseBuilder.resetToken(resetToken).build();
        }

        return responseBuilder.auth(generateAuthenticationResponse(user)).build();
    }


    @Override
    @Transactional
    public ApiResponse<Object> resendVerification(String identifier, VerificationType type) {
        // 1. Check User tồn tại
        User user = userRepository.findByEmailOrPhone(identifier)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Instant now = Instant.now();

        // 2. Check Lockout (Khóa do nhập sai quá nhiều lần trước đó)
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new AppException(ErrorCode.ACCOUNT_TEMPORARILY_LOCKED);
        }

        // 3. Logic chặn Spam Resend (Check xem đã đủ điều kiện gửi lại chưa)
        validateResendCondition(user, identifier, type, now);

        // 4. Triệu hồi "nhà máy" gen code (Đã xử lý Token/OTP bên trong)
        SecurityCodePayload payload = generateSecurityCode(user, identifier, type);

        // 5. Bắn Event gửi thông báo
        eventPublisher.publishEvent(NotificationEvent.builder()
                .identifier(identifier)
                .code(payload.code())
                .type(type)
                .build());

        log.info("Đã gửi lại mã/link xác thực ({}) cho: {}", type, identifier);

        // 6. Trả về Metadata cho FE
        return ApiResponse.<Object>success(null, "Mã xác thực mới đã được gửi.")
                .toBuilder()
                .metadata(payload.metadata())
                .build();
    }

    // --- LUỒNG ĐĂNG NHẬP & TOKEN ---
    @Override
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1. Tìm User
        User user = userRepository.findByEmailOrPhone(request.getIdentifier())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Check Password
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!authenticated) {
            processFailedAttempt(user); // Hàm này mày giữ nguyên để tăng count sai pass
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. Check các trạng thái "Chết" (Banned, Deleted, Locked)
        checkUserStatus(user);

        // 4. Check XÁC THỰC (UNVERIFIED) - Chỗ này mày cần nhất nè
        if (user.getStatus() == UserStatus.UNVERIFIED) {
            handleUnverifiedUser(user, request.getIdentifier());
        }

        // 5. Nếu mọi thứ OK -> Reset số lần sai pass và trả về Token
        resetFailedAttempts(user);
        return generateAuthenticationResponse(user);
    }

    @Override
    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        // 1. Tìm Token trong DB
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        Instant now = Instant.now();

        // 2. Kiểm tra: Đã bị thu hồi (revoked) chưa? Đã hết hạn (expires_at) chưa?
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(now)) {
            // Nếu token không còn hợp lệ, xóa luôn cho sạch DB (hoặc giữ lại tùy mày)
            refreshTokenRepository.delete(refreshToken);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        // 3. Lấy User để tạo cặp Token mới
        User user = refreshToken.getUser();

        // 4. Vô hiệu hóa Token cũ (Revoke)
        // Thay vì xóa ngay, mình set revoked = true để đánh dấu nó đã được dùng
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // 5. Trả về cặp Token mới (Hàm này sẽ save 1 bản ghi RefreshToken mới vào DB)
        return generateAuthenticationResponse(user);
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request){
        var token = request.getToken();
        boolean isValid = true;
        String scope = null;
        String subject = null;

        try {
            SignedJWT signedJWT = jwtProvider.verifyToken(token);

            var claimsSet = signedJWT.getJWTClaimsSet();
            String jid = claimsSet.getJWTID();

            if (invalidatedTokenRepository.existsById(jid)) {
                isValid = false;
            }

            if (isValid) {
                scope = claimsSet.getStringClaim("scope");
                subject = claimsSet.getSubject();
            }
        } catch (Exception e) {
            isValid = false;
            log.error("Introspect failed: {}", e.getMessage());
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .scope(scope)
                .subject(subject)
                .build();
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException{
        try {
            var signedToken = jwtProvider.verifyToken(request.getToken());

            String jid = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jid)
                    .expiryAt(expiryTime.toInstant())
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);

        } catch (Exception e) {
            log.info("Token process failed or already expired: {}", e.getMessage());
        }
    }

    // ================= HELPER METHODS (PRIVATE) =================

    private SecurityCodePayload generateSecurityCode(User user, String identifier, VerificationType type) {
        boolean isEmail = identifier.contains("@");
        Instant now = Instant.now();
        boolean isEmailRegister = (type == VerificationType.REGISTER && isEmail);

        String maskedTarget = isEmail ? maskEmail(identifier) : maskPhone(identifier);

        if (isEmailRegister) {
            verificationTokenRepository.invalidateAllActiveTokens(user, type);
            String token = UUID.randomUUID().toString();
            long expirySeconds = 86400;

            verificationTokenRepository.save(VerificationToken.builder()
                    .token(token)
                    .user(user)
                    .type(type)
                    .expiryAt(now.plusSeconds(expirySeconds))
                    .build());

            Map<String, Object> meta = new HashMap<>();
            meta.put("target", maskedTarget);
            meta.put("remainingSeconds", expirySeconds);
            meta.put("type", "TOKEN");

            return new SecurityCodePayload(token, meta);
        }
        else {
            // --- LOGIC XỬ LÝ OTP (Cho Phone hoặc Forgot Password Email) ---
            VerificationOtpCode latestOtp = verificationOtpCodeRepository
                    .findTopByUserAndTypeOrderByCreatedAtDesc(user, type)
                    .orElse(null);

            // Check xem có đang bị khóa do nhập sai > 5 lần không
            if (latestOtp != null && latestOtp.getAttemptCount() >= 5 && latestOtp.getExpiryAt().isAfter(now)) {
                long remainingSeconds = Duration.between(now, latestOtp.getExpiryAt()).getSeconds();
                throw new AppException(ErrorCode.OTP_LOCKED, Map.of(
                        "remainingSeconds", Math.max(0, remainingSeconds),
                        "remainingAttempts", 0
                ));
            }

            // Vô hiệu hóa mã cũ
            verificationOtpCodeRepository.invalidateAllActiveOtp(user, type);

            // Tạo OTP mới
            String otpCode = String.format("%06d", new SecureRandom().nextInt(900000) + 100000);
            int resendCount = (latestOtp != null) ? latestOtp.getResendCount() + 1 : 1;
            long expirySeconds = 120;

            verificationOtpCodeRepository.save(VerificationOtpCode.builder()
                    .otpCode(otpCode).user(user).target(identifier).type(type)
                    .expiryAt(now.plusSeconds(expirySeconds))
                    .resendCount((latestOtp != null) ? latestOtp.getResendCount() + 1 : 1)
                    .lastResendAt(now).isUsed(false).build());

            // Metadata cho OTP
            Map<String, Object> meta = new HashMap<>();
            meta.put("target", maskedTarget);
            meta.put("remainingSeconds", expirySeconds);
            meta.put("resendCount", resendCount);
            meta.put("type", "OTP");

            return new SecurityCodePayload(otpCode, meta);

        }
    }

    private void handleSuccessVerification(User user, VerificationType type) {
        if (type == VerificationType.REGISTER) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            // promotionService.rewardWelcomeVoucher(user);
            log.info("User {} activated successfully", user.getId());
        } else if (type == VerificationType.FORGOT_PASSWORD) {
            log.info("User {} verified for forgot password", user.getId());
        }
    }


    private AuthenticationResponse generateAuthenticationResponse(User user) {
        String accessToken = jwtProvider.generateToken(user, 900); // 15p
        String refreshTokenStr = UUID.randomUUID().toString(); // Hoặc dùng jwtProvider nếu muốn JWT

        // BẮT BUỘC: Lưu vào DB bảng refresh_tokens
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                // .deviceInfo(...) // Nếu có thông tin device thì nhét vào đây
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return authenticationMapper.toAuthenticationResponse(user, accessToken, refreshTokenStr, true);
    }

    private void validateResendCondition(User user, String identifier, VerificationType type, Instant now) {
        // Check CHÍNH XÁC dựa trên identifier người dùng đang nhập ở FE
        boolean isEmail = identifier.contains("@");
        boolean isEmailRegister = (type == VerificationType.REGISTER && isEmail);

        // Nếu là Email Register -> Không chặn 2 phút, cho phép gửi lại link thoải mái hơn
        if (isEmailRegister) {
            return;
        }

        // Luồng cho OTP (Phone hoặc Forgot Password)
        VerificationOtpCode lastOtp = verificationOtpCodeRepository
                .findTopByUserAndTypeOrderByCreatedAtDesc(user, type)
                .orElse(null);

        if (lastOtp != null) {
            // 1. Chống phá hoại: Gửi quá 5 lần thì khóa 2h
            if (lastOtp.getResendCount() >= 5) {
                user.setLockedUntil(now.plus(Duration.ofHours(2)));
                userRepository.save(user);
                throw new AppException(ErrorCode.TOO_MANY_RESEND_ATTEMPTS);
            }

            // 2. Chống spam: Phải đợi mã cũ hết hạn (2 phút) mới được xin mã mới
            if (now.isBefore(lastOtp.getExpiryAt())) {
                long secondsLeft = Duration.between(now, lastOtp.getExpiryAt()).getSeconds();
                throw new AppException(ErrorCode.RESEND_TOO_OFTEN, Map.of("secondsLeft", secondsLeft));
            }
        }
    }

    private void handleUnverifiedUser(User user, String identifier) {
        Instant now = Instant.now();
        VerificationType type = VerificationType.REGISTER;

        // Tìm mã OTP/Token gần nhất của luồng Register
        // (Vì identifier có thể là Email hoặc Phone nên nó sẽ tự check đúng bảng)
        SecurityCodePayload payload;

        try {
            // Tận dụng luôn hàm validateResendCondition để check xem cái cũ còn dùng được không
            validateResendCondition(user, identifier, type, now);

            // Nếu không ném lỗi (tức là mã cũ đã hết hạn hoặc chưa có), ta tạo mã mới luôn
            payload = generateSecurityCode(user, identifier, type);

            // Bắn event thông báo luôn cho nóng
            eventPublisher.publishEvent(NotificationEvent.builder()
                    .identifier(identifier)
                    .code(payload.code())
                    .type(type)
                    .build());

        } catch (AppException e) {
            // Nếu validateResendCondition báo lỗi (tức là mã cũ VẪN CÒN HẠN)
            // Ta đi tìm cái mã đó trong DB để lấy metadata trả về cho FE
            payload = fetchExistingMetadata(user, type, identifier);
        }

        throw new AppException(
                ErrorCode.USER_NOT_VERIFIED,
                "Tài khoản chưa xác thực. Vui lòng nhập mã đã gửi.",
                payload.metadata() // Trả về đầy đủ: remainingSeconds, resendCount...
        );
    }

    private SecurityCodePayload fetchExistingMetadata(User user, VerificationType type, String identifier) {
        // 1. Xác định xem trường hợp này đang dùng Token hay OTP
        boolean isEmail = identifier.contains("@");
        boolean useToken = (type == VerificationType.REGISTER && isEmail);

        String maskedTarget = isEmail ? maskEmail(identifier) : maskPhone(identifier);

        if (useToken) {
            // --- TRƯỜNG HỢP TOKEN (Chỉ dành cho Register qua Email) ---
            VerificationToken token = verificationTokenRepository.findByUserAndType(user, type)
                    .orElseThrow(() -> new AppException(ErrorCode.NO_ACTIVE_OTP_FOUND));

            Map<String, Object> meta = new HashMap<>();
            meta.put("target", maskedTarget);
            meta.put("remainingSeconds", Math.max(0, Duration.between(Instant.now(), token.getExpiryAt()).getSeconds()));
            meta.put("type", "TOKEN");

            return new SecurityCodePayload(token.getToken(), meta);

        } else {
            // --- TRƯỜNG HỢP OTP (Register bằng SĐT HOẶC Forgot Password bằng cả Email/SĐT) ---
            VerificationOtpCode otp = verificationOtpCodeRepository.findTopByUserAndTypeOrderByCreatedAtDesc(user, type)
                    .orElseThrow(() -> new AppException(ErrorCode.NO_ACTIVE_OTP_FOUND));

            Map<String, Object> meta = new HashMap<>();
            meta.put("target", maskedTarget);
            meta.put("remainingSeconds", Math.max(0, Duration.between(Instant.now(), otp.getExpiryAt()).getSeconds()));
            meta.put("resendCount", otp.getResendCount());
            meta.put("remainingAttempts", Math.max(0, 5 - otp.getAttemptCount()));
            meta.put("type", "OTP");

            return new SecurityCodePayload(otp.getOtpCode(), meta);
        }
    }

    private void processFailedAttempt(User user) {
        int newAttempts = user.getFailedAttemptCount() + 1;
        user.setFailedAttemptCount(newAttempts);
        if (newAttempts >= 5) {
            user.setLockedUntil(Instant.now().plus(30, ChronoUnit.MINUTES));
        }
        userRepository.save(user);
    }

    private void checkUserStatus(User user) {
        Instant now = Instant.now();

        // 1. Check xem có bị BANNED không
        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException(ErrorCode.ACCOUNT_BANNED);
        }

        // 2. Check xem có đang trong thời gian bị khóa (LockedUntil) không
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            long minutesLeft = Duration.between(now, user.getLockedUntil()).toMinutes();
            throw new AppException(ErrorCode.ACCOUNT_TEMPORARILY_LOCKED,
                    Map.of("lockedUntil", user.getLockedUntil(), "minutesLeft", Math.max(1, minutesLeft)));
        }

        // 3. Check logic Xóa mềm (Soft Delete - 30 ngày)
        if (user.getDeletedAt() != null) {
            Instant deadline = user.getDeletedAt().plus(30, ChronoUnit.DAYS);
            if (now.isAfter(deadline)) {
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }

            // Nếu Admin xóa thì không cho tự khôi phục
            if (user.isDeletedByAdmin()) {
                throw new AppException(ErrorCode.ACCOUNT_LOCKED_BY_ADMIN);
            }

            // Nếu User tự xóa mà nay login lại thì khôi phục luôn
            user.setDeletedAt(null);
            user.setStatus(UserStatus.ACTIVE);
            log.info("Tài khoản {} đã tự động khôi phục.", user.getId());
        }
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedAttemptCount() > 0 || user.getLockedUntil() != null) {
            user.setFailedAttemptCount(0);
            user.setLockedUntil(null);
            userRepository.save(user); // Lưu lại trạng thái sạch
            log.info("Reset failed attempts for user: {}", user.getId());
        }
    }

    private String maskEmail(String email) {
        if (email == null) return "";
        return email.replaceAll("(^.{2})(.*)(@.*$)", "$1***$3");
    }

    private String maskPhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("(\\d{3})(\\d{4})(\\d{3})", "$1****$3");
    }
}
