package com.datn.identityprovip.repository;


import com.datn.identityprovip.entity.UserRole;
import com.datn.identityprovip.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
