package com.datn.identityprovip.dto.event;

import com.datn.identityprovip.enums.VerificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationEvent {
    String identifier;
    String code;
    VerificationType type;
    String targetName;
    String newValue;
}
