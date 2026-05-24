package com.dorandoran.backend.domain.elder.service;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.elder.ElderRepository;
import com.dorandoran.backend.domain.elder.dto.ElderCreateRequest;
import com.dorandoran.backend.domain.elder.dto.ElderResponse;
import com.dorandoran.backend.domain.elder.dto.ElderUpdateRequest;
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
public class ElderService {

    private final UserRepository userRepository;
    private final ElderRepository elderRepository;

    @Transactional
    public ElderResponse create(UUID guardianId, ElderCreateRequest request) {
        User guardian = loadGuardian(guardianId);
        Elder elder = Elder.builder()
                .guardian(guardian)
                .name(request.name())
                .ageGroup(request.ageGroup())
                .gender(request.gender())
                .profileImageUrl(request.profileImageUrl())
                .greetingComment(request.greetingComment())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .interests(request.interests())
                .preferredCallType(request.preferredCallType())
                .difficultyLevel(request.difficultyLevel())
                .requestNotes(request.requestNotes())
                .build();
        Elder saved = elderRepository.save(elder);
        return ElderResponse.from(saved);
    }

    public List<ElderResponse> findMyElders(UUID guardianId) {
        User guardian = loadGuardian(guardianId);
        return elderRepository.findAllByGuardianOrderByCreatedAtDesc(guardian).stream()
                .map(ElderResponse::from)
                .toList();
    }

    @Transactional
    public ElderResponse update(UUID guardianId, UUID elderId, ElderUpdateRequest request) {
        User guardian = loadGuardian(guardianId);
        Elder elder = elderRepository.findByIdAndGuardian(elderId, guardian)
                .orElseThrow(() -> {
                    if (elderRepository.existsById(elderId)) {
                        return new BusinessException(ErrorCode.ELDER_ACCESS_DENIED);
                    }
                    return new BusinessException(ErrorCode.ELDER_NOT_FOUND);
                });
        elder.update(
                request.name(),
                request.ageGroup(),
                request.gender(),
                request.profileImageUrl(),
                request.greetingComment(),
                request.phoneNumber(),
                request.address(),
                request.interests(),
                request.preferredCallType(),
                request.difficultyLevel(),
                request.requestNotes()
        );
        return ElderResponse.from(elder);
    }

    private User loadGuardian(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() != Role.GUARDIAN) {
            throw new BusinessException(ErrorCode.NOT_A_GUARDIAN_USER);
        }
        return user;
    }
}
