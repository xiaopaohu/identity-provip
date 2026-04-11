package com.datn.identityprovip.validator;

import com.datn.identityprovip.dto.request.RegisterRequest;
import com.datn.identityprovip.exception.AppException;
import com.datn.identityprovip.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class RegisterValidator {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_REGEX = "^(0|84)(3|5|7|8|9)[0-9]{8}$";

    public void validate(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        // 2. Kiểm tra định dạng identifier (Email hoặc Phone)
        String identifier = request.getIdentifier().trim();

        boolean isEmail = identifier.matches(EMAIL_REGEX);
        boolean isPhone = identifier.matches(PHONE_REGEX);

        if (!isEmail && !isPhone) {
            throw new AppException(ErrorCode.INVALID_IDENTIFIER_FORMAT,
                    "Identifier phải là Email hoặc Số điện thoại hợp lệ.");
        }

        validatePasswordStrength(request.getPassword());
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new AppException(ErrorCode.PASSWORD_TOO_SHORT);
        }
    }
}
