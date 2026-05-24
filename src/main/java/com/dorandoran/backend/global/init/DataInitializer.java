package com.dorandoran.backend.global.init;

import com.dorandoran.backend.domain.availabletime.AvailableTime;
import com.dorandoran.backend.domain.availabletime.AvailableTimeOwnerType;
import com.dorandoran.backend.domain.availabletime.AvailableTimeRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "test1234!";

    private final UserRepository userRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final ElderRepository elderRepository;
    private final DeviceRepository deviceRepository;
    private final AvailableTimeRepository availableTimeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedUser("youth@test.com", "청년 테스터", Role.YOUTH, "010-1111-1111", UserStatus.ACTIVE);
        User guardian = seedUser("guardian@test.com", "보호자 테스터", Role.GUARDIAN, "010-2222-2222", UserStatus.ACTIVE);
        seedUser("admin@test.com", "관리자 테스터", Role.ADMIN, "010-3333-3333", UserStatus.ACTIVE);

        User approved = seedUser("youth_approved@test.com", "청년 승인완료", Role.YOUTH, "010-1000-0001", UserStatus.ACTIVE);
        User pending = seedUser("youth_pending@test.com", "청년 승인대기", Role.YOUTH, "010-1000-0002", UserStatus.ACTIVE);
        User rejected = seedUser("youth_rejected@test.com", "청년 반려", Role.YOUTH, "010-1000-0003", UserStatus.ACTIVE);
        User banned = seedUser("youth_banned@test.com", "청년 제재", Role.YOUTH, "010-1000-0004", UserStatus.SUSPENDED);

        seedYouthProfile(approved, YouthApprovalStatus.APPROVED, null);
        seedYouthProfile(pending, YouthApprovalStatus.PENDING, null);
        seedYouthProfile(rejected, YouthApprovalStatus.REJECTED, "프로필 정보가 부족합니다.");
        seedYouthProfile(banned, YouthApprovalStatus.APPROVED, null);

        Elder elder = seedElder(guardian);
        if (elder != null) {
            seedDevice(elder);
            seedAvailableTimes(approved, elder, guardian);
        }
    }



    private void seedAvailableTimes(User youth, Elder elder, User guardian) {
        LocalDateTime start = LocalDateTime.now()
                .plusDays(1)
                .withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        if (!availableTimeRepository.existsOverlapForYouth(youth.getId(), start, end)) {
            availableTimeRepository.save(AvailableTime.builder()
                    .ownerType(AvailableTimeOwnerType.YOUTH)
                    .youth(youth)
                    .registeredBy(youth)
                    .startTime(start)
                    .endTime(end)
                    .isBooked(false)
                    .build());
            log.info("[seed] Created AvailableTime YOUTH {} ({} ~ {})",
                    youth.getEmail(), start, end);
        }
        if (!availableTimeRepository.existsOverlapForElder(elder.getId(), start, end)) {
            availableTimeRepository.save(AvailableTime.builder()
                    .ownerType(AvailableTimeOwnerType.ELDER)
                    .elder(elder)
                    .registeredBy(guardian)
                    .startTime(start)
                    .endTime(end)
                    .isBooked(false)
                    .build());
            log.info("[seed] Created AvailableTime ELDER {} ({} ~ {})",
                    elder.getName(), start, end);
        }
    }

    private User seedUser(String email, String name, Role role, String phone, UserStatus status) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User saved = userRepository.save(User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .name(name)
                    .role(role)
                    .phoneNumber(phone)
                    .status(status)
                    .build());
            log.info("[seed] Created {} account: {} / {} (status={})", role, email, DEFAULT_PASSWORD, status);
            return saved;
        });
    }

    private Elder seedElder(User guardian) {
        List<Elder> existing = elderRepository.findAllByGuardian(guardian);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        Elder elder = elderRepository.save(Elder.builder()
                .guardian(guardian)
                .name("박도란")
                .ageGroup("70대")
                .gender(Gender.FEMALE)
                .profileImageUrl(null)
                .greetingComment("꽃과 산책 이야기를 좋아합니다.")
                .phoneNumber("010-1111-1111")
                .address("서울시 종로구 도란도란길 1")
                .interests(List.of("산책", "드라마", "꽃"))
                .preferredCallType(CallType.VIDEO)
                .difficultyLevel(DifficultyLevel.LOW)
                .requestNotes("천천히 말해주시면 좋습니다.")
                .build());
        log.info("[seed] Created Elder {} for guardian {}", elder.getName(), guardian.getEmail());
        return elder;
    }

    private Device seedDevice(Elder elder) {
        return deviceRepository.findByElder_Id(elder.getId()).orElseGet(() -> {
            Device device = deviceRepository.save(Device.builder()
                    .elder(elder)
                    .deviceType(DeviceType.TABLET)
                    .serialNumber("SEED-TABLET-0001")
                    .deviceToken("seed-device-token-0001")
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .trackingNumber("SEED-TRK-0001")
                    .deliveryAddress(elder.getAddress())
                    .deliveredAt(LocalDateTime.now())
                    .deviceStatus(DeviceStatus.REGISTERED)
                    .registeredAt(LocalDateTime.now())
                    .lastConnectedAt(null)
                    .build());
            log.info("[seed] Created Device {} for elder {} (token={})",
                    device.getSerialNumber(), elder.getName(), device.getDeviceToken());
            return device;
        });
    }

    private void seedYouthProfile(User youth, YouthApprovalStatus approvalStatus, String rejectionReason) {
        if (youthProfileRepository.existsByYouth(youth)) {
            return;
        }
        youthProfileRepository.save(YouthProfile.builder()
                .youth(youth)
                .approvalStatus(approvalStatus)
                .rejectionReason(rejectionReason)
                .activityStatus(YouthActivityStatus.AVAILABLE)
                .isCompleted(approvalStatus == YouthApprovalStatus.APPROVED)
                .build());
        log.info("[seed] Created YouthProfile for {} (approval={}, reason={})",
                youth.getEmail(), approvalStatus, rejectionReason);
    }
}
