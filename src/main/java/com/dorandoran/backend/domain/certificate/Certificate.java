package com.dorandoran.backend.domain.certificate;

import com.dorandoran.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "certificates",
        uniqueConstraints = @UniqueConstraint(name = "uk_certificates_serial", columnNames = "certificate_serial"),
        indexes = {
                @Index(name = "idx_certificates_youth", columnList = "youth_id"),
                @Index(name = "idx_certificates_issued_at", columnList = "issued_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certificate {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "youth_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_certificates_youth"))
    private User youth;

    @Column(name = "certificate_serial", length = 100, nullable = false)
    private String certificateSerial;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "certified_hours", nullable = false)
    private int certifiedHours;

    @Column(name = "pdf_url", columnDefinition = "TEXT")
    private String pdfUrl;

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Builder
    private Certificate(User youth, String certificateSerial, String title,
                        int certifiedHours, String pdfUrl) {
        this.youth = youth;
        this.certificateSerial = certificateSerial;
        this.title = title;
        this.certifiedHours = certifiedHours;
        this.pdfUrl = pdfUrl;
    }
}
