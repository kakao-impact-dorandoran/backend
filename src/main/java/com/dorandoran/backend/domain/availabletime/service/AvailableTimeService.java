package com.dorandoran.backend.domain.availabletime.service;

import com.dorandoran.backend.domain.availabletime.AvailableTime;
import com.dorandoran.backend.domain.availabletime.AvailableTimeOwnerType;
import com.dorandoran.backend.domain.availabletime.AvailableTimeRepository;
import com.dorandoran.backend.domain.availabletime.dto.AvailableTimeCreateRequest;
import com.dorandoran.backend.domain.availabletime.dto.AvailableTimeResponse;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.elder.ElderRepository;
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
public class AvailableTimeService {

    private final UserRepository userRepository;
    private final ElderRepository elderRepository;
    private final AvailableTimeRepository availableTimeRepository;

    @Transactional
    public AvailableTimeResponse createForYouth(UUID userId, AvailableTimeCreateRequest request) {
        validateRange(request);
        User youth = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (youth.getRole() != Role.YOUTH) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (availableTimeRepository.existsOverlapForYouth(youth.getId(),
                request.startTime(), request.endTime())) {
            throw new BusinessException(ErrorCode.AVAILABLE_TIME_OVERLAPPED);
        }
        AvailableTime saved = availableTimeRepository.save(AvailableTime.builder()
                .ownerType(AvailableTimeOwnerType.YOUTH)
                .youth(youth)
                .registeredBy(youth)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .isBooked(false)
                .build());
        return AvailableTimeResponse.from(saved);
    }

    @Transactional
    public AvailableTimeResponse createForElder(UUID userId, UUID elderId,
                                                AvailableTimeCreateRequest request) {
        validateRange(request);
        User guardian = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (guardian.getRole() != Role.GUARDIAN) {
            throw new BusinessException(ErrorCode.NOT_A_GUARDIAN_USER);
        }
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ELDER_NOT_FOUND));
        if (!elder.getGuardian().getId().equals(guardian.getId())) {
            throw new BusinessException(ErrorCode.ELDER_ACCESS_DENIED);
        }
        if (availableTimeRepository.existsOverlapForElder(elder.getId(),
                request.startTime(), request.endTime())) {
            throw new BusinessException(ErrorCode.AVAILABLE_TIME_OVERLAPPED);
        }
        AvailableTime saved = availableTimeRepository.save(AvailableTime.builder()
                .ownerType(AvailableTimeOwnerType.ELDER)
                .elder(elder)
                .registeredBy(guardian)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .isBooked(false)
                .build());
        return AvailableTimeResponse.from(saved);
    }

    public List<AvailableTimeResponse> find(UUID userId,
                                            AvailableTimeOwnerType ownerType, UUID ownerId) {
        if (ownerType == null || ownerId == null) {
            throw new BusinessException(ErrorCode.INVALID_AVAILABLE_TIME_QUERY);
        }
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<AvailableTime> list;
        if (ownerType == AvailableTimeOwnerType.YOUTH) {
            verifyYouthQuery(actor, ownerId);
            list = availableTimeRepository.findAllByYouth_IdOrderByStartTimeAsc(ownerId);
        } else {
            verifyElderQuery(actor, ownerId);
            list = availableTimeRepository.findAllByElder_IdOrderByStartTimeAsc(ownerId);
        }
        return list.stream().map(AvailableTimeResponse::from).toList();
    }

    private void verifyYouthQuery(User actor, UUID youthId) {
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (actor.getRole() == Role.YOUTH && actor.getId().equals(youthId)) {
            return;
        }
        throw new BusinessException(ErrorCode.AVAILABLE_TIME_ACCESS_DENIED);
    }

    private void verifyElderQuery(User actor, UUID elderId) {
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (actor.getRole() == Role.GUARDIAN) {
            Elder elder = elderRepository.findById(elderId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ELDER_NOT_FOUND));
            if (elder.getGuardian().getId().equals(actor.getId())) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.AVAILABLE_TIME_ACCESS_DENIED);
    }

    private void validateRange(AvailableTimeCreateRequest request) {
        if (request.startTime() == null || request.endTime() == null
                || !request.startTime().isBefore(request.endTime())) {
            throw new BusinessException(ErrorCode.INVALID_AVAILABLE_TIME_RANGE);
        }
    }
}
