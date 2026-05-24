package com.dorandoran.backend.domain.youth.controller;

import com.dorandoran.backend.domain.youth.dto.YouthProfileCreateRequest;
import com.dorandoran.backend.domain.youth.dto.YouthProfileCreateResponse;
import com.dorandoran.backend.domain.youth.dto.YouthProfileResponse;
import com.dorandoran.backend.domain.youth.dto.YouthProfileUpdateRequest;
import com.dorandoran.backend.domain.youth.service.YouthProfileService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Youth Profile", description = "청년 프로필 API")
@RestController
@RequestMapping("/api/v1/youth/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('YOUTH')")
public class YouthProfileController {

    private final YouthProfileService youthProfileService;

    @Operation(summary = "청년 프로필 등록",
            description = "청년 본인이 자기소개 프로필을 등록한다. approvalStatus=PENDING, activityStatus=AVAILABLE로 생성된다.")
    @PostMapping
    public ResponseEntity<YouthProfileCreateResponse> create(
            @AuthenticatedUser UUID userId,
            @Valid @RequestBody YouthProfileCreateRequest request) {
        YouthProfileCreateResponse response = youthProfileService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "내 청년 프로필 조회", description = "청년 본인의 프로필 및 승인 상태를 조회한다.")
    @GetMapping("/me")
    public ResponseEntity<YouthProfileResponse> getMyProfile(@AuthenticatedUser UUID userId) {
        return ResponseEntity.ok(youthProfileService.getMyProfile(userId));
    }

    @Operation(summary = "내 청년 프로필 수정",
            description = "청년 본인의 프로필 정보를 수정한다. approvalStatus/activityStatus/rejectionReason은 변경되지 않는다.")
    @PatchMapping("/me")
    public ResponseEntity<YouthProfileResponse> updateMyProfile(
            @AuthenticatedUser UUID userId,
            @Valid @RequestBody YouthProfileUpdateRequest request) {
        return ResponseEntity.ok(youthProfileService.updateMyProfile(userId, request));
    }
}
