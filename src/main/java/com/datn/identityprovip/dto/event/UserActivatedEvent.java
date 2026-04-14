package com.datn.identityprovip.dto.event;

import com.datn.identityprovip.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEvent;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActivatedEvent extends ApplicationEvent {
    User user;

    public UserActivatedEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
