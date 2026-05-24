package com.dorandoran.backend.domain.help.service;

import com.dorandoran.backend.domain.device.dto.DeviceAuthContext;
import com.dorandoran.backend.domain.device.service.DeviceAuthService;
import com.dorandoran.backend.domain.help.HelpRequest;
import com.dorandoran.backend.domain.help.HelpRequestRepository;
import com.dorandoran.backend.domain.help.HelpRequestStatus;
import com.dorandoran.backend.domain.help.dto.AdminHelpRequestProcessRequest;
import com.dorandoran.backend.domain.help.dto.AdminHelpRequestResponse;
import com.dorandoran.backend.domain.help.dto.HelpRequestCreateRequest;
import com.dorandoran.backend.domain.help.dto.HelpRequestResponse;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HelpRequestService {

    private final HelpRequestRepository helpRequestRepository;
    private final UserRepository userRepository;
    private final DeviceAuthService deviceAuthService;

    @Transactional
    public HelpRequestResponse createRequest(String authorizationHeader, HelpRequestCreateRequest request) {
        DeviceAuthContext context = deviceAuthService.authenticate(authorizationHeader);

        HelpRequest saved = helpRequestRepository.save(
                HelpRequest.builder()
                        .elder(context.elder())
                        .device(context.device())
                        .requestType(request != null ? request.requestType() : null)
                        .deviceStatus(request != null ? request.deviceStatus() : null)
                        .build()
        );

        return HelpRequestResponse.from(saved);
    }

    public List<AdminHelpRequestResponse> getRequests(HelpRequestStatus status) {
        List<HelpRequest> requests = status != null
                ? helpRequestRepository.findAllByHandledStatusOrderByCreatedAtDesc(status)
                : helpRequestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream().map(AdminHelpRequestResponse::from).toList();
    }

    @Transactional
    public AdminHelpRequestResponse processRequest(UUID adminId, UUID helpRequestId,
                                                   AdminHelpRequestProcessRequest request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        HelpRequest helpRequest = helpRequestRepository.findById(helpRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HELP_REQUEST_NOT_FOUND));

        if (helpRequest.getHandledStatus() == HelpRequestStatus.HANDLED) {
            throw new BusinessException(ErrorCode.HELP_REQUEST_ALREADY_HANDLED);
        }

        if (request.status() != HelpRequestStatus.HANDLED) {
            throw new BusinessException(ErrorCode.INVALID_HELP_REQUEST_STATUS);
        }

        helpRequest.handle(admin);

        return AdminHelpRequestResponse.from(helpRequest);
    }
}
