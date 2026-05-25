# 도란도란 Backend v2.0 최종 QA 체크리스트

> Step K-0 최종 QA 결과 기록. 본 문서는 main에 머지된 v2.0 백엔드의 인수인계용 검증 기록이다.

## 1. 브랜치 및 커밋 상태

| 항목 | 값 |
| --- | --- |
| 작업 브랜치 | `chore/v2-backend-final-qa-handoff` |
| 분기 기준 | `origin/main` (`a36806a` - Step J-2 머지 커밋) |
| 운영 코드(`src/main/**`) 변경 | 없음 |
| `src/test/**` 변경 | 없음 |
| `build.gradle` 변경 | 테스트 JVM 타임존을 `Asia/Seoul` 로 고정 (테스트 인프라) |
| `frontend` 변경 | 없음 |
| 부모 `/workspace`/`/workspace/docs` 변경 | 없음 |

> `LocalDateTime.now()` 를 사용하는 통합 테스트가 호스트 TZ(UTC 등)에서 KST 자정을 넘는 슬롯을 생성하면 서비스 측 `LocalDate.now(Asia/Seoul)` 와 어긋난다. 운영 코드/픽스처를 손대지 않기 위해 테스트 JVM TZ만 KST로 고정.

## 2. 빌드 / 테스트 결과

| 명령 | 결과 |
| --- | --- |
| `./gradlew compileJava` | BUILD SUCCESSFUL |
| `./gradlew compileTestJava` | BUILD SUCCESSFUL |
| `./gradlew test --rerun-tasks` | BUILD SUCCESSFUL |

| 테스트 통계 | 값 |
| --- | --- |
| 총 테스트 수 | **28** |
| 성공 | **28** |
| 실패 | **0** |
| 스킵 | **0** |

테스트 스위트별 분포:

| 스위트 | 개수 |
| --- | --- |
| `BackendApplicationTests` (컨텍스트 로드) | 1 |
| `AuthIntegrationTest` | 4 |
| `YouthProfileIntegrationTest` | 3 |
| `MatchIntegrationTest` | 3 |
| `MatchLimitIntegrationTest` | 1 |
| `ScheduleIntegrationTest` | 3 |
| `CallIntegrationTest` | 2 |
| `DeviceMainIntegrationTest` | 1 |
| `ActivityRecordIntegrationTest` | 1 |
| `CertificateIntegrationTest` | 2 |
| `CertificateIssueIntegrationTest` | 2 |
| `ReportIntegrationTest` | 2 |
| `MatchTerminationIntegrationTest` | 2 |
| `HelpRequestIntegrationTest` | 1 |

## 3. v2.0 폐기 로직 잔재 검색 결과

검색 범위: `src/main`, `src/test`, `build.gradle`, `application.yaml`.

| 키워드 | 결과 | 비고 |
| --- | --- | --- |
| `PhoneVerification` | **0건** | v2.0에서 보호자 연락처 인증 제거 완료 |
| `phone-verifications` | **0건** | 엔드포인트 없음 |
| `PHONE_VERIFICATION` | **0건** | ErrorCode 없음 |
| `subscription` / `Subscription` / `SUBSCRIPTION` | **0건** | 구독 도메인 없음 |
| `payment` / `Payment` / `PAYMENT` | **0건** | 결제 도메인 없음 |
| `settlement` / `Settlement` / `SETTLEMENT` | **0건** | 정산 도메인 없음 |
| `billing` / `Billing` / `BILLING` | **0건** | 빌링 도메인 없음 |

→ v2.0에서 폐기된 도메인의 코드/주석/문자열 잔재 **없음**.

## 4. 인증 / 인가 점검

### 4.1 SecurityConfig 정책 ([SecurityConfig.java](../src/main/java/com/dorandoran/backend/global/config/SecurityConfig.java))

- 세션: STATELESS
- CSRF, formLogin, httpBasic: 비활성화
- CORS: `http://localhost:*`, `http://127.0.0.1:*` 만 허용 + credentials 허용
- JWT 필터: `JwtAuthenticationFilter` 가 `UsernamePasswordAuthenticationFilter` 앞단에 등록
- `@EnableMethodSecurity` 활성화 → 컨트롤러 `@PreAuthorize` 로 역할 가드

