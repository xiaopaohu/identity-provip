package com.datn.identityprovip.service.impl;

import com.datn.identityprovip.entity.District;
import com.datn.identityprovip.entity.Province;
import com.datn.identityprovip.entity.Ward;
import com.datn.identityprovip.repository.DistrictRepository;
import com.datn.identityprovip.repository.ProvinceRepository;
import com.datn.identityprovip.repository.WardRepository;
import com.datn.identityprovip.service.GeoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeoServiceImpl implements GeoService {

    ProvinceRepository provinceRepo;
    DistrictRepository districtRepo;
    WardRepository wardRepo;

    @Override
    public List<Province> getProvinces() {
        return provinceRepo.findAll();
    }

    @Override
    public List<District> getDistricts(String provinceCode) {
        return districtRepo.findByProvinceCode(provinceCode);
    }

    @Override
    public List<Ward> getWards(String districtCode) {
        return wardRepo.findByDistrictCode(districtCode);
    }
}
