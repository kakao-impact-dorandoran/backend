package com.dorandoran.backend.domain.help.controller;

import com.dorandoran.backend.domain.help.HelpRequestStatus;
import com.dorandoran.backend.domain.help.dto.AdminHelpRequestProcessRequest;
import com.dorandoran.backend.domain.help.dto.AdminHelpRequestResponse;
import com.dorandoran.backend.domain.help.service.HelpRequestService;
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

@Tag(name = "Admin HelpRequest", description = "관리자 도움 요청 처리 API")
@RestController
@RequestMapping("/api/v1/admin/help-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminHelpRequestController {

    private final HelpRequestService helpRequestService;

    @Operation(summary = "도움 요청 목록 조회",
            description = "관리자가 도움 요청 목록을 조회한다. status 파라미터로 필터링 가능하다.")
    @GetMapping
    public ResponseEntity<List<AdminHelpRequestResponse>> getRequests(
            @RequestParam(required = false) HelpRequestStatus status) {
        return ResponseEntity.ok(helpRequestService.getRequests(status));
    }

    @Operation(summary = "도움 요청 처리",
            description = "관리자가 도움 요청을 처리 완료 상태로 변경한다.")
    @PatchMapping("/{helpRequestId}")
    public ResponseEntity<AdminHelpRequestResponse> processRequest(
            @AuthenticatedUser UUID adminId,
            @PathVariable UUID helpRequestId,
            @Valid @RequestBody AdminHelpRequestProcessRequest request) {
        return ResponseEntity.ok(helpRequestService.processRequest(adminId, helpRequestId, request));
    }
}
