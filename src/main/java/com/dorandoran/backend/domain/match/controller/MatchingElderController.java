package com.dorandoran.backend.domain.match.controller;

import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.elder.DifficultyLevel;
import com.dorandoran.backend.domain.match.dto.MatchingElderDetailResponse;
import com.dorandoran.backend.domain.match.dto.MatchingElderListResponse;
import com.dorandoran.backend.domain.match.service.MatchService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Matching Elder", description = "청년용 매칭 가능한 어르신 조회 API")
@RestController
@RequestMapping("/api/v1/matching/elders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('YOUTH')")
public class MatchingElderController {

    private final MatchService matchService;

    @Operation(summary = "청년용 매칭 가능 어르신 목록 조회",
            description = "APPROVED 청년만 호출 가능. 주소/연락처 등 민감정보는 응답에 포함되지 않는다. AvailableTime 기반 필터는 Step F에서 구현 예정.")
    @GetMapping
    public ResponseEntity<List<MatchingElderListResponse>> findMatchableElders(
            @AuthenticatedUser UUID youthUserId,
            @RequestParam(value = "interest", required = false) String interest,
            @RequestParam(value = "preferredCallType", required = false) CallType preferredCallType,
            @RequestParam(value = "difficultyLevel", required = false) DifficultyLevel difficultyLevel) {
        return ResponseEntity.ok(
                matchService.findMatchableElders(youthUserId, interest, preferredCallType, difficultyLevel));
    }

    @Operation(summary = "청년용 어르신 상세 조회",
            description = "APPROVED 청년만 호출 가능. AVAILABLE 상태 어르신만 조회 허용. 민감정보는 응답에 포함되지 않는다.")
    @GetMapping("/{elderId}")
    public ResponseEntity<MatchingElderDetailResponse> getMatchableElder(
            @AuthenticatedUser UUID youthUserId,
            @PathVariable("elderId") UUID elderId) {
        return ResponseEntity.ok(matchService.getMatchableElder(youthUserId, elderId));
    }
}
