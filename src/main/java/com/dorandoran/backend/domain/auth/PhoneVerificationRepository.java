package com.dorandoran.backend.domain.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, UUID> {

    Optional<PhoneVerification> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    boolean existsByPhoneNumberAndIsVerifiedTrue(String phoneNumber);
}
