package com.dorandoran.backend.domain.user.controller;

import com.dorandoran.backend.domain.user.dto.AuthUserResponse;
import com.dorandoran.backend.domain.user.service.UserQueryService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;

    @Operation(summary = "내 정보 조회", description = "현재 인증된 사용자의 기본 정보 및 역할(Role) 반환")
    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> getMe(@AuthenticatedUser UUID userId) {
        return ResponseEntity.ok(userQueryService.getMe(userId));
    }
}
