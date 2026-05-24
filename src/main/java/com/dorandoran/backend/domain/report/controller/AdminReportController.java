package com.dorandoran.backend.domain.report.controller;

import com.dorandoran.backend.domain.report.ReportStatus;
import com.dorandoran.backend.domain.report.dto.AdminReportProcessRequest;
import com.dorandoran.backend.domain.report.dto.AdminReportResponse;
import com.dorandoran.backend.domain.report.service.ReportService;
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

@Tag(name = "Admin Report", description = "관리자 신고 관리 API")
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 목록 조회", description = "관리자가 전체 신고 목록을 조회한다. status 파라미터로 필터링 가능.")
    @GetMapping
    public ResponseEntity<List<AdminReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status) {
        return ResponseEntity.ok(reportService.getReports(status));
    }

    @Operation(summary = "신고 처리", description = "관리자가 신고를 처리한다. (REVIEWING, RESOLVED, REJECTED)")
    @PatchMapping("/{reportId}")
    public ResponseEntity<AdminReportResponse> processReport(
            @AuthenticatedUser UUID adminId,
            @PathVariable("reportId") UUID reportId,
            @Valid @RequestBody AdminReportProcessRequest request) {
        return ResponseEntity.ok(reportService.processReport(adminId, reportId, request));
    }
}
