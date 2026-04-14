package com.datn.identityprovip.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ward {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "district_code", nullable = false)
    private String districtCode;
}
