package com.dorandoran.backend.domain.user.dto;

import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;

import java.util.UUID;

public record AuthUserResponse(
        UUID id,
        String email,
        String name,
        Role role,
        String profileUrl
) {
    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getProfileUrl()
        );
    }
}
