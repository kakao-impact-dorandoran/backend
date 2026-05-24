package com.dorandoran.backend.domain.certificate;

import com.dorandoran.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    List<Certificate> findAllByYouthOrderByIssuedAtDesc(User youth);

    List<Certificate> findAllByYouth_IdOrderByIssuedAtDesc(UUID youthId);

    boolean existsByCertificateSerial(String certificateSerial);

    long countByIssuedAtBetween(LocalDateTime start, LocalDateTime end);
}
