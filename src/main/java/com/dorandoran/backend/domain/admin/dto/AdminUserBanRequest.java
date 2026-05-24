package com.dorandoran.backend.domain.admin.dto;

import jakarta.validation.constraints.Size;

public record AdminUserBanRequest(
        @Size(max = 500, message = "제재 사유는 최대 500자까지 입력할 수 있습니다.")
        String reason
) {
}
