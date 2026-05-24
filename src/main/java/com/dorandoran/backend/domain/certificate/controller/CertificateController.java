package com.dorandoran.backend.domain.certificate.controller;

import com.dorandoran.backend.domain.certificate.dto.CertificateIssueRequest;
import com.dorandoran.backend.domain.certificate.dto.CertificateResponse;
import com.dorandoran.backend.domain.certificate.service.CertificateService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Certificate", description = "사회참여 증명서 API")
@RestController
@RequestMapping("/api/v1/youth/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @Operation(summary = "사회참여 증명서 발급",
            description = "누적 활동 시간이 기준(10시간) 이상이면 발급 가능 시간 범위 내에서 증명서를 발급한다.")
    @PostMapping
    @PreAuthorize("hasRole('YOUTH')")
    public ResponseEntity<CertificateResponse> issueCertificate(
            @AuthenticatedUser UUID youthUserId,
            @Valid @RequestBody CertificateIssueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(certificateService.issueCertificate(youthUserId, request));
    }

    @Operation(summary = "내 증명서 목록 조회",
            description = "청년 본인이 발급받은 사회참여 증명서를 최신순으로 조회한다.")
    @GetMapping("/me")
    @PreAuthorize("hasRole('YOUTH')")
    public ResponseEntity<List<CertificateResponse>> findMyCertificates(
            @AuthenticatedUser UUID youthUserId) {
        return ResponseEntity.ok(certificateService.findMyCertificates(youthUserId));
    }
}
