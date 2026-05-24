package com.dorandoran.backend.domain.availabletime.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Elder AvailableTime", description = "어르신 가능 시간 등록 API")
@RestController
@RequestMapping("/api/v1/elders/{elderId}/available-times")
@RequiredArgsConstructor
@PreAuthorize("hasRole('GUARDIAN')")
public class ElderAvailableTimeController {

    private final AvailableTimeService availableTimeService;

    @Operation(summary = "어르신 가능 시간 등록",
            description = "보호자/기관이 본인이 등록한 어르신의 대화 가능 시간을 등록한다.")
    @PostMapping
    public ResponseEntity<AvailableTimeResponse> createForElder(
            @AuthenticatedUser UUID userId,
            @PathVariable("elderId") UUID elderId,
            @Valid @RequestBody AvailableTimeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availableTimeService.createForElder(userId, elderId, request));
    }
}
