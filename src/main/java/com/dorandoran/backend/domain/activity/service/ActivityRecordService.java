package com.dorandoran.backend.domain.activity.service;

import com.dorandoran.backend.domain.activity.ActivityRecord;
import com.dorandoran.backend.domain.activity.ActivityRecordRepository;
import com.dorandoran.backend.domain.activity.dto.ActivityRecordCreateRequest;
import com.dorandoran.backend.domain.activity.dto.ActivityRecordResponse;
import com.dorandoran.backend.domain.activity.dto.ActivityRecordSummaryResponse;
import com.dorandoran.backend.domain.call.CallLog;
import com.dorandoran.backend.domain.call.CallLogRepository;
import com.dorandoran.backend.domain.call.CallLogStatus;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchRepository;
import com.dorandoran.backend.domain.match.MatchStatus;
import com.dorandoran.backend.domain.schedule.Schedule;
import com.dorandoran.backend.domain.schedule.ScheduleRepository;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.domain.volunteer.YouthVolunteerStats;
import com.dorandoran.backend.domain.volunteer.service.YouthVolunteerStatsService;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;
import com.dorandoran.backend.domain.youth.YouthProfileRepository;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityRecordService {

    private final UserRepository userRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final MatchRepository matchRepository;
    private final ScheduleRepository scheduleRepository;
    private final CallLogRepository callLogRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final YouthVolunteerStatsService statsService;

    @Transactional
    public ActivityRecordResponse createActivityRecord(UUID youthUserId, ActivityRecordCreateRequest request) {
        User youth = loadApprovedYouth(youthUserId);

        Match match = matchRepository.findById(request.matchId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
        if (!match.getYouth().getId().equals(youth.getId())) {
            throw new BusinessException(ErrorCode.MATCH_ACCESS_DENIED);
        }
        MatchStatus matchStatus = match.getStatus();
        if (matchStatus != MatchStatus.MATCHED
                && matchStatus != MatchStatus.IN_PROGRESS
                && matchStatus != MatchStatus.ENDED) {
            throw new BusinessException(ErrorCode.ACTIVITY_MATCH_NOT_RECORDABLE);
        }

        Schedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (!schedule.getMatch().getId().equals(match.getId())) {
            throw new BusinessException(ErrorCode.ACTIVITY_SCHEDULE_MISMATCH);
        }
        if (activityRecordRepository.existsBySchedule_Id(schedule.getId())) {
            throw new BusinessException(ErrorCode.ACTIVITY_RECORD_DUPLICATED);
        }

        CallLog callLog = null;
        Integer resolvedDurationMinutes = request.durationMinutes();
        LocalDateTime resolvedStartAt = request.actualStartAt();
        LocalDateTime resolvedEndAt = request.actualEndAt();

        if (request.callLogId() != null) {
            callLog = callLogRepository.findById(request.callLogId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CALL_LOG_NOT_FOUND));
            if (!callLog.getMatch().getId().equals(match.getId())) {
                throw new BusinessException(ErrorCode.ACTIVITY_CALL_LOG_MISMATCH);
            }
            if (callLog.getStatus() != CallLogStatus.COMPLETED) {
                throw new BusinessException(ErrorCode.CALL_LOG_NOT_COMPLETED);
            }
            if (activityRecordRepository.existsByCallLog_Id(callLog.getId())) {
                throw new BusinessException(ErrorCode.ACTIVITY_RECORD_DUPLICATED_CALL_LOG);
            }
            if (callLog.getStartAt() != null && callLog.getEndAt() != null) {
                resolvedStartAt = callLog.getStartAt();
                resolvedEndAt = callLog.getEndAt();
                long minutes = Duration.between(resolvedStartAt, resolvedEndAt).toMinutes();
                if (minutes <= 0) {
                    throw new BusinessException(ErrorCode.INVALID_ACTIVITY_DURATION);
                }
                resolvedDurationMinutes = (int) minutes;
            }
        }

        if (request.isCompleted()) {
            if (resolvedDurationMinutes == null) {
                if (resolvedStartAt == null || resolvedEndAt == null) {
                    throw new BusinessException(ErrorCode.INVALID_ACTIVITY_DURATION);
                }
                long minutes = Duration.between(resolvedStartAt, resolvedEndAt).toMinutes();
                if (minutes <= 0) {
                    throw new BusinessException(ErrorCode.INVALID_ACTIVITY_DURATION);
                }
                resolvedDurationMinutes = (int) minutes;
            }
            if (resolvedDurationMinutes <= 0) {
                throw new BusinessException(ErrorCode.INVALID_ACTIVITY_DURATION);
            }
            if (resolvedStartAt != null && resolvedEndAt != null
                    && !resolvedStartAt.isBefore(resolvedEndAt)) {
                throw new BusinessException(ErrorCode.INVALID_ACTIVITY_DURATION);
            }
        }

        ActivityRecord saved = activityRecordRepository.save(ActivityRecord.builder()
                .match(match)
                .schedule(schedule)
                .youth(youth)
                .elder(match.getElder())
                .callLog(callLog)
                .isCompleted(request.isCompleted())
                .actualStartAt(resolvedStartAt)
                .actualEndAt(resolvedEndAt)
                .durationMinutes(resolvedDurationMinutes)
                .notes(request.notes())
                .build());

        YouthVolunteerStats stats = statsService.getOrCreateForYouth(youth);
        if (request.isCompleted() && resolvedDurationMinutes != null && resolvedDurationMinutes > 0) {
            stats.addDurationMinutes(resolvedDurationMinutes);
        }

        return ActivityRecordResponse.from(saved, stats.getTotalDurationMinutes());
    }

    public List<ActivityRecordSummaryResponse> findActivityRecords(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        Role role = user.getRole();
        if (role == Role.YOUTH) {
            return activityRecordRepository.findAllByYouth_IdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .map(ActivityRecordSummaryResponse::from)
                    .toList();
        }
        if (role == Role.GUARDIAN) {
            return activityRecordRepository.findAllByElder_Guardian_IdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .map(ActivityRecordSummaryResponse::forGuardian)
                    .toList();
        }
        if (role == Role.ADMIN) {
            return activityRecordRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .map(ActivityRecordSummaryResponse::from)
                    .toList();
        }
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
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
