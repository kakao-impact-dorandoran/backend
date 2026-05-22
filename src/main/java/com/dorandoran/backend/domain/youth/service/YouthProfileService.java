package com.dorandoran.backend.domain.youth.service;

import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.youth.YouthActivityStatus;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;
import com.dorandoran.backend.domain.youth.YouthProfileRepository;
import com.dorandoran.backend.domain.youth.dto.YouthProfileCreateRequest;
import com.dorandoran.backend.domain.youth.dto.YouthProfileCreateResponse;
import com.dorandoran.backend.domain.youth.dto.YouthProfileResponse;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import com.dorandoran.backend.global.security.ForbiddenWordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class YouthProfileService {

    private static final int MAX_KEYWORD_COUNT = 5;

    private final UserRepository userRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final ForbiddenWordFilter forbiddenWordFilter;

    @Transactional
    public YouthProfileCreateResponse create(UUID userId, YouthProfileCreateRequest request) {
        User youth = loadYouth(userId);
        if (youthProfileRepository.existsByYouth(youth)) {
            throw new BusinessException(ErrorCode.YOUTH_PROFILE_ALREADY_EXISTS);
        }

        List<String> keywords = request.keywords();
        if (keywords != null && keywords.size() > MAX_KEYWORD_COUNT) {
            throw new BusinessException(ErrorCode.KEYWORD_LIMIT_EXCEEDED);
        }
        if (forbiddenWordFilter.contains(request.greetingComment())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_WORD_INCLUDED);
        }

        YouthProfile profile = YouthProfile.builder()
                .youth(youth)
                .profileImageUrl(request.profileImageUrl())
                .keywords(keywords)
                .greetingComment(request.greetingComment())
                .voiceSampleUrl(request.voiceSampleUrl())
                .approvalStatus(YouthApprovalStatus.PENDING)
                .rejectionReason(null)
                .activityStatus(YouthActivityStatus.AVAILABLE)
                .isCompleted(true)
                .build();
        YouthProfile saved = youthProfileRepository.save(profile);
        return YouthProfileCreateResponse.from(saved);
    }

    public YouthProfileResponse getMyProfile(UUID userId) {
        User youth = loadYouth(userId);
        YouthProfile profile = youthProfileRepository.findByYouth(youth)
                .orElseThrow(() -> new BusinessException(ErrorCode.YOUTH_PROFILE_NOT_FOUND));
        return YouthProfileResponse.from(profile);
    }

    private User loadYouth(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() != Role.YOUTH) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return user;
    }
}
