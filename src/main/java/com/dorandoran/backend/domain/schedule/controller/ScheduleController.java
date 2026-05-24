package com.dorandoran.backend.domain.schedule.controller;

import com.dorandoran.backend.domain.schedule.dto.ScheduleCancelRequest;
import com.dorandoran.backend.domain.schedule.dto.ScheduleCreateRequest;
import com.dorandoran.backend.domain.schedule.dto.ScheduleResponse;
import com.dorandoran.backend.domain.schedule.service.ScheduleService;
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

@Tag(name = "Schedule", description = "대화 일정 API")
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "대화 일정 생성",
            description = "매칭된 청년/어르신의 가능 시간이 모두 포함되고 기존 일정과 충돌하지 않을 때 CONFIRMED 일정을 생성한다.")
    @PostMapping
    @PreAuthorize("hasAnyRole('YOUTH','ADMIN')")
    public ResponseEntity<ScheduleResponse> create(
            @AuthenticatedUser UUID userId,
            @Valid @RequestBody ScheduleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.create(userId, request));
    }

    @Operation(summary = "내 일정 목록 조회",
            description = "YOUTH/GUARDIAN/ADMIN 역할별로 관련 일정을 조회한다.")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('YOUTH','GUARDIAN','ADMIN')")
    public ResponseEntity<List<ScheduleResponse>> findMy(@AuthenticatedUser UUID userId) {
        return ResponseEntity.ok(scheduleService.findMy(userId));
    }

    @Operation(summary = "일정 취소",
            description = "확정된 일정을 취소한다. YOUTH는 본인 일정만, ADMIN은 모든 일정을 취소할 수 있다.")
    @PatchMapping("/{scheduleId}/cancel")
    @PreAuthorize("hasAnyRole('YOUTH','ADMIN')")
    public ResponseEntity<ScheduleResponse> cancel(
            @AuthenticatedUser UUID userId,
            @PathVariable("scheduleId") UUID scheduleId,
            @RequestBody(required = false) @Valid ScheduleCancelRequest request) {
        return ResponseEntity.ok(scheduleService.cancel(userId, scheduleId, request));
    }
}
