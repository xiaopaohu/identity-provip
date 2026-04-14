package com.datn.identityprovip.repository;

import com.datn.identityprovip.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, String> {

    List<Ward> findByDistrictCode(String districtCode);
}
