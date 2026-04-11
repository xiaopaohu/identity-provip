package com.datn.identityprovip.listener;

import com.datn.identityprovip.dto.event.NotificationEvent;
import com.datn.identityprovip.service.EmailService;
import com.datn.identityprovip.service.TelegramSmsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityNotificationListener {

    EmailService emailService;
    TelegramSmsService telegramService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotification(NotificationEvent event) {
        log.info("Nhận sự kiện thông báo cho: {}", event.getIdentifier());

        if (event.getIdentifier().contains("@")) {
            emailService.sendEmail(event);
        } else {
            telegramService.sendOtp(event);
        }
    }
}