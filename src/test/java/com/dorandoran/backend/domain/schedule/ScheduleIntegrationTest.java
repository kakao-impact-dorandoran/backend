package com.dorandoran.backend.domain.schedule;

import com.dorandoran.backend.domain.availabletime.AvailableTime;
import com.dorandoran.backend.domain.availabletime.AvailableTimeOwnerType;
import com.dorandoran.backend.domain.availabletime.AvailableTimeRepository;
import com.dorandoran.backend.domain.availabletime.dto.AvailableTimeCreateRequest;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchRepository;
import com.dorandoran.backend.domain.schedule.dto.ScheduleCreateRequest;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.jwt.JwtTokenProvider;
import com.dorandoran.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScheduleIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AvailableTimeRepository availableTimeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    @DisplayName("TC-024: 청년 가능 시간 등록 요청은 201로 등록된다")
    void createYouthAvailableTimeSucceeds() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());
        LocalDateTime start = futureSlotStart(1);
        AvailableTimeCreateRequest body = new AvailableTimeCreateRequest(start, start.plusHours(1));

        mockMvc.perform(post("/api/v1/available-times/youth")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerType").value("YOUTH"));
    }

    @Test
    @DisplayName("TC-025: 양측 가능 시간이 등록된 매칭의 일정 생성은 201 + CONFIRMED 상태로 생성된다")
    void createScheduleSucceeds() throws Exception {
        ScheduleFixture fixture = setupScheduleFixture(2);
        ScheduleCreateRequest body = new ScheduleCreateRequest(
                fixture.match().getId(),
                fixture.slotStart(),
                fixture.slotEnd());

        mockMvc.perform(post("/api/v1/schedules")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fixture.youthToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduleId").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("TC-026: 동일 시간대 일정 중복 생성은 409 + SC001 로 차단된다")
    void createConflictingScheduleFails() throws Exception {
        ScheduleFixture fixture = setupScheduleFixture(3);
        ScheduleCreateRequest body = new ScheduleCreateRequest(
                fixture.match().getId(),
                fixture.slotStart(),
                fixture.slotEnd());

        mockMvc.perform(post("/api/v1/schedules")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fixture.youthToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/schedules")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fixture.youthToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SC001"));
    }

    private ScheduleFixture setupScheduleFixture(int daysAhead) {
        User youth = dataFactory.createApprovedYouth();
        User guardian = dataFactory.createActiveUser(Role.GUARDIAN);
        Elder elder = dataFactory.createElder(guardian);
        Match match = matchRepository.save(Match.builder()
                .youth(youth)
                .elder(elder)
                .icebreakingMessage("안녕하세요")
                .build());

        LocalDateTime slotStart = futureSlotStart(daysAhead);
        LocalDateTime slotEnd = slotStart.plusHours(2);

        availableTimeRepository.save(AvailableTime.builder()
                .ownerType(AvailableTimeOwnerType.YOUTH)
                .youth(youth)
                .registeredBy(youth)
                .startTime(slotStart)
                .endTime(slotEnd)
                .isBooked(false)
                .build());
        availableTimeRepository.save(AvailableTime.builder()
                .ownerType(AvailableTimeOwnerType.ELDER)
                .elder(elder)
                .registeredBy(guardian)
                .startTime(slotStart)
                .endTime(slotEnd)
                .isBooked(false)
                .build());

        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());
        return new ScheduleFixture(match, slotStart, slotStart.plusHours(1), token);
    }

    private LocalDateTime futureSlotStart(int daysAhead) {
        return LocalDateTime.now()
                .plusDays(daysAhead)
                .withHour(14).withMinute(0).withSecond(0).withNano(0);
    }

    private record ScheduleFixture(Match match,
                                   LocalDateTime slotStart,
                                   LocalDateTime slotEnd,
                                   String youthToken) {
    }
}
