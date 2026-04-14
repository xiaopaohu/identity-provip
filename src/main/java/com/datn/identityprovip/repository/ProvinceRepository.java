package com.datn.identityprovip.repository;

import com.datn.identityprovip.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, String> {
}
