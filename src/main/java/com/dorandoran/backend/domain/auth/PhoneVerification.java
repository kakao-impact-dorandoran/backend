package com.dorandoran.backend.domain.auth;

import com.dorandoran.backend.global.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "phone_verifications",
        indexes = @Index(name = "idx_phone_verifications_phone", columnList = "phone_number")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "verification_code", length = 10, nullable = false)
    private String verificationCode;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Builder
    private PhoneVerification(String phoneNumber, String verificationCode, LocalDateTime expiresAt) {
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt;
        this.isVerified = false;
    }

    public void markVerified() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
