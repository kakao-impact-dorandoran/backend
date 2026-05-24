package com.dorandoran.backend.domain.certificate;

import com.dorandoran.backend.domain.certificate.dto.CertificateIssueRequest;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.jwt.JwtTokenProvider;
import com.dorandoran.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CertificateIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("TC-037: 누적 활동 시간이 부족하면 증명서 발급은 409 + CT003 으로 차단된다")
    void issueCertificateWithoutEnoughHoursFails() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());
        CertificateIssueRequest body = new CertificateIssueRequest(10);

        mockMvc.perform(post("/api/v1/youth/certificates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CT003"));
    }

    @Test
    @DisplayName("발급 신청 시간이 최소 기준(10시간) 미만이면 409 + CT003 으로 차단된다")
    void issueCertificateRequestedHoursBelowMinimumFails() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());
        CertificateIssueRequest body = new CertificateIssueRequest(5);

        mockMvc.perform(post("/api/v1/youth/certificates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CT003"));
    }
}
