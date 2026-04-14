package com.datn.identityprovip.service;

import com.datn.identityprovip.dto.response.provinces.DistrictResponse;
import com.datn.identityprovip.dto.response.provinces.ProvinceResponse;
import com.datn.identityprovip.dto.response.provinces.WardResponse;
import com.datn.identityprovip.entity.District;
import com.datn.identityprovip.entity.Province;
import com.datn.identityprovip.entity.Ward;
import com.datn.identityprovip.repository.DistrictRepository;
import com.datn.identityprovip.repository.ProvinceRepository;
import com.datn.identityprovip.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GeoImportService {

    private final RestTemplate restTemplate;
    private final ProvinceRepository provinceRepo;
    private final DistrictRepository districtRepo;
    private final WardRepository wardRepo;

    @Transactional
    public void importData() {
        String url = "https://provinces.open-api.vn/api/?depth=3";

        ProvinceResponse[] data =
                restTemplate.getForObject(url, ProvinceResponse[].class);

        for (ProvinceResponse p : data) {

            if (provinceRepo.existsById(p.getCode())) continue;

            provinceRepo.save(new Province(p.getCode(), p.getName()));

            for (DistrictResponse d : p.getDistricts()) {

                districtRepo.save(new District(
                        d.getCode(),
                        d.getName(),
                        p.getCode()
                ));

                for (WardResponse w : d.getWards()) {

                    wardRepo.save(new Ward(
                            w.getCode(),
                            w.getName(),
                            d.getCode()
                    ));
                }
            }
        }
    }
}