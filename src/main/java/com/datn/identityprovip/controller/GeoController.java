package com.datn.identityprovip.controller;

import com.datn.identityprovip.entity.District;
import com.datn.identityprovip.entity.Province;
import com.datn.identityprovip.entity.Ward;
import com.datn.identityprovip.service.GeoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/geo")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GeoController {
    GeoService geoService;

    @GetMapping("/provinces")
    public List<Province> getProvinces() {
        return geoService.getProvinces();
    }

    @GetMapping("/districts/{provinceCode}")
    public List<District> getDistricts(@PathVariable String provinceCode) {
        return geoService.getDistricts(provinceCode);
    }

    @GetMapping("/wards/{districtCode}")
    public List<Ward> getWards(@PathVariable String districtCode) {
        return geoService.getWards(districtCode);
    }
}