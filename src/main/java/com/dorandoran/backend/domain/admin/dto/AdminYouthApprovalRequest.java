package com.dorandoran.backend.domain.admin.dto;

import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminYouthApprovalRequest(
        @NotNull(message = "approvalStatus는 필수입니다.")
        YouthApprovalStatus approvalStatus,
        @Size(max = 500, message = "반려 사유는 최대 500자까지 입력할 수 있습니다.")
        String rejectionReason
) {
}
