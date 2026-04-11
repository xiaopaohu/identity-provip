package com.datn.identityprovip.repository;

import com.datn.identityprovip.entity.User;
import com.datn.identityprovip.entity.VerificationToken;
import com.datn.identityprovip.enums.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserAndType(User user, VerificationType type);

    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.user = :user AND v.type = :type")
    void invalidateAllActiveTokens(@Param("user") User user, @Param("type") VerificationType type);

    void deleteByExpiryAtBefore(Instant now);

    void deleteByUser(User user);
}