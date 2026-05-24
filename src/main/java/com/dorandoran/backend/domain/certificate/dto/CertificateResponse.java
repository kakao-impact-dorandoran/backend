package com.dorandoran.backend.domain.certificate.dto;

import com.dorandoran.backend.domain.certificate.Certificate;

import java.time.LocalDateTime;
import java.util.UUID;

public record CertificateResponse(
        UUID certificateId,
        String certificateSerial,
        String title,
        UUID youthId,
        int certifiedHours,
        String pdfUrl,
        LocalDateTime issuedAt
) {
    public static CertificateResponse from(Certificate certificate) {
        return new CertificateResponse(
                certificate.getId(),
                certificate.getCertificateSerial(),
                certificate.getTitle(),
                certificate.getYouth().getId(),
                certificate.getCertifiedHours(),
                certificate.getPdfUrl(),
                certificate.getIssuedAt()
        );
    }
}
