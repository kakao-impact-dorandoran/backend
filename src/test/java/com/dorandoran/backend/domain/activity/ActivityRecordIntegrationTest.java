package com.dorandoran.backend.domain.activity;

import com.dorandoran.backend.domain.activity.dto.ActivityRecordCreateRequest;
import com.dorandoran.backend.domain.call.CallLog;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.schedule.Schedule;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActivityRecordIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private YouthVolunteerStatsRepository youthVolunteerStatsRepository;

    @Test
    @DisplayName("TC-034: 완료된 활동 기록을 작성하면 누적 활동 시간이 즉시 증가한다")
    void createActivityRecordAccumulatesDuration() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        User guardian = dataFactory.createGuardian();
        Elder elder = dataFactory.createElder(guardian);
        Match match = dataFactory.createMatchedMatch(youth, elder);

        LocalDateTime callStart = LocalDateTime.now().minusHours(2).withSecond(0).withNano(0);
        LocalDateTime callEnd = callStart.plusMinutes(45);
        Schedule schedule = dataFactory.createConfirmedSchedule(match, callStart, callEnd);
        CallLog callLog = dataFactory.createCompletedCallLog(match, schedule, callStart, callEnd);

        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());
        ActivityRecordCreateRequest body = new ActivityRecordCreateRequest(
                match.getId(), schedule.getId(), callLog.getId(),
                true, null, null, null, "활동 기록 노트");

        mockMvc.perform(post("/api/v1/activity-records")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityRecordId").exists())
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.totalDurationMinutes").value(45));

        YouthVolunteerStats stats = youthVolunteerStatsRepository.findByYouth(youth).orElseThrow();
        assertThat(stats.getTotalDurationMinutes()).isEqualTo(45);
    }
}