### 4.2 Public endpoints (permitAll)

| 경로 | 비고 |
| --- | --- |
| `/api/v1/auth/**` | 로그인 |
| `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` | Swagger |
| `/actuator/health` | 헬스 체크 |

### 4.3 Device-token endpoints (Security 차원 permitAll, 서비스 레이어에서 Device 인증)

| 경로 | 인증 방식 | 검증 위치 |
| --- | --- | --- |
| `GET /api/v1/device/main` | `Authorization: Device {token}` | `DeviceAuthService.authenticate()` |
| `GET /api/v1/device/elders/{elderId}/today-schedule` | `Authorization: Device {token}` | `DeviceAuthService.authenticate()` |
| `POST /api/v1/calls/video` | `Authorization: Device {token}` | `DeviceAuthService.authenticate()` |
| `POST /api/v1/calls/audio` | `Authorization: Device {token}` | `DeviceAuthService.authenticate()` |
| `PATCH /api/v1/calls/{callLogId}/end` | `Authorization: Device {token}` **또는** `Authorization: Bearer {YOUTH JWT}` | `CallController.endCall()` 에서 헤더 prefix 로 분기 |
| `POST /api/v1/help-requests` | `Authorization: Device {token}` | `DeviceAuthService.authenticate()` |

> Security 필터에서는 `permitAll` 이지만, `DeviceAuthService` 가 토큰 유효성/등록 상태/연결된 어르신을 검증한다. (`DEVICE_AUTH_REQUIRED`, `INVALID_DEVICE_AUTHORIZATION`, `DEVICE_NOT_FOUND`, `DEVICE_NOT_REGISTERED`, `DEVICE_ACCESS_DENIED`)

### 4.4 Admin endpoints 보호

`@PreAuthorize("hasRole('ADMIN')")` 가 컨트롤러 클래스 단위로 강제됨:

- `AdminYouthController` → `/api/v1/admin/youths/**`
- `AdminUserController` → `/api/v1/admin/users/**`
- `AdminReportController` → `/api/v1/admin/reports/**`
- `AdminMatchTerminationRequestController` → `/api/v1/admin/match-termination-requests/**`
- `AdminHelpRequestController` → `/api/v1/admin/help-requests/**`

→ `permitAll` 또는 익명 접근 가능한 admin 엔드포인트 **없음**.

### 4.5 Role-별 가드 요약

| Role | 주요 가드된 도메인 |
| --- | --- |
| `YOUTH` | 청년 프로필 등록/조회/수정, 활동 상태 변경, 매칭 생성/조회, 가능 시간 등록, 일정 생성/취소, 활동 기록 작성, 증명서 발급, 누적 통계 조회 |
| `GUARDIAN` | 어르신 등록/조회/수정, 어르신 가능 시간 등록, 기기 조회, 매칭 목록 조회, 중단 요청 |
| `ADMIN` | 청년 승인/반려, 사용자 제재, 신고/중단/도움 요청 처리 |
| `DEVICE` (token) | 전용 기기 메인/오늘 일정, 통화 시작, 통화 종료, 도움 요청 |

## 5. 도메인별 구현 상태

### 5.1 Auth / User

- `POST /api/v1/auth/login` - JWT(access/refresh) 발급, 응답에 `AuthUserResponse` 포함
- `GET /api/v1/users/me` - 현재 인증 사용자 기본 정보
- 로그인 차단: `PENDING`(U005), `REJECTED`(U006), `SUSPENDED`(U004)

### 5.2 Youth

- `POST /api/v1/youth/profile`, `GET/PATCH /me`
- `PATCH /api/v1/youth/status` - 활동 상태 변경 (APPROVED 청년만)
- `GET /api/v1/admin/youths`, `/{id}`, `PATCH /{id}/approval`
- `PATCH /api/v1/admin/users/{id}/ban`

### 5.3 Elder / Device

