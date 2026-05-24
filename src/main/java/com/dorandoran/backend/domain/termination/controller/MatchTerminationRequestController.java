package com.dorandoran.backend.domain.termination.controller;

import com.dorandoran.backend.domain.termination.dto.MatchTerminationCreateRequest;
import com.dorandoran.backend.domain.termination.dto.MatchTerminationResponse;
import com.dorandoran.backend.domain.termination.service.MatchTerminationRequestService;
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

@Tag(name = "MatchTermination", description = "매칭 중단 요청 API")
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchTerminationRequestController {

    private final MatchTerminationRequestService terminationRequestService;

    @Operation(summary = "매칭 중단 요청", description = "청년 또는 보호자가 매칭 중단을 요청한다.")
    @PostMapping("/{matchId}/termination-requests")
    @PreAuthorize("hasAnyRole('YOUTH','GUARDIAN')")
    public ResponseEntity<MatchTerminationResponse> createRequest(
            @AuthenticatedUser UUID userId,
            @PathVariable("matchId") UUID matchId,
            @Valid @RequestBody MatchTerminationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(terminationRequestService.createRequest(userId, matchId, request));
    }
}
