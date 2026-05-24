package com.dorandoran.backend.domain.certificate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CertificateIssueRequest(
        @NotNull(message = "requestedHours는 필수입니다.")
        @Min(value = 1, message = "requestedHours는 1 이상이어야 합니다.")
        Integer requestedHours
) {
}
