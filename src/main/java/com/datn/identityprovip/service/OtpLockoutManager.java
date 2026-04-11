package com.datn.identityprovip.service;

import com.datn.identityprovip.entity.User;
import com.datn.identityprovip.entity.VerificationOtpCode;
import com.datn.identityprovip.exception.AppException;
import com.datn.identityprovip.exception.ErrorCode;
import com.datn.identityprovip.repository.UserRepository;
import com.datn.identityprovip.repository.VerificationOtpCodeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OtpLockoutManager {

    VerificationOtpCodeRepository otpRepository;
    UserRepository userRepository;

    static final int MAX_ATTEMPTS = 5; // Tối đa 5 lần nhập sai cho 1 mã
    static final int MAX_RESENDS = 5;   // Tối đa 5 lần yêu cầu gửi lại trong 1 khoảng thời gian

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedAttempt(VerificationOtpCode otp) {
        int currentAttempts = otp.getAttemptCount() + 1;
        otp.setAttemptCount(currentAttempts);
        otp.setLastAttemptAt(Instant.now());

        // Nếu vượt quá số lần thử, vô hiệu hóa mã này ngay lập tức
        if (currentAttempts >= MAX_ATTEMPTS) {
            otp.setUsed(true);
            log.warn("OTP ID {} đã bị vô hiệu hóa do nhập sai quá {} lần", otp.getId(), MAX_ATTEMPTS);
        }

        otpRepository.saveAndFlush(otp);
        return currentAttempts;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOtpUsed(VerificationOtpCode otp) {
        otp.setUsed(true);
        otpRepository.saveAndFlush(otp);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockUser(User user, Duration duration, String reason) {
        user.setLockedUntil(Instant.now().plus(duration));
        // Mày có thể lưu reason vào một bảng Audit Log nếu muốn Pro hơn
        userRepository.saveAndFlush(user);
        log.info("User {} bị khóa trong {} vì: {}", user.getId(), duration, reason);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleResendLimit(User user, VerificationOtpCode lastOtp) {
        // Logic này dùng để chặn việc User spam nút "Resend"
        int resendCount = (lastOtp != null) ? lastOtp.getResendCount() + 1 : 1;

        if (resendCount >= MAX_RESENDS) {
            lockUser(user, Duration.ofHours(24), "Spam resend OTP");
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS, "Bạn đã yêu cầu quá nhiều mã. Vui lòng quay lại sau 24h.");
        }
    }
}