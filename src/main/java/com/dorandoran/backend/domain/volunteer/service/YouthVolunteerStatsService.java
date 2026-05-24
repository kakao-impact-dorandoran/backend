package com.dorandoran.backend.domain.volunteer.service;

import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.domain.volunteer.YouthVolunteerStats;
import com.dorandoran.backend.domain.volunteer.YouthVolunteerStatsRepository;
import com.dorandoran.backend.domain.volunteer.dto.YouthVolunteerStatsResponse;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;
import com.dorandoran.backend.domain.youth.YouthProfileRepository;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class YouthVolunteerStatsService {

    private final UserRepository userRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final YouthVolunteerStatsRepository statsRepository;

    @Transactional
    public YouthVolunteerStats getOrCreateForYouth(User youth) {
        return statsRepository.findByYouth(youth)
                .orElseGet(() -> statsRepository.save(
                        YouthVolunteerStats.builder().youth(youth).build()));
    }

    public YouthVolunteerStatsResponse getMyStats(UUID youthUserId) {
        User youth = loadApprovedYouth(youthUserId);
        return statsRepository.findByYouth(youth)
                .map(YouthVolunteerStatsResponse::from)
                .orElseGet(() -> YouthVolunteerStatsResponse.empty(youth.getId()));
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
