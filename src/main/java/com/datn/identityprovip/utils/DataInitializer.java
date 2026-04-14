package com.datn.identityprovip.utils;

import com.datn.identityprovip.service.GeoImportService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final GeoImportService geoImportService;

    @PostConstruct
    public void init() {
        geoImportService.importData();
    }
}
