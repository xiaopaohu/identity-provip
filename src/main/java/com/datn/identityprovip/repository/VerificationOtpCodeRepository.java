package com.datn.identityprovip.repository;

import com.datn.identityprovip.entity.User;
import com.datn.identityprovip.entity.VerificationOtpCode;
import com.datn.identityprovip.enums.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;


@Repository
public interface VerificationOtpCodeRepository extends JpaRepository<VerificationOtpCode, Long> {

    Optional<VerificationOtpCode> findTopByUserAndTargetAndTypeAndIsUsedFalseOrderByCreatedAtDesc(
            User user, String target, VerificationType type);

    @Query("""
        SELECT o FROM VerificationOtpCode o 
        WHERE o.otpCode = :otpCode 
        AND o.user = :user 
        AND o.target = :target
        AND o.type = :type
        AND o.isUsed = false 
        AND o.expiryAt > :now 
        AND (o.lockedUntil IS NULL OR o.lockedUntil < :now)
    """)
    Optional<VerificationOtpCode> findValidOtp(
            String otpCode, User user, String target, VerificationType type, Instant now);

    Optional<VerificationOtpCode> findTopByUserAndTypeOrderByCreatedAtDesc(User user, VerificationType type);

    // Trong VerificationOtpCodeRepository.java
    @Modifying
    @Query("UPDATE VerificationOtpCode v SET v.isUsed = true WHERE v.user = :user AND v.type = :type AND v.isUsed = false")
    void invalidateAllActiveOtp(@Param("user") User user, @Param("type") VerificationType type);

    @Modifying
    @Transactional
    void deleteByUser(User user);

    boolean existsByUserAndTargetAndTypeAndExpiryAtAfterAndIsUsedFalse(
            User user, String target, VerificationType type, Instant now);
}
