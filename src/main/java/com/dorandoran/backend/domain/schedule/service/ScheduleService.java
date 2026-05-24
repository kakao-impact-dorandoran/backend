package com.dorandoran.backend.domain.schedule.service;

import com.dorandoran.backend.domain.availabletime.AvailableTimeRepository;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchRepository;
import com.dorandoran.backend.domain.match.MatchStatus;
import com.dorandoran.backend.domain.schedule.Schedule;
import com.dorandoran.backend.domain.schedule.ScheduleRepository;
import com.dorandoran.backend.domain.schedule.dto.ScheduleCancelRequest;
import com.dorandoran.backend.domain.schedule.dto.ScheduleCreateRequest;
import com.dorandoran.backend.domain.schedule.dto.ScheduleResponse;
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
public class ScheduleService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ScheduleRepository scheduleRepository;
    private final AvailableTimeRepository availableTimeRepository;

    @Transactional
    public ScheduleResponse create(UUID userId, ScheduleCreateRequest request) {
        if (request.scheduledStartAt() == null || request.scheduledEndAt() == null
                || !request.scheduledStartAt().isBefore(request.scheduledEndAt())) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_TIME_RANGE);
        }
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (actor.getRole() != Role.YOUTH && actor.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Match match = matchRepository.findById(request.matchId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        if (actor.getRole() == Role.YOUTH
                && !match.getYouth().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.MATCH_ACCESS_DENIED);
        }

        MatchStatus matchStatus = match.getStatus();
        if (matchStatus != MatchStatus.MATCHED && matchStatus != MatchStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.MATCH_NOT_SCHEDULABLE);
        }

        UUID youthId = match.getYouth().getId();
        UUID elderId = match.getElder().getId();

        boolean withinYouthAvailable = availableTimeRepository.existsContainingForYouth(
                youthId, request.scheduledStartAt(), request.scheduledEndAt());
        boolean withinElderAvailable = availableTimeRepository.existsContainingForElder(
                elderId, request.scheduledStartAt(), request.scheduledEndAt());
        if (!withinYouthAvailable || !withinElderAvailable) {
            throw new BusinessException(ErrorCode.SCHEDULE_OUT_OF_AVAILABLE_TIME);
        }

        if (scheduleRepository.existsConflictForYouth(youthId,
                request.scheduledStartAt(), request.scheduledEndAt())
                || scheduleRepository.existsConflictForElder(elderId,
                request.scheduledStartAt(), request.scheduledEndAt())) {
            throw new BusinessException(ErrorCode.SCHEDULE_CONFLICT);
        }

        Schedule saved = scheduleRepository.save(Schedule.builder()
                .match(match)
                .scheduledStartAt(request.scheduledStartAt())
                .scheduledEndAt(request.scheduledEndAt())
                .createdBy(actor)
                .build());
        // isBooked 처리는 단일 슬롯 소모 정책일 때만 의미가 있어 이번 MVP에서는 변경하지 않는다.
        return ScheduleResponse.from(saved);
    }

    public List<ScheduleResponse> findMy(UUID userId) {
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<Schedule> schedules;
        if (actor.getRole() == Role.YOUTH) {
            schedules = scheduleRepository
                    .findAllByMatch_Youth_IdOrderByScheduledStartAtAsc(actor.getId());
        } else if (actor.getRole() == Role.GUARDIAN) {
            schedules = scheduleRepository
                    .findAllByMatch_Elder_Guardian_IdOrderByScheduledStartAtAsc(actor.getId());
        } else if (actor.getRole() == Role.ADMIN) {
            schedules = scheduleRepository.findAllByOrderByScheduledStartAtAsc();
        } else {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return schedules.stream().map(ScheduleResponse::from).toList();
    }

    @Transactional
    public ScheduleResponse cancel(UUID userId, UUID scheduleId, ScheduleCancelRequest request) {
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (actor.getRole() != Role.YOUTH && actor.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (actor.getRole() == Role.YOUTH
                && !schedule.getMatch().getYouth().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ACCESS_DENIED);
        }
        if (schedule.isCanceled()) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_CANCELED);
        }
        if (schedule.isCompleted()) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_COMPLETED);
        }

        String reason = request != null ? request.cancelReason() : null;
        schedule.cancel(actor, reason);
        return ScheduleResponse.from(schedule);
    }
}
