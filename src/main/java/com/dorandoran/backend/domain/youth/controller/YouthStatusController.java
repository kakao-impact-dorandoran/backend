package com.dorandoran.backend.domain.youth.controller;

import com.dorandoran.backend.domain.youth.dto.YouthActivityStatusResponse;
import com.dorandoran.backend.domain.youth.dto.YouthActivityStatusUpdateRequest;
import com.dorandoran.backend.domain.youth.service.YouthProfileService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Youth Status", description = "청년 활동 상태 API")
@RestController
@RequestMapping("/api/v1/youth/status")
@RequiredArgsConstructor
@PreAuthorize("hasRole('YOUTH')")
public class YouthStatusController {

    private final YouthProfileService youthProfileService;

    @Operation(summary = "청년 활동 상태 변경",
            description = "승인 완료(APPROVED) 청년이 활동 가능/휴식/불가 상태를 변경한다. approvalStatus/프로필 내용은 변경되지 않는다.")
    @PatchMapping
    public ResponseEntity<YouthActivityStatusResponse> updateMyActivityStatus(
            @AuthenticatedUser UUID userId,
            @Valid @RequestBody YouthActivityStatusUpdateRequest request) {
        return ResponseEntity.ok(youthProfileService.updateMyActivityStatus(userId, request));
    }
}
