package com.dorandoran.backend.domain.termination.controller;

import com.dorandoran.backend.domain.termination.MatchTerminationRequestStatus;
import com.dorandoran.backend.domain.termination.dto.AdminMatchTerminationProcessRequest;
import com.dorandoran.backend.domain.termination.dto.AdminMatchTerminationResponse;
import com.dorandoran.backend.domain.termination.service.MatchTerminationRequestService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
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

@Tag(name = "Admin MatchTermination", description = "관리자 매칭 중단 요청 관리 API")
@RestController
@RequestMapping("/api/v1/admin/match-termination-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMatchTerminationRequestController {

    private final MatchTerminationRequestService terminationRequestService;

    @Operation(summary = "매칭 중단 요청 목록 조회", description = "관리자가 전체 중단 요청 목록을 조회한다. status 파라미터로 필터링 가능.")
    @GetMapping
    public ResponseEntity<List<AdminMatchTerminationResponse>> getRequests(
            @RequestParam(required = false) MatchTerminationRequestStatus status) {
        return ResponseEntity.ok(terminationRequestService.getRequests(status));
    }

    @Operation(summary = "매칭 중단 요청 처리", description = "관리자가 중단 요청을 승인(APPROVED) 또는 반려(REJECTED)한다. 승인 시 매칭 상태가 ENDED로 변경된다.")
    @PatchMapping("/{requestId}")
    public ResponseEntity<AdminMatchTerminationResponse> processRequest(
            @AuthenticatedUser UUID adminId,
            @PathVariable("requestId") UUID requestId,
            @Valid @RequestBody AdminMatchTerminationProcessRequest request) {
        return ResponseEntity.ok(terminationRequestService.processRequest(adminId, requestId, request));
    }
}
