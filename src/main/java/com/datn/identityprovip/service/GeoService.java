package com.datn.identityprovip.service;

import com.datn.identityprovip.entity.District;
import com.datn.identityprovip.entity.Province;
import com.datn.identityprovip.entity.Ward;

import java.util.List;

public interface GeoService {
    List<Province> getProvinces();
    List<District> getDistricts(String provinceCode);
    List<Ward> getWards(String districtCode);
}