- `POST/GET/PATCH /api/v1/elders/**` - 보호자/기관 전용
- `GET /api/v1/devices/{deviceId}` , `GET /api/v1/elders/{elderId}/device` - 보호자/관리자
- `GET /api/v1/device/main`, `GET /api/v1/device/elders/{elderId}/today-schedule` - Device 토큰
- ⚠️ `Device` 엔티티/등록/배송 흐름은 seed 기준 `DeviceStatus.REGISTERED`. 신규 기기 등록/배송 상태 변경 API는 v2.0 범위에서 별도 노출 없음 (seed 또는 운영자 수동 등록 가정).

### 5.4 Match

- `POST /api/v1/matches` - APPROVED 청년 + 사전 인사말 필수 + 즉시 MATCHED
- `GET /api/v1/matches/my`, `/{matchId}`, `/limit/me`
- `GET /api/v1/matching/elders`, `/{elderId}` - 청년용 (민감정보 마스킹)
- 담당 인원 제한: 기본 `5` (`YouthMatchLimit.DEFAULT_MAX_MATCH_COUNT = 5`)

### 5.5 AvailableTime / Schedule

- `POST /api/v1/available-times/youth` - 청년 본인
- `POST /api/v1/elders/{elderId}/available-times` - 보호자
- `GET /api/v1/available-times?ownerType=&ownerId=` - 조회
- `POST /api/v1/schedules` - 양측 가능 시간이 모두 포함되어야 생성 가능
- `GET /api/v1/schedules/my`, `PATCH /{scheduleId}/cancel`
- 매칭 어르신 목록 `availableFrom`/`availableTo` 필터 지원

### 5.6 Call

- `POST /api/v1/calls/video|audio` - Device 토큰
- `PATCH /api/v1/calls/{callLogId}/end` - Device 토큰 또는 YOUTH JWT

### 5.7 Activity / Certificate / Stats

- `POST /api/v1/activity-records`, `GET /api/v1/activity-records`
- `POST /api/v1/youth/certificates` - 누적 활동 시간이 요청 시간 이상이고 미발급 분만 발급 가능. 시리얼 `DRDR-YYYY-0001` 포맷.
- `GET /api/v1/youth/certificates/me`
- `GET /api/v1/youth/volunteer-stats/me`

### 5.8 Report / Termination / HelpRequest

- `POST /api/v1/reports` - YOUTH/GUARDIAN
- `GET/PATCH /api/v1/admin/reports/**` - ADMIN
- `POST /api/v1/matches/{matchId}/termination-requests` - YOUTH/GUARDIAN
- `GET/PATCH /api/v1/admin/match-termination-requests/**` - ADMIN
- `POST /api/v1/help-requests` - Device 토큰
- `GET/PATCH /api/v1/admin/help-requests/**` - ADMIN
- 재처리 차단: 신고(PENDING→처리), 중단(REQUESTED→처리), 도움요청(PENDING→처리) 후 동일 상태 변경 금지

## 6. 테스트 커버리지 요약

핵심 MVP 시나리오는 통합 테스트로 검증됨. (TC 번호는 `/workspace/docs/09_테스트_케이스.md` 참조)

| TC | 시나리오 | 결과 |
| --- | --- | --- |
| TC-001 / TC-002 / TC-003 / TC-005 | 승인/대기/반려/제재 청년 로그인 분기 | ✅ |
| TC-007 / TC-010 / TC-011 | 청년 프로필 제출 → 관리자 승인/반려 | ✅ |
| TC-020 / TC-021 / TC-022 | 사전 인사말 매칭 + 담당 인원 제한 | ✅ |
| TC-024 / TC-025 / TC-026 | 가능 시간 등록 + 일정 생성 + 중복 차단 | ✅ |
| TC-028 | Device token 메인 조회 | ✅ |
| TC-030 / TC-031 | 화상/음성 통화 시작 | ✅ |
| TC-033 | 도움 요청 생성 | ✅ |
| TC-034 | 활동 기록 + 누적 시간 즉시 반영 | ✅ |
| TC-036 / TC-037 / TC-038 | 증명서 발급/누적 부족/이미 발급분 재차감 | ✅ |
| TC-040 / TC-042 | 신고 접수 + 관리자 처리 | ✅ |
| TC-041 / TC-043 | 중단 요청 + 관리자 승인 → 매칭 ENDED | ✅ |

