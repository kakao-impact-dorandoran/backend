package com.dorandoran.backend.support;

import com.dorandoran.backend.domain.availabletime.AvailableTime;
import com.dorandoran.backend.domain.availabletime.AvailableTimeOwnerType;
import com.dorandoran.backend.domain.availabletime.AvailableTimeRepository;
import com.dorandoran.backend.domain.call.CallLog;
import com.dorandoran.backend.domain.call.CallLogRepository;
import com.dorandoran.backend.domain.call.CallLogStatus;
import com.dorandoran.backend.domain.device.DeliveryStatus;
import com.dorandoran.backend.domain.device.Device;
import com.dorandoran.backend.domain.device.DeviceRepository;
import com.dorandoran.backend.domain.device.DeviceStatus;
import com.dorandoran.backend.domain.device.DeviceType;
import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.elder.DifficultyLevel;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.elder.ElderRepository;
import com.dorandoran.backend.domain.elder.Gender;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchRepository;
import com.dorandoran.backend.domain.report.Report;
import com.dorandoran.backend.domain.report.ReportRepository;
import com.dorandoran.backend.domain.report.ReportType;
import com.dorandoran.backend.domain.schedule.Schedule;
import com.dorandoran.backend.domain.schedule.ScheduleRepository;
import com.dorandoran.backend.domain.schedule.ScheduleStatus;
import com.dorandoran.backend.domain.termination.MatchTerminationRequest;
import com.dorandoran.backend.domain.termination.MatchTerminationRequestRepository;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.domain.volunteer.YouthVolunteerStats;
import com.dorandoran.backend.domain.volunteer.YouthVolunteerStatsRepository;
import com.dorandoran.backend.domain.youth.YouthActivityStatus;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthMatchLimit;
import com.dorandoran.backend.domain.youth.YouthMatchLimitRepository;
import com.dorandoran.backend.domain.youth.YouthProfile;
import com.dorandoran.backend.domain.youth.YouthProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class TestDataFactory {

    public static final String DEFAULT_PASSWORD = "test1234!";

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final UserRepository userRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final ElderRepository elderRepository;
    private final DeviceRepository deviceRepository;
    private final MatchRepository matchRepository;
    private final ScheduleRepository scheduleRepository;
    private final AvailableTimeRepository availableTimeRepository;
    private final CallLogRepository callLogRepository;
    private final YouthVolunteerStatsRepository youthVolunteerStatsRepository;
    private final YouthMatchLimitRepository youthMatchLimitRepository;
    private final ReportRepository reportRepository;
    private final MatchTerminationRequestRepository matchTerminationRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(Role role, UserStatus status) {
        long seq = SEQUENCE.incrementAndGet();
        String email = "test-" + role.name().toLowerCase() + "-" + seq + "@example.com";
        return userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .name(role.name() + " #" + seq)
                .role(role)
                .phoneNumber(generatePhoneNumber(seq))
                .status(status)
                .build());
    }

    public User createActiveUser(Role role) {
        return createUser(role, UserStatus.ACTIVE);
    }

    public User createAdmin() {
        return createUser(Role.ADMIN, UserStatus.ACTIVE);
    }

    public User createGuardian() {
        return createUser(Role.GUARDIAN, UserStatus.ACTIVE);
    }

    public User createApprovedYouth() {
        User youth = createUser(Role.YOUTH, UserStatus.ACTIVE);
        createYouthProfile(youth, YouthApprovalStatus.APPROVED, null);
        return youth;
    }

    public User createPendingYouth() {
        User youth = createUser(Role.YOUTH, UserStatus.ACTIVE);
        createYouthProfile(youth, YouthApprovalStatus.PENDING, null);
        return youth;
    }

    public User createYouthUserWithoutProfile() {
        return createUser(Role.YOUTH, UserStatus.ACTIVE);
    }

    public User createRejectedYouth(String reason) {
        User youth = createUser(Role.YOUTH, UserStatus.ACTIVE);
        createYouthProfile(youth, YouthApprovalStatus.REJECTED, reason);
        return youth;
    }

    public User createSuspendedYouth() {
        User youth = createUser(Role.YOUTH, UserStatus.SUSPENDED);
        createYouthProfile(youth, YouthApprovalStatus.APPROVED, null);
        return youth;
    }

    public YouthProfile createYouthProfile(User youth,
                                           YouthApprovalStatus approvalStatus,
                                           String rejectionReason) {
        return youthProfileRepository.save(YouthProfile.builder()
                .youth(youth)
                .approvalStatus(approvalStatus)
                .rejectionReason(rejectionReason)
                .activityStatus(YouthActivityStatus.AVAILABLE)
                .isCompleted(approvalStatus == YouthApprovalStatus.APPROVED)
                .build());
    }

    public Elder createElder(User guardian) {
        long seq = SEQUENCE.incrementAndGet();
        return elderRepository.save(Elder.builder()
                .guardian(guardian)
                .name("어르신 #" + seq)
                .ageGroup("70대")
                .gender(Gender.FEMALE)
                .greetingComment("반갑습니다.")
                .phoneNumber(generatePhoneNumber(seq))
                .address("서울시 종로구 도란도란길 " + seq)
                .interests(List.of("산책", "꽃"))
                .preferredCallType(CallType.VIDEO)
                .difficultyLevel(DifficultyLevel.LOW)
                .requestNotes(null)
                .build());
    }

    public Device createRegisteredDevice(Elder elder, String deviceToken) {
        long seq = SEQUENCE.incrementAndGet();
        return deviceRepository.save(Device.builder()
                .elder(elder)
                .deviceType(DeviceType.TABLET)
                .serialNumber("TEST-SN-" + seq)
                .deviceToken(deviceToken)
                .deliveryStatus(DeliveryStatus.DELIVERED)
                .trackingNumber("TEST-TRK-" + seq)
                .deliveryAddress(elder.getAddress())
                .deliveredAt(LocalDateTime.now())
                .deviceStatus(DeviceStatus.REGISTERED)
                .registeredAt(LocalDateTime.now())
                .build());
    }

    public Match createMatchedMatch(User youth, Elder elder) {
        return matchRepository.save(Match.builder()
                .youth(youth)
                .elder(elder)
                .icebreakingMessage("안녕하세요, 잘 부탁드립니다.")
                .build());
    }

    public AvailableTime createYouthAvailableTime(User youth, LocalDateTime start, LocalDateTime end) {
        return availableTimeRepository.save(AvailableTime.builder()
                .ownerType(AvailableTimeOwnerType.YOUTH)
                .youth(youth)
                .registeredBy(youth)
                .startTime(start)
                .endTime(end)
                .isBooked(false)
                .build());
    }

    public AvailableTime createElderAvailableTime(Elder elder, User registeredBy,
                                                  LocalDateTime start, LocalDateTime end) {
        return availableTimeRepository.save(AvailableTime.builder()
                .ownerType(AvailableTimeOwnerType.ELDER)
                .elder(elder)
                .registeredBy(registeredBy)
                .startTime(start)
                .endTime(end)
                .isBooked(false)
                .build());
    }

    public Schedule createConfirmedSchedule(Match match, LocalDateTime start, LocalDateTime end) {
        return scheduleRepository.save(Schedule.builder()
                .match(match)
                .scheduledStartAt(start)
                .scheduledEndAt(end)
                .status(ScheduleStatus.CONFIRMED)
                .createdBy(match.getYouth())
                .build());
    }

    public CallLog createCompletedCallLog(Match match, Schedule schedule,
                                          LocalDateTime startAt, LocalDateTime endAt) {
        CallLog callLog = callLogRepository.save(CallLog.builder()
                .match(match)
                .schedule(schedule)
                .callType(CallType.VIDEO)
                .startAt(startAt)
                .status(CallLogStatus.PENDING)
                .build());
        // mark completed and set endAt manually because complete() uses LocalDateTime.now()
        callLog.complete();
        overrideEndAt(callLog, endAt);
        return callLogRepository.save(callLog);
    }

    public YouthVolunteerStats createYouthVolunteerStats(User youth,
                                                         int totalDurationMinutes,
                                                         int totalCertifiedHours) {
        YouthVolunteerStats stats = youthVolunteerStatsRepository.save(
                YouthVolunteerStats.builder().youth(youth).build());
        if (totalDurationMinutes > 0) {
            stats.addDurationMinutes(totalDurationMinutes);
        }
        if (totalCertifiedHours > 0) {
            stats.addCertifiedHours(totalCertifiedHours);
        }
        return youthVolunteerStatsRepository.save(stats);
    }

    public YouthMatchLimit createYouthMatchLimit(User youth, int currentCount) {
        YouthMatchLimit limit = youthMatchLimitRepository.save(
                YouthMatchLimit.builder().youth(youth).build());
        for (int i = 0; i < currentCount; i++) {
            limit.increment();
        }
        return youthMatchLimitRepository.save(limit);
    }

    public Report createPendingReport(User reporter, Match match) {
        return reportRepository.save(Report.builder()
                .reporterUser(reporter)
                .match(match)
                .reportType(ReportType.ETC)
                .content("테스트 신고 내용")
                .build());
    }

    public MatchTerminationRequest createRequestedTermination(User requester, Match match) {
        return matchTerminationRequestRepository.save(MatchTerminationRequest.builder()
                .match(match)
                .requesterUser(requester)
                .reason("테스트 사유")
                .build());
    }

    public UUID userId(User user) {
        return user.getId();
    }

    private static String generatePhoneNumber(long seq) {
        long suffix = seq % 100_000_000L;
        return String.format("010-%04d-%04d", (int) (suffix / 10_000), (int) (suffix % 10_000));
    }

    private static void overrideEndAt(CallLog callLog, LocalDateTime endAt) {
        try {
            Field field = CallLog.class.getDeclaredField("endAt");
            field.setAccessible(true);
            field.set(callLog, endAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to override CallLog.endAt", e);
        }
    }
}
