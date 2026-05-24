package com.dorandoran.backend.domain.call.service;

import com.dorandoran.backend.domain.call.CallLog;
import com.dorandoran.backend.domain.call.CallLogRepository;
import com.dorandoran.backend.domain.call.dto.CallEndRequest;
import com.dorandoran.backend.domain.call.dto.CallLogResponse;
import com.dorandoran.backend.domain.call.dto.CallStartRequest;
import com.dorandoran.backend.domain.device.dto.DeviceAuthContext;
import com.dorandoran.backend.domain.device.service.DeviceAuthService;
import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchRepository;
import com.dorandoran.backend.domain.match.MatchStatus;
import com.dorandoran.backend.domain.schedule.Schedule;
import com.dorandoran.backend.domain.schedule.ScheduleRepository;
import com.dorandoran.backend.domain.schedule.ScheduleStatus;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallService {

    private final DeviceAuthService deviceAuthService;
    private final MatchRepository matchRepository;
    private final ScheduleRepository scheduleRepository;
    private final CallLogRepository callLogRepository;

    @Transactional
    public CallLogResponse startCall(String authorizationHeader, CallStartRequest request, CallType callType) {
        if (callType == null) {
            throw new BusinessException(ErrorCode.INVALID_CALL_TYPE);
        }
        DeviceAuthContext context = deviceAuthService.authenticate(authorizationHeader);

        Match match = matchRepository.findById(request.matchId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        Elder elder = context.elder();
        if (!match.getElder().getId().equals(elder.getId())) {
            throw new BusinessException(ErrorCode.CALL_MATCH_MISMATCH);
        }

        MatchStatus matchStatus = match.getStatus();
        if (matchStatus != MatchStatus.MATCHED && matchStatus != MatchStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.CALL_ACCESS_DENIED);
        }

        Schedule schedule = null;
        if (request.scheduleId() != null) {
            schedule = scheduleRepository.findById(request.scheduleId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
            if (!schedule.getMatch().getId().equals(match.getId())) {
                throw new BusinessException(ErrorCode.CALL_SCHEDULE_MISMATCH);
            }
            if (schedule.getStatus() != ScheduleStatus.CONFIRMED) {
                throw new BusinessException(ErrorCode.CALL_SCHEDULE_NOT_CONFIRMED);
            }
        }

        CallLog saved = callLogRepository.save(CallLog.builder()
                .match(match)
                .schedule(schedule)
                .callType(callType)
                .build());
        return CallLogResponse.from(saved);
    }

    @Transactional
    public CallLogResponse endCallByDevice(String authorizationHeader, UUID callLogId, CallEndRequest request) {
        DeviceAuthContext context = deviceAuthService.authenticate(authorizationHeader);
        CallLog callLog = loadCallLog(callLogId);

        if (!callLog.getMatch().getElder().getId().equals(context.elder().getId())) {
            throw new BusinessException(ErrorCode.CALL_ACCESS_DENIED);
        }
        return finishCall(callLog);
    }

    @Transactional
    public CallLogResponse endCallByYouth(UUID youthId, UUID callLogId, CallEndRequest request) {
        CallLog callLog = loadCallLog(callLogId);

        if (!callLog.getMatch().getYouth().getId().equals(youthId)) {
            throw new BusinessException(ErrorCode.CALL_ACCESS_DENIED);
        }
        return finishCall(callLog);
    }

    private CallLog loadCallLog(UUID callLogId) {
        return callLogRepository.findById(callLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CALL_LOG_NOT_FOUND));
    }

    private CallLogResponse finishCall(CallLog callLog) {
        if (callLog.isEnded()) {
            throw new BusinessException(ErrorCode.CALL_ALREADY_ENDED);
        }
        callLog.complete();
        return CallLogResponse.from(callLog);
    }
}
