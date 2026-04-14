package com.datn.identityprovip.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {
    UUID id;
    String receiverName;
    String phoneNumber;

    String provinceCode;
    String districtCode;
    String wardCode;

    String provinceName;
    String districtName;
    String wardName;

    String detailAddress;

    String completeAddress;

    boolean defaultAddress;
    String addressType;
    Instant createdAt;
    Instant updatedAt;
}