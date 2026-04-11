package com.datn.identityprovip.entity;


import com.datn.identityprovip.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @org.hibernate.annotations.UuidGenerator
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;


    @Column(name = "email", unique = true)
    String email;

    @Column(name = "phone", unique = true, length = 20)
    String phone;

    @Column(name = "password_hash")
    String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    UserStatus status;

    @Column(name = "two_factor_enabled")
    boolean twoFactorEnabled;

    @Column(name = "is_mfa_verified")
    boolean isMfaVerified;

    @Column(name = "failed_attempt_count")
    int failedAttemptCount;

    @Column(name = "locked_until")
    Instant lockedUntil;

    @Column(name = "last_login_at")
    Instant lastLoginAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    Instant createdAt;

    @Column(name = "updated_at")
    Instant updatedAt;

    @Column(name = "deleted_at")
    Instant deletedAt;

    @Column(name = "is_deleted_by_admin")
    boolean deletedByAdmin;

    @Column(name = "ban_reason")
    String banReason;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<UserRole> userRoles;

    public void addRole(Role role) {
        if (userRoles == null) {
            userRoles = new HashSet<>();
        }
        UserRole userRole = UserRole.builder()
//                .id(new UserRoleId(this.id, role.getId()))
                .user(this)
                .role(role)
                .build();
        userRole.setId(new UserRoleId(this.id, role.getId()));
        userRoles.add(userRole);
    }
}