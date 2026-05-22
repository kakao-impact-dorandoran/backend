package com.dorandoran.backend.domain.match.controller;

import com.dorandoran.backend.domain.match.dto.MatchCreateRequest;
import com.dorandoran.backend.domain.match.dto.MatchDetailResponse;
import com.dorandoran.backend.domain.match.dto.MatchResponse;
import com.dorandoran.backend.domain.match.dto.MatchSummaryResponse;
import com.dorandoran.backend.domain.match.dto.YouthMatchLimitResponse;
import com.dorandoran.backend.domain.match.service.MatchService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Match", description = "청년 선택형 매칭 API")
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @Operation(summary = "청년 선택형 즉시 매칭 생성",
            description = "APPROVED 청년이 어르신을 선택하고 사전 인사말과 함께 매칭을 즉시 생성한다. 생성 시 status=MATCHED.")
    @PostMapping
    @PreAuthorize("hasRole('YOUTH')")
    public ResponseEntity<MatchResponse> createMatch(
            @AuthenticatedUser UUID youthUserId,
            @Valid @RequestBody MatchCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchService.createMatch(youthUserId, request));
    }

    @Operation(summary = "내 매칭 목록 조회",
            description = "청년은 본인이 생성한 매칭, 보호자는 본인이 등록한 어르신의 매칭 목록을 조회한다.")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('YOUTH','GUARDIAN')")
    public ResponseEntity<List<MatchSummaryResponse>> findMyMatches(@AuthenticatedUser UUID userId) {
        return ResponseEntity.ok(matchService.findMyMatches(userId));
    }

    @Operation(summary = "담당 인원 제한 조회",
            description = "APPROVED 청년이 현재 담당 인원과 최대 인원을 조회한다. 기본 최대 인원은 5.")
    @GetMapping("/limit/me")
    @PreAuthorize("hasRole('YOUTH')")
    public ResponseEntity<YouthMatchLimitResponse> getMyLimit(@AuthenticatedUser UUID youthUserId) {
        return ResponseEntity.ok(matchService.getMyLimit(youthUserId));
    }

    @Operation(summary = "매칭 상세 조회",
            description = "관리자는 모든 매칭, 청년/보호자는 본인 관련 매칭만 조회 가능.")
    @GetMapping("/{matchId}")
    @PreAuthorize("hasAnyRole('YOUTH','GUARDIAN','ADMIN')")
    public ResponseEntity<MatchDetailResponse> getMatch(
            @AuthenticatedUser UUID userId,
            @PathVariable("matchId") UUID matchId) {
        return ResponseEntity.ok(matchService.getMatch(userId, matchId));
    }
}
