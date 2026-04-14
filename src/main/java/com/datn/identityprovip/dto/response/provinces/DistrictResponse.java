package com.datn.identityprovip.dto.response.provinces;

import lombok.Data;

import java.util.List;

@Data
public class DistrictResponse {
    String code;
    String name;
    List<WardResponse> wards;
}
