package com.dorandoran.backend.global.init;

import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.user.UserStatus;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seed("youth@test.com", "청년 테스터", Role.YOUTH, "010-1111-1111");
        seed("guardian@test.com", "보호자 테스터", Role.GUARDIAN, "010-2222-2222");
        seed("admin@test.com", "관리자 테스터", Role.ADMIN, "010-3333-3333");
    }

    private void seed(String email, String name, Role role, String phone) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .name(name)
                .role(role)
                .phoneNumber(phone)
                .status(UserStatus.ACTIVE)
                .build());
        log.info("[seed] Created {} account: {} / {}", role, email, DEFAULT_PASSWORD);
    }
}
