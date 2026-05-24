package com.dorandoran.backend.domain.certificate.service;

import com.dorandoran.backend.domain.certificate.Certificate;
import com.dorandoran.backend.domain.certificate.CertificateRepository;
import com.dorandoran.backend.domain.certificate.dto.CertificateIssueRequest;
import com.dorandoran.backend.domain.certificate.dto.CertificateResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateService {

    public static final int MINIMUM_ISSUE_HOURS = 10;
    public static final String CERTIFICATE_TITLE = "도란도란 사회참여 증명서";
    private static final String SERIAL_PREFIX = "DRDR";
    private static final int MAX_SERIAL_RETRIES = 5;

    private final UserRepository userRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final CertificateRepository certificateRepository;
    private final YouthVolunteerStatsService statsService;

    @Transactional
    public CertificateResponse issueCertificate(UUID youthUserId, CertificateIssueRequest request) {
        User youth = loadApprovedYouth(youthUserId);
        YouthVolunteerStats stats = statsService.getOrCreateForYouth(youth);

        int requestedHours = request.requestedHours();
        if (requestedHours < MINIMUM_ISSUE_HOURS) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_ENOUGH_ACTIVITY_TIME);
        }

        int availableHours = stats.availableCertificateHours();
        if (availableHours < MINIMUM_ISSUE_HOURS) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_ENOUGH_ACTIVITY_TIME);
        }
        if (requestedHours > availableHours) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_ENOUGH_ACTIVITY_TIME);
        }

        String serial = generateSerialNumber(LocalDateTime.now().getYear());

        Certificate certificate = certificateRepository.save(Certificate.builder()
                .youth(youth)
                .certificateSerial(serial)
                .title(CERTIFICATE_TITLE)
                .certifiedHours(requestedHours)
                .pdfUrl(null)
                .build());

        stats.addCertifiedHours(requestedHours);

        return CertificateResponse.from(certificate);
    }

    public List<CertificateResponse> findMyCertificates(UUID youthUserId) {
        User youth = loadApprovedYouth(youthUserId);
        return certificateRepository.findAllByYouthOrderByIssuedAtDesc(youth)
                .stream()
                .map(CertificateResponse::from)
                .toList();
    }

    private String generateSerialNumber(int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year + 1, 1, 1, 0, 0);
        long issuedThisYear = certificateRepository.countByIssuedAtBetween(start, end);

        for (int attempt = 0; attempt < MAX_SERIAL_RETRIES; attempt++) {
            long next = issuedThisYear + 1 + attempt;
            String candidate = String.format(Locale.ROOT, "%s-%d-%04d", SERIAL_PREFIX, year, next);
            if (!certificateRepository.existsByCertificateSerial(candidate)) {
                return candidate;
            }
        }
        throw new BusinessException(ErrorCode.CERTIFICATE_SERIAL_DUPLICATED);
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
