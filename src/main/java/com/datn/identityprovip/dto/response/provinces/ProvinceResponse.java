package com.datn.identityprovip.dto.response.provinces;

import lombok.Data;

import java.util.List;

@Data
public class ProvinceResponse {
    String code;
    String name;
    List<DistrictResponse> districts;
}
