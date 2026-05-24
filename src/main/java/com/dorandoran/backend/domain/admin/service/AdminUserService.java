package com.dorandoran.backend.domain.admin.service;

import com.dorandoran.backend.domain.admin.dto.AdminUserBanRequest;
import com.dorandoran.backend.domain.admin.dto.AdminUserBanResponse;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional
    public AdminUserBanResponse banUser(UUID actorAdminId, UUID targetUserId, AdminUserBanRequest request) {
        if (actorAdminId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.CANNOT_BAN_SELF);
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (target.getRole() == Role.ADMIN) {
            throw new BusinessException(ErrorCode.CANNOT_BAN_ADMIN);
        }
        if (target.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_SUSPENDED);
        }
        target.suspend();
        String reason = (request != null && request.reason() != null) ? request.reason() : null;
        log.info("[admin-ban] adminId={} targetUserId={} reason={}", actorAdminId, targetUserId, reason);
        return AdminUserBanResponse.from(target);
    }
}
