package com.dorandoran.backend.domain.help.controller;

import com.dorandoran.backend.domain.help.dto.HelpRequestCreateRequest;
import com.dorandoran.backend.domain.help.dto.HelpRequestResponse;
import com.dorandoran.backend.domain.help.service.HelpRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "HelpRequest", description = "어르신 도움 요청 API")
@RestController
@RequestMapping("/api/v1/help-requests")
@RequiredArgsConstructor
public class HelpRequestController {

    private final HelpRequestService helpRequestService;

    @Operation(summary = "도움 요청 생성",
            description = "전용 기기 인증으로 어르신이 도움 요청을 생성한다.")
    @PostMapping
    public ResponseEntity<HelpRequestResponse> createRequest(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody(required = false) HelpRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(helpRequestService.createRequest(authorization, request));
    }
}
