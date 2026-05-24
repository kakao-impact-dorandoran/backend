package com.dorandoran.backend.domain.report;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.report.dto.AdminReportProcessRequest;
import com.dorandoran.backend.domain.report.dto.ReportCreateRequest;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.jwt.JwtTokenProvider;
import com.dorandoran.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ReportRepository reportRepository;

    @Test
    @DisplayName("TC-040: 청년이 매칭 관련 신고를 접수하면 201과 PENDING 상태로 생성된다")
    void createReportSucceeds() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        User guardian = dataFactory.createGuardian();
        Elder elder = dataFactory.createElder(guardian);
        Match match = dataFactory.createMatchedMatch(youth, elder);

        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());
        ReportCreateRequest body = new ReportCreateRequest(
                match.getId(), null, null, elder.getId(),
                ReportType.INAPPROPRIATE_LANGUAGE,
                "부적절한 표현이 있었습니다.");

        mockMvc.perform(post("/api/v1/reports")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").exists())
                .andExpect(jsonPath("$.reporterUserId").value(youth.getId().toString()))
                .andExpect(jsonPath("$.matchId").value(match.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("TC-042: 관리자가 PENDING 신고를 RESOLVED로 처리하면 status/adminMemo/resolvedAt이 저장된다")
    void adminResolvesReportSucceeds() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        User guardian = dataFactory.createGuardian();
        Elder elder = dataFactory.createElder(guardian);
        Match match = dataFactory.createMatchedMatch(youth, elder);
        Report report = dataFactory.createPendingReport(youth, match);

        User admin = dataFactory.createAdmin();
        String adminToken = jwtTokenProvider.createAccessToken(admin.getId(), Role.ADMIN.name());
        String memo = "관리자 메모: 정상 처리.";
        AdminReportProcessRequest body = new AdminReportProcessRequest(ReportStatus.RESOLVED, memo);

        mockMvc.perform(patch("/api/v1/admin/reports/{reportId}", report.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(report.getId().toString()))
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.adminMemo").value(memo))
                .andExpect(jsonPath("$.resolvedAt").exists());

        Report reloaded = reportRepository.findById(report.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(reloaded.getAdminMemo()).isEqualTo(memo);
        assertThat(reloaded.getResolvedAt()).isNotNull();
        assertThat(reloaded.getAdmin().getId()).isEqualTo(admin.getId());
    }
}
