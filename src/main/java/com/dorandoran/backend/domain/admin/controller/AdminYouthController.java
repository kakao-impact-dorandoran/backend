package com.dorandoran.backend.domain.admin.controller;

import com.dorandoran.backend.domain.admin.dto.AdminYouthApprovalRequest;
import com.dorandoran.backend.domain.admin.dto.AdminYouthApprovalResponse;
import com.dorandoran.backend.domain.admin.dto.AdminYouthDetailResponse;
import com.dorandoran.backend.domain.admin.dto.AdminYouthListResponse;
import com.dorandoran.backend.domain.admin.service.AdminYouthService;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin Youth", description = "관리자 청년 관리 API")
@RestController
@RequestMapping("/api/v1/admin/youths")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminYouthController {

    private final AdminYouthService adminYouthService;

    @Operation(summary = "청년 목록 조회", description = "관리자가 청년 프로필 목록을 조회한다. approvalStatus 쿼리 파라미터로 필터링할 수 있다.")
    @GetMapping
    public ResponseEntity<List<AdminYouthListResponse>> findYouths(
            @RequestParam(name = "approvalStatus", required = false) YouthApprovalStatus approvalStatus) {
        return ResponseEntity.ok(adminYouthService.findYouths(approvalStatus));
    }

    @Operation(summary = "청년 상세 조회", description = "관리자가 특정 청년의 사용자 정보와 프로필을 조회한다.")
    @GetMapping("/{youthId}")
    public ResponseEntity<AdminYouthDetailResponse> getYouthDetail(@PathVariable("youthId") UUID youthId) {
        return ResponseEntity.ok(adminYouthService.getYouthDetail(youthId));
    }

    @Operation(summary = "청년 가입 검수", description = "관리자가 청년 프로필을 승인(APPROVED) 또는 반려(REJECTED) 처리한다. PENDING으로 되돌릴 수 없다.")
    @PatchMapping("/{youthId}/approval")
    public ResponseEntity<AdminYouthApprovalResponse> changeApproval(
            @PathVariable("youthId") UUID youthId,
            @Valid @RequestBody AdminYouthApprovalRequest request) {
        return ResponseEntity.ok(adminYouthService.changeApproval(youthId, request));
    }
}
