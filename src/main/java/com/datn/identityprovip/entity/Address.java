package com.datn.identityprovip.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address {

    @Id
    @org.hibernate.annotations.UuidGenerator
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    Profile profile;

    @Column(name = "receiver_name", nullable = false)
    String receiverName;

    @Column(name = "phone_number", nullable = false)
    String phoneNumber;

    @Column(name = "province_code")
    String provinceCode;

    @Column(name = "district_code")
    String districtCode;

    @Column(name = "ward_code")
    String wardCode;

    @Column(name = "province_name")
    String provinceName;

    @Column(name = "district_name")
    String districtName;

    @Column(name = "ward_name")
    String wardName;

    @Column(name = "detail_address", columnDefinition = "TEXT")
    String detailAddress;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    boolean defaultAddress = false;

    @Builder.Default
    @Column(name = "address_type")
    String addressType = "HOME";

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;
}
