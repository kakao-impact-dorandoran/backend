package com.dorandoran.backend.domain.call.controller;

import com.dorandoran.backend.domain.call.dto.CallEndRequest;
import com.dorandoran.backend.domain.call.dto.CallLogResponse;
import com.dorandoran.backend.domain.call.dto.CallStartRequest;
import com.dorandoran.backend.domain.call.service.CallService;
import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Call", description = "통화 시작/종료 API")
@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
public class CallController {

    private static final String DEVICE_SCHEME_PREFIX = "Device ";

    private final CallService callService;

    @Operation(summary = "화상 통화 시작",
            description = "전용 기기 인증으로 어르신이 화상 통화를 시작한다. CallLog가 PENDING 상태로 생성된다.")
    @PostMapping("/video")
    public ResponseEntity<CallLogResponse> startVideoCall(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody CallStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(callService.startCall(authorization, request, CallType.VIDEO));
    }

    @Operation(summary = "음성 통화 시작",
            description = "전용 기기 인증으로 어르신이 음성 통화를 시작한다. CallLog가 PENDING 상태로 생성된다.")
    @PostMapping("/audio")
    public ResponseEntity<CallLogResponse> startAudioCall(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody CallStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(callService.startCall(authorization, request, CallType.AUDIO));
    }

    @Operation(summary = "통화 종료",
            description = "Device token 또는 YOUTH JWT로 통화 종료를 기록한다. 이미 종료된 통화는 다시 종료할 수 없다.")
    @PatchMapping("/{callLogId}/end")
    public ResponseEntity<CallLogResponse> endCall(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable("callLogId") UUID callLogId,
            @RequestBody(required = false) CallEndRequest request) {
        if (authorization != null && authorization.startsWith(DEVICE_SCHEME_PREFIX)) {
            return ResponseEntity.ok(callService.endCallByDevice(authorization, callLogId, request));
        }
        UUID youthId = resolveAuthenticatedYouthId();
        return ResponseEntity.ok(callService.endCallByYouth(youthId, callLogId, request));
    }

    private UUID resolveAuthenticatedYouthId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UUID uuid)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        boolean isYouth = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_YOUTH".equals(a.getAuthority()));
        if (!isYouth) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return uuid;
    }
}