직접 테스트로 커버되지 않은 영역(프론트 연동 시 수동 확인 권장):

- 가능 시간 충돌/잘못된 범위 케이스 (`AT002`, `AT003`)
- 어르신 단건/목록 응답 마스킹 (`MatchingElderListResponse`, `MatchingElderDetailResponse`)
- 일정 취소 시 매칭 상태 변경
- 활동 기록의 `actualStartAt`/`actualEndAt` 수동 입력 흐름
- 신고/중단/도움요청 재처리 차단 (구현은 되어 있으나 통합 테스트 미작성)

## 7. 프론트 연동 전 확인 사항

1. **Base URL** 은 `http://localhost:8080` 가정. 운영 도메인은 별도 환경변수.
2. JWT 는 `Authorization: Bearer {accessToken}` 으로 전송. `Authorization` 응답 헤더는 노출 허용됨.
3. CORS 허용 출처는 `http://localhost:*`, `http://127.0.0.1:*`. 그 외 출처(예: 프론트 배포 도메인)는 운영 application.yaml 에서 추가 필요.
4. Device token 호출은 반드시 `Authorization: Device {token}` 헤더 사용. `Bearer` 와 혼동 금지.
5. `Authorization` 응답 헤더는 CORS exposedHeaders 에 등록됨. 토큰 재발급 패턴이 있다면 그대로 사용 가능.
6. 일정/가능 시간/활동 기록의 시간 값은 서버가 Asia/Seoul 로 해석함. 프론트는 ISO-8601 로컬 시각(`yyyy-MM-ddTHH:mm:ss`)으로 전송 권장.
7. `application.yaml` 의 `JWT_SECRET` 환경변수가 운영에서는 반드시 32바이트 이상이어야 한다.
8. `ddl-auto: update` 가 운영 yaml에 남아있음 (MVP 단계 정책). 운영 배포 전 마이그레이션 정책 확정 필요.

## 8. 후순위 / 미구현 기능

v2.0 범위 외, 후속 단계에서 다룰 항목:

| 항목 | 상태 | 비고 |
| --- | --- | --- |
| 회원가입 (이메일/소셜) | 미구현 | 현재는 `DataInitializer` 로 seed 만 존재 |
| 토큰 재발급(`/api/v1/auth/refresh`) | 미구현 | accessToken 만료 시 재로그인 필요 |
| 비밀번호 변경/찾기 | 미구현 | |
| Push / 알림 (FCM 등) | 미구현 | |
| 증명서 PDF 실제 생성 | 미구현 | 시리얼/메타데이터만 발급, 파일 생성 없음 |
| 외부 파일 스토리지 (S3 등) | 미구현 | `profileImageUrl`, `voiceSampleUrl` 은 URL 문자열 저장만 |
| OAuth / SNS 로그인 | 미구현 | |
| 통화 실제 미디어 연결 (WebRTC 등) | 미구현 | `CallLog` 만 기록 |
| 보호자 연락처 인증 (PhoneVerification) | **v2.0에서 폐기 — 구현/테스트 대상 제외** |
| 결제 / 구독 / 정산 / 빌링 | **v2.0에서 폐기 — 구현/테스트 대상 제외** |
| Notification 도메인 | 미구현 |

## 9. 결론

- 빌드/테스트/Security/폐기 코드 잔재 전 항목 통과.
- 운영 코드 변경 없음. 테스트 인프라(JVM TZ) 1줄만 수정.
- 프론트 연동을 위한 가이드 문서는 [FRONTEND_API_HANDOFF.md](./FRONTEND_API_HANDOFF.md) 참조.
- 실행/운영 절차는 [BACKEND_RUNBOOK.md](./BACKEND_RUNBOOK.md) 참조.
