package com.dorandoran.backend.domain.admin.service;

import com.dorandoran.backend.domain.admin.dto.AdminYouthApprovalRequest;
import com.dorandoran.backend.domain.admin.dto.AdminYouthApprovalResponse;
import com.dorandoran.backend.domain.admin.dto.AdminYouthDetailResponse;
import com.dorandoran.backend.domain.admin.dto.AdminYouthListResponse;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
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
public class AdminYouthService {

    private final UserRepository userRepository;
    private final YouthProfileRepository youthProfileRepository;

    public List<AdminYouthListResponse> findYouths(YouthApprovalStatus approvalStatus) {
        List<YouthProfile> profiles = (approvalStatus == null)
                ? youthProfileRepository.findAllByOrderByCreatedAtDesc()
                : youthProfileRepository.findAllByApprovalStatusOrderByCreatedAtDesc(approvalStatus);
        return profiles.stream().map(AdminYouthListResponse::from).toList();
    }

    public AdminYouthDetailResponse getYouthDetail(UUID youthId) {
        User youth = userRepository.findById(youthId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (youth.getRole() != Role.YOUTH) {
            throw new BusinessException(ErrorCode.NOT_A_YOUTH_USER);
        }
        YouthProfile profile = youthProfileRepository.findByYouth(youth)
                .orElseThrow(() -> new BusinessException(ErrorCode.YOUTH_PROFILE_NOT_FOUND));
        return AdminYouthDetailResponse.from(profile);
    }

    @Transactional
    public AdminYouthApprovalResponse changeApproval(UUID youthId, AdminYouthApprovalRequest request) {
        YouthApprovalStatus target = request.approvalStatus();
        if (target == null || target == YouthApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_APPROVAL_STATUS);
        }

        User youth = userRepository.findById(youthId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (youth.getRole() != Role.YOUTH) {
            throw new BusinessException(ErrorCode.NOT_A_YOUTH_USER);
        }
        YouthProfile profile = youthProfileRepository.findByYouth(youth)
                .orElseThrow(() -> new BusinessException(ErrorCode.YOUTH_PROFILE_NOT_FOUND));

        if (target == YouthApprovalStatus.APPROVED) {
            profile.approve();
        } else {
            String reason = request.rejectionReason();
            if (reason == null || reason.isBlank()) {
                throw new BusinessException(ErrorCode.REJECTION_REASON_REQUIRED);
            }
            profile.reject(reason);
        }

        return AdminYouthApprovalResponse.from(profile);
    }
}
