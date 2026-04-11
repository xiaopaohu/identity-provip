package com.datn.identityprovip.service;

import com.datn.identityprovip.dto.request.*;
import com.datn.identityprovip.dto.response.AdminUserResponse;
import com.datn.identityprovip.dto.response.PageResponse;
import com.datn.identityprovip.dto.response.UserResponse;
import com.datn.identityprovip.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

public interface UserService {
    // --- NHÓM ADMIN ---
    AdminUserResponse createUser(AdminCreateUserRequest request);
    PageResponse<AdminUserResponse> getAllUsers(int page, int size);
    AdminUserResponse getAdminUserDetail(UUID id);
    AdminUserResponse updateUser(UUID userId, AdminUserUpdateRequest request);

    // Điều khiển trạng thái
    void softDelete(UUID targetId);
    void hardDelete(UUID targetId);
    AdminUserResponse restoreUser(UUID targetId);

    // Ban/Unban: Dùng Enum UserStatus bên trong logic
    AdminUserResponse updateStatus(UUID targetId, UserStatus status, String reason, Instant until);

    // --- NHÓM NGƯỜI DÙNG (SELF-SERVICE) ---
    UserResponse getMyInfo();
    void changePassword(ChangePasswordRequest request);
    void requestUpdateIdentifier(UpdateIdentifierRequest request);
    void confirmUpdateIdentifier(VerifyRequest request);

    void selfDeactivate();
}
