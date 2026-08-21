package com.learningpath.repository;

import com.learningpath.entity.OtpVerification;
import com.learningpath.entity.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {

    Optional<OtpVerification> findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String email,
            OtpPurpose purpose
    );

    List<OtpVerification> findAllByEmailAndPurposeAndUsedFalse(
            String email,
            OtpPurpose purpose
    );

    void deleteAllByEmail(String email);
}
