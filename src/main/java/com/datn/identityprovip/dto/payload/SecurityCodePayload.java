package com.datn.identityprovip.dto.payload;

import lombok.Builder;

import java.util.Map;

@Builder
public record SecurityCodePayload(
        String code,
        Map<String, Object> metadata
) {}
