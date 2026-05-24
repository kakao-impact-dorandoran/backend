package com.dorandoran.backend.domain.auth.service;

import com.dorandoran.backend.domain.auth.Token;
import com.dorandoran.backend.domain.auth.TokenRepository;
import com.dorandoran.backend.domain.auth.dto.LoginRequest;
import com.dorandoran.backend.domain.auth.dto.LoginResponse;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.domain.user.dto.AuthUserResponse;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;
import com.dorandoran.backend.domain.youth.YouthProfileRepository;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import com.dorandoran.backend.global.jwt.JwtProperties;
import com.dorandoran.backend.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Inactive account");
        }

        YouthProfile youthProfile = null;
        if (user.getRole() == Role.YOUTH) {
            Optional<YouthProfile> profileOpt = youthProfileRepository.findByYouth(user);
            if (profileOpt.isEmpty()) {
                throw new BusinessException(ErrorCode.YOUTH_PENDING_APPROVAL);
            }
            youthProfile = profileOpt.get();
            YouthApprovalStatus approvalStatus = youthProfile.getApprovalStatus();
            if (approvalStatus == YouthApprovalStatus.PENDING) {
                throw new BusinessException(ErrorCode.YOUTH_PENDING_APPROVAL);
            }
            if (approvalStatus == YouthApprovalStatus.REJECTED) {
                String reason = youthProfile.getRejectionReason();
                String detail = (reason == null || reason.isBlank())
                        ? ErrorCode.YOUTH_REJECTED.getMessage()
                        : ErrorCode.YOUTH_REJECTED.getMessage() + " 사유: " + reason;
                throw new BusinessException(ErrorCode.YOUTH_REJECTED, detail);
            }
        }

        String role = user.getRole().name();
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), role);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), role);

        LocalDateTime refreshExpiry = LocalDateTime.now()
                .plusNanos(jwtProperties.getRefreshTokenValidityMs() * 1_000_000L);

        tokenRepository.findByUserAndRevokedFalse(user)
                .ifPresentOrElse(
                        existing -> existing.rotate(refreshToken, refreshExpiry),
                        () -> tokenRepository.save(Token.builder()
                                .user(user)
                                .refreshToken(refreshToken)
                                .expiresAt(refreshExpiry)
                                .build())
                );

        return new LoginResponse(accessToken, refreshToken, AuthUserResponse.from(user, youthProfile));
    }
}
