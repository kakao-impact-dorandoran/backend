package com.dorandoran.backend.domain.availabletime.controller;

import com.dorandoran.backend.domain.availabletime.AvailableTimeOwnerType;
import com.dorandoran.backend.domain.availabletime.dto.AvailableTimeCreateRequest;
import com.dorandoran.backend.domain.availabletime.dto.AvailableTimeResponse;
import com.dorandoran.backend.domain.availabletime.service.AvailableTimeService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "AvailableTime", description = "가능 시간 API")
@RestController
@RequestMapping("/api/v1/available-times")
@RequiredArgsConstructor
public class AvailableTimeController {

    private final AvailableTimeService availableTimeService;

    @Operation(summary = "청년 가능 시간 등록", description = "청년 본인의 활동 가능 시간을 등록한다.")
    @PostMapping("/youth")
    @PreAuthorize("hasRole('YOUTH')")
    public ResponseEntity<AvailableTimeResponse> createForYouth(
            @AuthenticatedUser UUID userId,
            @Valid @RequestBody AvailableTimeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availableTimeService.createForYouth(userId, request));
    }

    @Operation(summary = "가능 시간 조회", description = "ownerType=YOUTH/ELDER, ownerId 기준 가능 시간 목록을 조회한다.")
    @GetMapping
    @PreAuthorize("hasAnyRole('YOUTH','GUARDIAN','ADMIN')")
    public ResponseEntity<List<AvailableTimeResponse>> find(
            @AuthenticatedUser UUID userId,
            @RequestParam("ownerType") AvailableTimeOwnerType ownerType,
            @RequestParam("ownerId") UUID ownerId) {
        return ResponseEntity.ok(availableTimeService.find(userId, ownerType, ownerId));
    }
}
