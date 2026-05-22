package com.dorandoran.backend.domain.admin.controller;

import com.dorandoran.backend.domain.admin.dto.AdminUserBanRequest;
import com.dorandoran.backend.domain.admin.dto.AdminUserBanResponse;
import com.dorandoran.backend.domain.admin.service.AdminUserService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Admin User", description = "관리자 사용자 관리 API")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "사용자 제재",
            description = "관리자가 운영 정책 위반 사용자를 SUSPENDED 상태로 변경한다. 관리자/본인은 제재할 수 없다.")
    @PatchMapping("/{userId}/ban")
    public ResponseEntity<AdminUserBanResponse> banUser(
            @AuthenticatedUser UUID adminId,
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody(required = false) AdminUserBanRequest request) {
        return ResponseEntity.ok(adminUserService.banUser(adminId, userId, request));
    }
}
