package com.dorandoran.backend.domain.admin.dto;

import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserBanResponse(
        UUID userId,
        String email,
        String name,
        Role role,
        UserStatus status,
        LocalDateTime updatedAt
) {
    public static AdminUserBanResponse from(User user) {
        return new AdminUserBanResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getStatus(),
                user.getUpdatedAt()
        );
    }
}
