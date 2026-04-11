package com.datn.identityprovip.service;

import com.datn.identityprovip.config.TelegramProperties;
import com.datn.identityprovip.dto.event.NotificationEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TelegramSmsService {

    TelegramProperties telegramProperties;
    RestTemplate restTemplate;

    public void sendOtp(NotificationEvent event) {
        String header = switch (event.getType()) {
            case REGISTER -> "🔑 [XÁC THỰC ĐĂNG KÝ]";
            case FORGOT_PASSWORD -> "🔄 [KHÔI PHỤC MẬT KHẨU]";
            case CHANGE_PHONE -> "📱 [THAY ĐỔI SỐ ĐIỆN THOẠI]";
            default -> "⚠️ [THÔNG BÁO BẢO MẬT]";
        };

        String text = String.format(
                "%s\nSĐT: %s\nMã OTP: %s\nHiệu lực: 2 phút.\nVui lòng không cung cấp mã cho bất kỳ ai.",
                header, event.getIdentifier(), event.getCode()
        );

        String url = "https://api.telegram.org/bot{token}/sendMessage?chat_id={chatId}&text={text}";

        Map<String, String> params = new HashMap<>();
        params.put("token", telegramProperties.getBotToken());
        params.put("chatId", telegramProperties.getChatId());
        params.put("text", text);

        try {
            restTemplate.getForObject(url, String.class, params);
            log.info("OTP Telegram gửi thành công tới: {}", event.getIdentifier());
        } catch (Exception e) {
            log.error("Lỗi API Telegram: {}", e.getMessage());
        }
    }
}
