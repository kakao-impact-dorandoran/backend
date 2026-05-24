package com.dorandoran.backend.domain.report.service;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.elder.ElderRepository;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchRepository;
import com.dorandoran.backend.domain.report.Report;
import com.dorandoran.backend.domain.report.ReportRepository;
import com.dorandoran.backend.domain.report.ReportStatus;
import com.dorandoran.backend.domain.report.dto.AdminReportProcessRequest;
import com.dorandoran.backend.domain.report.dto.AdminReportResponse;
import com.dorandoran.backend.domain.report.dto.ReportCreateRequest;
import com.dorandoran.backend.domain.report.dto.ReportResponse;
import com.dorandoran.backend.domain.schedule.Schedule;
import com.dorandoran.backend.domain.schedule.ScheduleRepository;
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
public class ReportService {

    private final UserRepository userRepository;
    private final ElderRepository elderRepository;
    private final MatchRepository matchRepository;
    private final ScheduleRepository scheduleRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public ReportResponse createReport(UUID reporterUserId, ReportCreateRequest request) {
        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Match match = null;
        if (request.matchId() != null) {
            match = matchRepository.findById(request.matchId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            verifyMatchRelation(reporter, match);
        }

        Schedule schedule = null;
        if (request.scheduleId() != null) {
            schedule = scheduleRepository.findById(request.scheduleId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
            verifyScheduleRelation(reporter, schedule);
        }

        User targetUser = null;
        if (request.targetUserId() != null) {
            targetUser = userRepository.findById(request.targetUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }

        Elder targetElder = null;
        if (request.targetElderId() != null) {
            targetElder = elderRepository.findById(request.targetElderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ELDER_NOT_FOUND));
        }

        Report report = reportRepository.save(Report.builder()
                .reporterUser(reporter)
                .targetUser(targetUser)
                .targetElder(targetElder)
                .match(match)
                .schedule(schedule)
                .reportType(request.reportType())
                .content(request.content())
                .build());

        return ReportResponse.from(report);
    }

    public List<AdminReportResponse> getReports(ReportStatus status) {
        List<Report> reports = status != null
                ? reportRepository.findAllByStatusOrderByCreatedAtDesc(status)
                : reportRepository.findAllByOrderByCreatedAtDesc();
        return reports.stream().map(AdminReportResponse::from).toList();
    }

    @Transactional
    public AdminReportResponse processReport(UUID adminId, UUID reportId, AdminReportProcessRequest request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        ReportStatus newStatus = request.status();
        if (newStatus == ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_REPORT_STATUS);
        }

        report.process(newStatus, admin, request.adminMemo());
        return AdminReportResponse.from(report);
    }

    private void verifyMatchRelation(User user, Match match) {
        Role role = user.getRole();
        if (role == Role.YOUTH && match.getYouth().getId().equals(user.getId())) {
            return;
        }
        if (role == Role.GUARDIAN && match.getElder().getGuardian().getId().equals(user.getId())) {
            return;
        }
        throw new BusinessException(ErrorCode.REPORT_ACCESS_DENIED);
    }

    private void verifyScheduleRelation(User user, Schedule schedule) {
        verifyMatchRelation(user, schedule.getMatch());
    }
}
