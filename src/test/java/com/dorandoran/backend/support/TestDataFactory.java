package com.dorandoran.backend.support;

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
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.domain.youth.YouthActivityStatus;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;
import com.dorandoran.backend.domain.youth.YouthProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

    public UUID userId(User user) {
        return user.getId();
    }

    private static String generatePhoneNumber(long seq) {
        long suffix = seq % 100_000_000L;
        return String.format("010-%04d-%04d", (int) (suffix / 10_000), (int) (suffix % 10_000));
    }
}
