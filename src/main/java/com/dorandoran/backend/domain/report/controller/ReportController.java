package com.dorandoran.backend.domain.report.controller;

import com.dorandoran.backend.domain.report.dto.ReportCreateRequest;
import com.dorandoran.backend.domain.report.dto.ReportResponse;
import com.dorandoran.backend.domain.report.service.ReportService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Report", description = "신고 API")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 접수", description = "청년 또는 보호자가 문제 상황을 신고한다.")
    @PostMapping
    @PreAuthorize("hasAnyRole('YOUTH','GUARDIAN')")
    public ResponseEntity<ReportResponse> createReport(
            @AuthenticatedUser UUID userId,
            @Valid @RequestBody ReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createReport(userId, request));
    }
}
