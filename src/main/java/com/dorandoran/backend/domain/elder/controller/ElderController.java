package com.dorandoran.backend.domain.elder.controller;

import com.dorandoran.backend.domain.elder.dto.ElderCreateRequest;
import com.dorandoran.backend.domain.elder.dto.ElderResponse;
import com.dorandoran.backend.domain.elder.dto.ElderUpdateRequest;
import com.dorandoran.backend.domain.elder.service.ElderService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Elder", description = "어르신 정보 API")
@RestController
@RequestMapping("/api/v1/elders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('GUARDIAN')")
public class ElderController {

    private final ElderService elderService;

    @Operation(summary = "어르신 정보 등록", description = "보호자/기관이 어르신 정보를 등록한다. 기본 상태는 AVAILABLE.")
    @PostMapping
    public ResponseEntity<ElderResponse> create(
            @AuthenticatedUser UUID guardianId,
            @Valid @RequestBody ElderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(elderService.create(guardianId, request));
    }

    @Operation(summary = "내 어르신 목록 조회", description = "보호자/기관이 자신이 등록한 어르신 목록을 조회한다.")
    @GetMapping("/my")
    public ResponseEntity<List<ElderResponse>> findMyElders(@AuthenticatedUser UUID guardianId) {
        return ResponseEntity.ok(elderService.findMyElders(guardianId));
    }

    @Operation(summary = "어르신 정보 수정", description = "보호자/기관이 본인이 등록한 어르신 정보를 수정한다. status/deviceId는 변경할 수 없다.")
    @PatchMapping("/{elderId}")
    public ResponseEntity<ElderResponse> update(
            @AuthenticatedUser UUID guardianId,
            @PathVariable("elderId") UUID elderId,
            @Valid @RequestBody ElderUpdateRequest request) {
        return ResponseEntity.ok(elderService.update(guardianId, elderId, request));
    }
}
