package com.datn.identityprovip.repository;

import com.datn.identityprovip.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    @Query("SELECT p FROM Profile p JOIN FETCH p.user WHERE p.identityId = :identityId")
    Optional<Profile> findByIdWithUser(@Param("identityId") UUID identityId);
    boolean existsByNickname(String nickname);
}
