package com.datn.identityprovip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {
    @NotBlank(message = "RECEIVER_NAME_REQUIRED")
    String receiverName;

    @NotBlank(message = "PHONE_NUMBER_REQUIRED")
    String phoneNumber;

    String provinceCode;
    String districtCode;
    String wardCode;

    String provinceName;
    String districtName;
    String wardName;

    String detailAddress;
    boolean defaultAddress;

    String addressType;
}
