package com.dorandoran.backend.domain.youth;

import com.dorandoran.backend.domain.admin.dto.AdminYouthApprovalRequest;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.youth.dto.YouthProfileCreateRequest;
import com.dorandoran.backend.global.jwt.JwtTokenProvider;
import com.dorandoran.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class YouthProfileIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private YouthProfileRepository youthProfileRepository;

    @Test
    @DisplayName("TC-007: 청년이 프로필을 제출하면 201과 PENDING 상태로 생성된다")
    void createYouthProfileSucceeds() throws Exception {
        User youth = dataFactory.createYouthUserWithoutProfile();
        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());

        YouthProfileCreateRequest body = new YouthProfileCreateRequest(
                null,
                List.of("산책", "독서"),
                "안녕하세요, 잘 부탁드립니다.",
                null
        );

        mockMvc.perform(post("/api/v1/youth/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").exists())
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"));

        YouthProfile saved = youthProfileRepository.findByYouth(youth).orElseThrow();
        assertThat(saved.getApprovalStatus()).isEqualTo(YouthApprovalStatus.PENDING);
        assertThat(saved.getGreetingComment()).isEqualTo("안녕하세요, 잘 부탁드립니다.");
    }

    @Test
    @DisplayName("TC-010: 관리자가 PENDING 청년을 승인하면 approvalStatus가 APPROVED로 변경된다")
    void adminApprovesYouthSucceeds() throws Exception {
        User admin = dataFactory.createAdmin();
        User youth = dataFactory.createPendingYouth();
        String adminToken = jwtTokenProvider.createAccessToken(admin.getId(), Role.ADMIN.name());

        AdminYouthApprovalRequest body = new AdminYouthApprovalRequest(
                YouthApprovalStatus.APPROVED, null);

        mockMvc.perform(patch("/api/v1/admin/youths/{youthId}/approval", youth.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.youthId").value(youth.getId().toString()))
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

        YouthProfile profile = youthProfileRepository.findByYouth(youth).orElseThrow();
        assertThat(profile.getApprovalStatus()).isEqualTo(YouthApprovalStatus.APPROVED);
        assertThat(profile.getRejectionReason()).isNull();
    }

    @Test
    @DisplayName("TC-011: 관리자가 PENDING 청년을 반려하면 REJECTED 상태와 rejectionReason이 저장된다")
    void adminRejectsYouthSucceeds() throws Exception {
        User admin = dataFactory.createAdmin();
        User youth = dataFactory.createPendingYouth();
        String adminToken = jwtTokenProvider.createAccessToken(admin.getId(), Role.ADMIN.name());

        String reason = "프로필 정보가 부족합니다.";
        AdminYouthApprovalRequest body = new AdminYouthApprovalRequest(
                YouthApprovalStatus.REJECTED, reason);

        mockMvc.perform(patch("/api/v1/admin/youths/{youthId}/approval", youth.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value(reason));

        YouthProfile profile = youthProfileRepository.findByYouth(youth).orElseThrow();
        assertThat(profile.getApprovalStatus()).isEqualTo(YouthApprovalStatus.REJECTED);
        assertThat(profile.getRejectionReason()).isEqualTo(reason);
    }
}
