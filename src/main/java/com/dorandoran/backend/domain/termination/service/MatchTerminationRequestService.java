package com.dorandoran.backend.domain.termination.service;

import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchRepository;
import com.dorandoran.backend.domain.match.MatchStatus;
import com.dorandoran.backend.domain.termination.MatchTerminationRequest;
import com.dorandoran.backend.domain.termination.MatchTerminationRequestRepository;
import com.dorandoran.backend.domain.termination.MatchTerminationRequestStatus;
import com.dorandoran.backend.domain.termination.dto.AdminMatchTerminationProcessRequest;
import com.dorandoran.backend.domain.termination.dto.AdminMatchTerminationResponse;
import com.dorandoran.backend.domain.termination.dto.MatchTerminationCreateRequest;
import com.dorandoran.backend.domain.termination.dto.MatchTerminationResponse;
import com.dorandoran.backend.domain.user.Role;
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
public class MatchTerminationRequestService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final MatchTerminationRequestRepository terminationRequestRepository;

    @Transactional
    public MatchTerminationResponse createRequest(UUID userId, UUID matchId, MatchTerminationCreateRequest request) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        verifyMatchRelation(requester, match);

        if (match.getStatus() == MatchStatus.ENDED) {
            throw new BusinessException(ErrorCode.MATCH_ALREADY_ENDED);
        }

        if (terminationRequestRepository.existsByMatch_IdAndStatus(matchId, MatchTerminationRequestStatus.REQUESTED)) {
            throw new BusinessException(ErrorCode.MATCH_TERMINATION_ALREADY_REQUESTED);
        }

        MatchTerminationRequest saved = terminationRequestRepository.save(
                MatchTerminationRequest.builder()
                        .match(match)
                        .requesterUser(requester)
                        .reason(request.reason())
                        .build()
        );

        return MatchTerminationResponse.from(saved);
    }

    public List<AdminMatchTerminationResponse> getRequests(MatchTerminationRequestStatus status) {
        List<MatchTerminationRequest> requests = status != null
                ? terminationRequestRepository.findAllByStatusOrderByCreatedAtDesc(status)
                : terminationRequestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream().map(AdminMatchTerminationResponse::from).toList();
    }

    @Transactional
    public AdminMatchTerminationResponse processRequest(UUID adminId, UUID requestId,
                                                        AdminMatchTerminationProcessRequest request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MatchTerminationRequest terminationRequest = terminationRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_TERMINATION_REQUEST_NOT_FOUND));

        MatchTerminationRequestStatus newStatus = request.status();
        if (newStatus == MatchTerminationRequestStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.INVALID_MATCH_TERMINATION_STATUS);
        }

        terminationRequest.process(newStatus, admin, request.adminMemo());

        if (newStatus == MatchTerminationRequestStatus.APPROVED) {
            terminationRequest.getMatch().end();
        }

        return AdminMatchTerminationResponse.from(terminationRequest);
    }

    private void verifyMatchRelation(User user, Match match) {
        Role role = user.getRole();
        if (role == Role.YOUTH && match.getYouth().getId().equals(user.getId())) {
            return;
        }
        if (role == Role.GUARDIAN && match.getElder().getGuardian().getId().equals(user.getId())) {
            return;
        }
        throw new BusinessException(ErrorCode.MATCH_TERMINATION_ACCESS_DENIED);
    }
}
