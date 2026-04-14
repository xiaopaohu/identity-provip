package com.datn.identityprovip.repository;

import com.datn.identityprovip.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, String> {

    List<District> findByProvinceCode(String provinceCode);
}
