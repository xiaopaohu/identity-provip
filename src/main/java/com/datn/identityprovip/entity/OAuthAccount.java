package com.datn.identityprovip.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "oauth_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OAuthAccount {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "provider", nullable = false, length = 50)
    String provider;

    @Column(name = "provider_user_id", nullable = false)
    String providerUserId;

    @Column(name = "created_at", insertable = false, updatable = false)
    LocalDateTime createdAt;
}
