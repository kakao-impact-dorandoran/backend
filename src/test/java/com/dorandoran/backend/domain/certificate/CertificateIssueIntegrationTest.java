package com.dorandoran.backend.domain.certificate;

import com.dorandoran.backend.domain.certificate.dto.CertificateIssueRequest;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.volunteer.YouthVolunteerStats;
import com.dorandoran.backend.domain.volunteer.YouthVolunteerStatsRepository;
import com.dorandoran.backend.global.jwt.JwtTokenProvider;
import com.dorandoran.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CertificateIssueIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private YouthVolunteerStatsRepository youthVolunteerStatsRepository;

    @Test
    @DisplayName("TC-036: 누적 10시간 이상이면 증명서 발급에 성공하고 시리얼/누적 발급시간이 갱신된다")
    void issueCertificateSucceeds() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        dataFactory.createYouthVolunteerStats(youth, 10 * 60, 0);

        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());
        CertificateIssueRequest body = new CertificateIssueRequest(10);

        mockMvc.perform(post("/api/v1/youth/certificates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.certificateId").exists())
                .andExpect(jsonPath("$.certificateSerial",
                        matchesPattern("DRDR-\\d{4}-\\d{4}")))
                .andExpect(jsonPath("$.certifiedHours").value(10))
                .andExpect(jsonPath("$.youthId").value(youth.getId().toString()));

        YouthVolunteerStats stats = youthVolunteerStatsRepository.findByYouth(youth).orElseThrow();
        assertThat(stats.getTotalCertifiedHours()).isEqualTo(10);
    }

    @Test
    @DisplayName("TC-038: 누적 15시간/이미 10시간 발급 상태에서 다시 10시간 발급을 요청하면 409 + CT003 으로 차단된다")
    void issueCertificateDuplicateBlocked() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        dataFactory.createYouthVolunteerStats(youth, 15 * 60, 10);

        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());
        CertificateIssueRequest body = new CertificateIssueRequest(10);

        mockMvc.perform(post("/api/v1/youth/certificates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CT003"));
    }
}
