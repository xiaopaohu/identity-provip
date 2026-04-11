package com.datn.identityprovip.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // ========================================================================
    // 9xxx: SYSTEM & COMMON (Lỗi hệ thống)
    // ========================================================================
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9001, "Thông điệp lỗi không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(9002, "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),

    // ========================================================================
    // 10xx: USER & AUTH (Thông tin người dùng)
    // ========================================================================
    USER_EXISTED(1001, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1003, "Mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    USER_NOT_VERIFIED(1004, "Tài khoản chưa xác thực. Vui lòng xác nhận mã OTP.", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1005, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    PHONE_EXISTED(1006, "Số điện thoại đã tồn tại", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_INCORRECT(1007, "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCHED(1008, "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1009, "Tài khoản chưa được xác thực hoặc phiên làm việc hết hạn", HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_REGISTERED(1010, "Email chưa được đăng ký", HttpStatus.BAD_REQUEST),
    PHONE_NOT_REGISTERED(1011, "Số điện thoại chưa được đăng ký", HttpStatus.BAD_REQUEST),
    INVALID_IDENTIFIER_FORMAT(1013, "Không đúng định dạng", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT(1014, "Mật khẩu quá ngắn", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(1015, "Mật khẩu không khớp", HttpStatus.BAD_REQUEST),

    // ========================================================================
    // 11xx: ROLE (Phân quyền)
    // ========================================================================
    ROLE_NOT_EXISTED(1101, "Quyền không tồn tại", HttpStatus.NOT_FOUND),

    // ========================================================================
    // 12xx: OTP & TOKEN (Mã xác thực & Phiên làm việc)
    // ========================================================================
    INVALID_TOKEN(1201, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1202, "Token đã hết hạn", HttpStatus.UNAUTHORIZED),
    INVALID_OTP(1203, "Mã OTP không chính xác", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1204, "Mã OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    OTP_INVALID_OR_EXPIRED(1205, "Mã OTP không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    NO_ACTIVE_OTP_FOUND(1206, "Không tìm thấy mã OTP khả dụng", HttpStatus.NOT_FOUND),
    NO_OTP_HISTORY(1207, "Không tìm thấy lịch sử gửi OTP", HttpStatus.NOT_FOUND),
    OTP_LOCKED(1208, "OTP đã bị khóa", HttpStatus.BAD_REQUEST),
    PHONE_ALREADY_VERIFIED(1209, "Số điện thoại này đã được xác thực", HttpStatus.BAD_REQUEST),
    INVALID_METHOD(1210, "Phương thức không hợp lệ", HttpStatus.BAD_REQUEST),
    OTP_STILL_VALID(1211, "OTP vẫn còn hiệu lực", HttpStatus.BAD_REQUEST),
    TOO_MANY_REQUESTS(1212, "Bạn đã yêu cầu quá nhiều mã. Vui lòng quay lại sau 24h", HttpStatus.BAD_REQUEST),
    USER_ALREADY_VERIFIED(1213, "Tài khoản đã xác thực!", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS_REACHED(1214, "Mã OTP đã hết lượt.", HttpStatus.BAD_REQUEST),
    TOO_MANY_RESEND_ATTEMPTS(1215, "Bạn đã yêu cầu gửi lại quá nhiều lần!", HttpStatus.BAD_REQUEST),
    RESEND_TOO_OFTEN(1215, "Chờ gửi lại sau", HttpStatus.BAD_REQUEST),
    INVALID_RECOVERY_METHOD(1216, "Phương thức khôi phục tài khoản không hợp lệ!", HttpStatus.BAD_REQUEST),

    // ========================================================================
    // 13xx: ACCOUNT STATUS (Trạng thái tài khoản)
    // ========================================================================
    ACCOUNT_BANNED(1301, "Tài khoản của bạn đã bị khóa vĩnh viễn", HttpStatus.FORBIDDEN),
    ACCOUNT_TEMPORARILY_LOCKED(1302, "Tài khoản bị khóa tạm thời. Vui lòng thử lại sau", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED_BY_ADMIN(1303, "Tài khoản bị khóa bởi quản trị viên", HttpStatus.FORBIDDEN),
    USER_DELETED_PERMANENTLY(1304, "Tài khoản đã bị xóa vĩnh viễn", HttpStatus.GONE);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}