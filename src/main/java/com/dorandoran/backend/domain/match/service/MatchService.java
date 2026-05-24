package com.dorandoran.backend.domain.match.service;

import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.elder.DifficultyLevel;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.elder.ElderRepository;
import com.dorandoran.backend.domain.elder.ElderStatus;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchRepository;
import com.dorandoran.backend.domain.match.MatchStatus;
import com.dorandoran.backend.domain.match.dto.MatchCreateRequest;
import com.dorandoran.backend.domain.match.dto.MatchDetailResponse;
import com.dorandoran.backend.domain.match.dto.MatchResponse;
import com.dorandoran.backend.domain.match.dto.MatchSummaryResponse;
import com.dorandoran.backend.domain.match.dto.MatchingElderDetailResponse;
import com.dorandoran.backend.domain.match.dto.MatchingElderListResponse;
import com.dorandoran.backend.domain.match.dto.YouthMatchLimitResponse;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthMatchLimit;
import com.dorandoran.backend.domain.youth.YouthMatchLimitRepository;
import com.dorandoran.backend.domain.youth.YouthProfile;
import com.dorandoran.backend.domain.youth.YouthProfileRepository;
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
public class MatchService {

    private static final List<MatchStatus> ACTIVE_STATUSES =
            List.of(MatchStatus.MATCHED, MatchStatus.IN_PROGRESS, MatchStatus.TERMINATION_REQUESTED);

    private final UserRepository userRepository;
    private final ElderRepository elderRepository;
    private final MatchRepository matchRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final YouthMatchLimitRepository youthMatchLimitRepository;

    public List<MatchingElderListResponse> findMatchableElders(UUID youthUserId,
                                                               String interest,
                                                               CallType preferredCallType,
                                                               DifficultyLevel difficultyLevel) {
        loadApprovedYouth(youthUserId);
        List<Elder> elders = elderRepository.findAllByStatusOrderByCreatedAtDesc(ElderStatus.AVAILABLE);
        return elders.stream()
                .filter(e -> preferredCallType == null || e.getPreferredCallType() == preferredCallType)
                .filter(e -> difficultyLevel == null || e.getDifficultyLevel() == difficultyLevel)
                .filter(e -> interest == null || interest.isBlank()
                        || (e.getInterests() != null && e.getInterests().contains(interest)))
                .map(MatchingElderListResponse::from)
                .toList();
    }

    public MatchingElderDetailResponse getMatchableElder(UUID youthUserId, UUID elderId) {
        loadApprovedYouth(youthUserId);
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ELDER_NOT_FOUND));
        if (elder.getStatus() != ElderStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.ELDER_NOT_AVAILABLE);
        }
        return MatchingElderDetailResponse.from(elder);
    }

    @Transactional
    public MatchResponse createMatch(UUID youthUserId, MatchCreateRequest request) {
        User youth = loadApprovedYouth(youthUserId);

        if (request.icebreakingMessage() == null || request.icebreakingMessage().isBlank()) {
            throw new BusinessException(ErrorCode.ICEBREAKING_MESSAGE_REQUIRED);
        }

        Elder elder = elderRepository.findById(request.elderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ELDER_NOT_FOUND));
        if (elder.getStatus() != ElderStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.ELDER_NOT_AVAILABLE);
        }
        if (matchRepository.existsByYouthAndElderAndStatusIn(youth, elder, ACTIVE_STATUSES)) {
            throw new BusinessException(ErrorCode.DUPLICATE_MATCH);
        }

        YouthMatchLimit limit = youthMatchLimitRepository.findByYouth(youth)
                .orElseGet(() -> youthMatchLimitRepository.save(
                        YouthMatchLimit.builder().youth(youth).build()));
        limit.increment();

        Match match = matchRepository.save(Match.builder()
                .youth(youth)
                .elder(elder)
                .icebreakingMessage(request.icebreakingMessage().trim())
                .build());
        elder.markMatched();
        return MatchResponse.from(match);
    }

    public List<MatchSummaryResponse> findMyMatches(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<Match> matches;
        if (user.getRole() == Role.YOUTH) {
            matches = matchRepository.findAllByYouthOrderByCreatedAtDesc(user);
        } else if (user.getRole() == Role.GUARDIAN) {
            matches = matchRepository.findAllByElder_GuardianOrderByCreatedAtDesc(user);
        } else {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return matches.stream().map(MatchSummaryResponse::from).toList();
    }

    public MatchDetailResponse getMatch(UUID userId, UUID matchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
        verifyMatchAccess(user, match);
        return MatchDetailResponse.from(match);
    }

    public YouthMatchLimitResponse getMyLimit(UUID youthUserId) {
        User youth = loadApprovedYouth(youthUserId);
        YouthMatchLimit limit = youthMatchLimitRepository.findByYouth(youth)
                .orElseGet(() -> {
                    YouthMatchLimit created = YouthMatchLimit.builder().youth(youth).build();
                    return youthMatchLimitRepository.saveAndFlush(created);
                });
        return YouthMatchLimitResponse.from(limit);
    }

    private void verifyMatchAccess(User user, Match match) {
        Role role = user.getRole();
        if (role == Role.ADMIN) {
            return;
        }
        if (role == Role.YOUTH && match.getYouth().getId().equals(user.getId())) {
            return;
        }
        if (role == Role.GUARDIAN
                && match.getElder().getGuardian().getId().equals(user.getId())) {
            return;
        }
        throw new BusinessException(ErrorCode.MATCH_ACCESS_DENIED);
    }

    private User loadApprovedYouth(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() != Role.YOUTH) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        YouthProfile profile = youthProfileRepository.findByYouth(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.YOUTH_PENDING_APPROVAL));
        YouthApprovalStatus approval = profile.getApprovalStatus();
        if (approval == YouthApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.YOUTH_PENDING_APPROVAL);
        }
        if (approval == YouthApprovalStatus.REJECTED) {
            throw new BusinessException(ErrorCode.YOUTH_REJECTED);
        }
        return user;
    }
}
