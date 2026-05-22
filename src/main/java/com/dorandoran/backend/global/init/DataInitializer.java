package com.dorandoran.backend.global.init;

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

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "test1234!";

    private final UserRepository userRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedUser("youth@test.com", "청년 테스터", Role.YOUTH, "010-1111-1111", UserStatus.ACTIVE);
        seedUser("guardian@test.com", "보호자 테스터", Role.GUARDIAN, "010-2222-2222", UserStatus.ACTIVE);
        seedUser("admin@test.com", "관리자 테스터", Role.ADMIN, "010-3333-3333", UserStatus.ACTIVE);

        User approved = seedUser("youth_approved@test.com", "청년 승인완료", Role.YOUTH, "010-1000-0001", UserStatus.ACTIVE);
        User pending = seedUser("youth_pending@test.com", "청년 승인대기", Role.YOUTH, "010-1000-0002", UserStatus.ACTIVE);
        User rejected = seedUser("youth_rejected@test.com", "청년 반려", Role.YOUTH, "010-1000-0003", UserStatus.ACTIVE);
        User banned = seedUser("youth_banned@test.com", "청년 제재", Role.YOUTH, "010-1000-0004", UserStatus.SUSPENDED);

        seedYouthProfile(approved, YouthApprovalStatus.APPROVED, null);
        seedYouthProfile(pending, YouthApprovalStatus.PENDING, null);
        seedYouthProfile(rejected, YouthApprovalStatus.REJECTED, "프로필 정보가 부족합니다.");
        seedYouthProfile(banned, YouthApprovalStatus.APPROVED, null);
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
