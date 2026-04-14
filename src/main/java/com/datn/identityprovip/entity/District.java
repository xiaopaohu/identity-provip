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
@Table(name = "districts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class District {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "province_code", nullable = false)
    private String provinceCode;
}
