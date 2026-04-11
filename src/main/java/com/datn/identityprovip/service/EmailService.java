package com.datn.identityprovip.service;

import com.datn.identityprovip.dto.event.NotificationEvent;
import com.datn.identityprovip.enums.VerificationType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    JavaMailSender mailSender;

    public void sendEmail(NotificationEvent event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getIdentifier());

            String subject = "";
            String content = "";
            String verifyLink = "http://localhost:5173/verify?token=" + event.getCode()
                    + "&identifier=" + event.getIdentifier()
                    + "&type=" + event.getType();

            if (event.getType() == VerificationType.REGISTER) {
                subject = "Xác thực tài khoản mới";
                content = String.format(
                        "Chào bạn,\n\nCảm ơn bạn đã đăng ký. Vui lòng nhấn vào link sau để kích hoạt tài khoản (hiệu lực trong 24h):\n%s\n\nNếu không phải bạn, vui lòng bỏ qua email này.",
                        verifyLink
                );
            } else {
                subject = "Mã đặt lại mật khẩu";
                content = String.format(
                        "Mã xác thực của bạn là: %s\nHiệu lực trong 2 phút.\nTuyệt đối không chia sẻ mã này cho bất kỳ ai.",
                        event.getCode()
                );
            }

            message.setSubject("[IdentityPro] " + subject);
            message.setText(content);
            mailSender.send(message);
            log.info("Email gửi thành công tới: {}", event.getIdentifier());
        } catch (Exception e) {
            log.error("Lỗi gửi Email: {}", e.getMessage());
        }
    }
}
