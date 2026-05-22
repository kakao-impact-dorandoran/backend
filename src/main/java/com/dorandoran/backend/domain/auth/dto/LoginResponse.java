package com.dorandoran.backend.domain.auth.dto;

import com.dorandoran.backend.domain.user.dto.AuthUserResponse;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        AuthUserResponse user
) {
}
