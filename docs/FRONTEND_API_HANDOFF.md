# 도란도란 Backend v2.0 프론트 연동 가이드

> 이 문서는 프론트엔드 팀이 v2.0 백엔드를 바로 연동할 수 있도록 정리한 핸드오프 문서입니다. 본문에 적힌 모든 API 경로/필드명은 현재 main 의 컨트롤러/DTO 코드를 기준으로 작성되었습니다.

---

## 1. 현재 백엔드 상태

- 버전: v2.0 MVP
- 기준 커밋: `origin/main` Step J-2 (`a36806a`)
- 빌드: Java 21 + Spring Boot 3.5.x
- DB: MySQL 8 / 운영, H2 (in-memory) / 테스트
- 인증: 이메일+비밀번호 JWT + 전용 기기용 Device 토큰
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- 테스트: 28건 전수 통과 (Step J-2 머지 시점)
- v2.0 폐기 도메인: 결제/구독/정산/빌링/PhoneVerification → 코드/잔재 **0건**

---

## 2. 서버 실행 방법

자세한 절차는 [BACKEND_RUNBOOK.md](./BACKEND_RUNBOOK.md) 참고. 요약:

```bash
# 1) MySQL 기동 (선택, docker 사용 시)
docker compose up -d

# 2) 환경변수 (필요 시)
export DB_HOST=localhost DB_PORT=3306 DB_NAME=dorandoran
export DB_USERNAME=dorandoran DB_PASSWORD=dorandoran1234!
export JWT_SECRET=<32바이트 이상의 시크릿>

# 3) 부트런
./gradlew bootRun
# → http://localhost:8080
```

서버 기동 시 `DataInitializer` (Profile=`!test`) 가 테스트 계정/어르신/기기/가능시간을 자동 시드합니다.

---

## 3. 테스트 실행 방법

```bash
./gradlew test
```

- H2 인메모리, `spring.profiles.active=test`
- 테스트 JVM 타임존: Asia/Seoul (build.gradle 에 고정)
- 통합 테스트만 28건. 자세한 TC 매핑은 [BACKEND_QA_CHECKLIST.md](./BACKEND_QA_CHECKLIST.md#6-테스트-커버리지-요약) 참고.

---

## 4. 공통 API 규칙

### 4.1 Base URL

- 로컬: `http://localhost:8080`
- 모든 도메인 API 는 `/api/v1` prefix 사용

### 4.2 공통 응답 형식

성공 응답은 일반적으로 도메인별 DTO 의 JSON 을 그대로 반환합니다. 별도의 `data`/`meta` 봉투(envelope) 는 **사용하지 않습니다**.

- 생성 성공: `201 Created` + body
- 조회/수정 성공: `200 OK` + body
- 빈 body 조회: `200 OK` + `null` 가능 (예: 오늘 일정 없음)

### 4.3 공통 에러 형식

`GlobalExceptionHandler` 가 다음 포맷으로 응답합니다.

```json
{
  "timestamp": "2026-05-24T15:30:00",
  "status": 403,
  "code": "U005",
  "message": "관리자 승인 대기 중입니다.",
  "errors": null
}
```

- `code` 는 `ErrorCode` enum 의 코드 (예: `C001`, `U004`, `M001`, ...). 프론트 분기 기준은 **code 값**으로 잡는 것을 권장.
- 입력 검증 실패(`400`) 시 `errors` 배열에 `{ field, value, reason }` 이 포함됩니다.

대표 ErrorCode (전체 목록은 [`ErrorCode.java`](../src/main/java/com/dorandoran/backend/global/error/ErrorCode.java) 참고):

| code | HTTP | 의미 |
| --- | --- | --- |
| `C001` | 400 | Invalid input value (validation 실패) |
| `C006` | 403 | Access denied (역할/리소스 권한 없음) |
| `C007` | 401 | Unauthorized (인증 자체 실패) |
| `U003` | 401 | 비밀번호 불일치 |
| `U004` | 403 | 운영 정책 위반으로 이용 제한 (SUSPENDED) |
| `U005` | 403 | 관리자 승인 대기 (YOUTH PENDING) |
| `U006` | 403 | 가입 신청 반려 (YOUTH REJECTED) |
| `A001`/`A002` | 401 | 잘못된/만료 JWT |
| `M001` | 409 | 담당 인원 초과 |
| `M005` | 400 | 사전 인사말 누락 |
| `SC001` | 409 | 일정 충돌 |
| `SC008` | 409 | 가능 시간 외 일정 |
| `D005` | 401 | Device Authorization 헤더 없음 |
| `D006` | 401 | Device Authorization 형식 오류 |
| `D007` | 403 | 기기가 REGISTERED 상태 아님 |
| `CT003` | 409 | 증명서 발급 기준 시간 부족 |

### 4.4 인증 방식

#### 4.4.1 Bearer JWT

대부분의 사용자 API 는 다음 헤더를 요구합니다.

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

- accessToken 유효기간: **1시간**
- refreshToken 유효기간: **14일** (현재 재발급 endpoint 미구현 — 만료 시 재로그인)
- 토큰 payload 의 subject = User UUID, claim `role` = `YOUTH | GUARDIAN | ADMIN`
- 응답 헤더 `Authorization` 은 CORS 의 `exposedHeaders` 로 열려있어, 추후 서버가 재발급 시 헤더로 내려보낼 수 있습니다 (현재는 응답 body 로만 발급).

#### 4.4.2 Device Token

전용 기기 (어르신 태블릿) 호출은 다음 헤더를 사용합니다.

```
Authorization: Device seed-device-token-0001
```

- 서비스 레이어(`DeviceAuthService`)에서 검증
- 기기 상태가 `REGISTERED` 가 아니면 `403 D007`
- 토큰이 등록되지 않은 경우 `404 D002`
- 헤더 자체가 없거나 prefix 가 다르면 `401 D005 / D006`

`PATCH /api/v1/calls/{callLogId}/end` 만 `Bearer` 와 `Device` 두 가지 인증을 모두 지원합니다 (청년/기기 어느 쪽이 끊든 종료 처리 가능).

---

## 5. 테스트 계정 및 Device Token

`DataInitializer` ([`DataInitializer.java`](../src/main/java/com/dorandoran/backend/global/init/DataInitializer.java)) 가 부트런 시 자동 생성하는 seed 데이터입니다. 비밀번호는 모두 `test1234!`.

### 5.1 사용자 계정

| 역할 | 이메일 | 비밀번호 | UserStatus | YouthApprovalStatus | 용도 |
| --- | --- | --- | --- | --- | --- |
| ADMIN | `admin@test.com` | `test1234!` | ACTIVE | - | 관리자 API 테스트 |
| YOUTH | `youth@test.com` | `test1234!` | ACTIVE | (프로필 없음) | 청년 프로필 미등록 상태 |
| GUARDIAN | `guardian@test.com` | `test1234!` | ACTIVE | - | 어르신/기기/가능시간 등록 |
| YOUTH | `youth_approved@test.com` | `test1234!` | ACTIVE | APPROVED | 청년 정상 로그인 / 매칭 가능 |
| YOUTH | `youth_pending@test.com` | `test1234!` | ACTIVE | PENDING | 로그인 차단(U005) 테스트 |
| YOUTH | `youth_rejected@test.com` | `test1234!` | ACTIVE | REJECTED | 로그인 차단(U006) 테스트 |
| YOUTH | `youth_banned@test.com` | `test1234!` | SUSPENDED | APPROVED | 로그인 차단(U004) 테스트 |

> 비밀번호 상수는 `DataInitializer.DEFAULT_PASSWORD` 에 정의되어 있습니다.

### 5.2 어르신 / 기기

| 항목 | 값 |
| --- | --- |
| 어르신 이름 | `박도란` |
| 보호자 | `guardian@test.com` |
| 기기 시리얼 | `SEED-TABLET-0001` |
| **Device Token** | `seed-device-token-0001` |
| DeviceStatus | `REGISTERED` |
| DeliveryStatus | `DELIVERED` |

### 5.3 시드된 가능 시간

`youth_approved@test.com` 과 어르신 `박도란` 모두 **D+1 일 14:00 ~ 15:00 KST** 의 가능 시간이 시드됩니다. 일정 생성 테스트 시 이 시간대 사용.

---

## 6. 역할별 주요 플로우

### 6.1 청년 (YOUTH) 플로우

1. `POST /api/v1/auth/login` 으로 로그인 → accessToken 획득
2. (최초) `POST /api/v1/youth/profile` 로 프로필 제출 → `approvalStatus=PENDING`
3. 관리자가 승인할 때까지 매칭/상세 API 호출 시 권한 에러
4. 승인 후 `GET /api/v1/users/me` 응답의 `approvalStatus=APPROVED` 확인
5. `GET /api/v1/matching/elders` 로 어르신 탐색 → `POST /api/v1/matches` 로 매칭 (사전 인사말 필수)
6. `POST /api/v1/available-times/youth` 로 가능 시간 등록 (보호자측이 어르신 가능 시간 등록한 뒤)
7. `POST /api/v1/schedules` 로 양측 가능 시간 안에서 일정 생성
8. (대화 종료 후) `POST /api/v1/activity-records` 작성 → 누적 시간 갱신
9. `GET /api/v1/youth/volunteer-stats/me` 로 누적 통계 확인
10. 10시간 누적 후 `POST /api/v1/youth/certificates` 로 증명서 발급

### 6.2 보호자 / 어르신 등록자 (GUARDIAN) 플로우

1. 로그인
2. `POST /api/v1/elders` 로 어르신 등록 (기본 상태 AVAILABLE)
3. `POST /api/v1/elders/{elderId}/available-times` 로 어르신 가능 시간 등록
4. `GET /api/v1/elders/{elderId}/device` 로 기기 정보 확인 (기기는 운영자/seed 로 등록 가정)
5. `GET /api/v1/matches/my` 로 어르신 관련 매칭 모니터링
6. 필요 시 `POST /api/v1/reports`, `POST /api/v1/matches/{matchId}/termination-requests`

### 6.3 관리자 (ADMIN) 플로우

1. 로그인
2. `GET /api/v1/admin/youths?approvalStatus=PENDING` 로 승인 대기 목록
3. `PATCH /api/v1/admin/youths/{youthId}/approval` 로 승인/반려
4. `PATCH /api/v1/admin/users/{userId}/ban` 으로 제재
5. `GET /api/v1/admin/reports`, `/match-termination-requests`, `/help-requests` 로 운영 큐 모니터링
6. 각각 `PATCH /{id}` 로 처리

### 6.4 전용 기기 (DEVICE) 플로우

1. 기기는 등록 시점에 `deviceToken` 을 미리 보유 (seed 또는 운영자 등록)
2. `GET /api/v1/device/main` 로 어르신 메인 화면 데이터 조회
3. 화면에 표시되는 버튼 종류는 `buttons` 필드: `VIDEO_CALL`, `AUDIO_CALL`, `HELP_REQUEST`
4. 버튼 클릭 시
   - 화상 통화: `POST /api/v1/calls/video`
   - 음성 통화: `POST /api/v1/calls/audio`
   - 도움 요청: `POST /api/v1/help-requests`
5. 통화 종료 시 `PATCH /api/v1/calls/{callLogId}/end` (Device 또는 YOUTH JWT)

---

## 7. API 목록

> 모든 경로는 `/api/v1` prefix. JSON 본문/응답 필드는 코드 DTO 와 일치.

### 7.1 Auth

#### 로그인

- **Method**: `POST`
- **Path**: `/api/v1/auth/login`
- **Auth**: Public
- **Request**:
  ```json
  {
    "email": "youth_approved@test.com",
    "password": "test1234!"
  }
  ```
- **Response (200)**:
  ```json
  {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "user": {
      "id": "uuid",
      "email": "youth_approved@test.com",
      "name": "청년 승인완료",
      "role": "YOUTH",
      "profileUrl": null,
      "status": "ACTIVE",
      "partnerCode": null,
      "approvalStatus": "APPROVED",
      "rejectionReason": null,
      "activityStatus": "AVAILABLE"
    }
  }
  ```
- **에러**: `U003`(비밀번호 불일치), `U004`(SUSPENDED), `U005`(PENDING), `U006`(REJECTED)

### 7.2 User / Me

#### 내 정보 조회

- **Method**: `GET`
- **Path**: `/api/v1/users/me`
- **Auth**: Bearer JWT (모든 역할)
- **Response**: `AuthUserResponse` (로그인 응답의 `user` 와 동일 스키마)

### 7.3 Youth Profile

#### 프로필 등록

- **Method**: `POST`
- **Path**: `/api/v1/youth/profile`
- **Auth**: Bearer JWT (YOUTH)
- **Request**:
  ```json
  {
    "profileImageUrl": "https://...",
    "keywords": ["등산", "고전", "사진"],
    "greetingComment": "산책과 책 좋아하는 청년입니다.",
    "voiceSampleUrl": "https://..."
  }
  ```
- **Response (201)**: `YouthProfileCreateResponse`
- **에러**: `Y004`(이미 존재), `Y001`(키워드 5개 초과), `Y002`(금칙어)

#### 내 프로필 조회

- `GET /api/v1/youth/profile/me` → `YouthProfileResponse`

#### 내 프로필 수정

- `PATCH /api/v1/youth/profile/me`
- Request: `YouthProfileUpdateRequest` (profileImageUrl/keywords/greetingComment/voiceSampleUrl 일부 수정)
- 응답의 `approvalStatus`/`activityStatus`/`rejectionReason` 은 변경되지 않음

#### 활동 상태 변경

- `PATCH /api/v1/youth/status`
- Request:
  ```json
  { "activityStatus": "AVAILABLE" }
  ```
  값: `AVAILABLE | BUSY | UNAVAILABLE`
- Response: `YouthActivityStatusResponse`

### 7.4 Admin Youth / User

#### 청년 목록 조회

- `GET /api/v1/admin/youths?approvalStatus=PENDING`
- Auth: Bearer JWT (ADMIN)
- Response: `AdminYouthListResponse[]`

#### 청년 상세 조회

- `GET /api/v1/admin/youths/{youthId}` → `AdminYouthDetailResponse`

#### 청년 승인/반려

- `PATCH /api/v1/admin/youths/{youthId}/approval`
- Request:
  ```json
  {
    "approvalStatus": "REJECTED",
    "rejectionReason": "프로필 정보 부족"
  }
  ```
  - `APPROVED` 처리 시 `rejectionReason` 생략 가능
  - `REJECTED` 처리 시 `rejectionReason` 필수 (`Y007`)
  - `PENDING` 으로 되돌릴 수 없음

#### 사용자 제재

- `PATCH /api/v1/admin/users/{userId}/ban`
- Request:
  ```json
  { "reason": "운영 정책 위반" }
  ```
- 차단: 관리자 본인(`U008`), 다른 관리자(`U007`), 이미 제재된 사용자(`U009`)

### 7.5 Elder

#### 어르신 등록

- `POST /api/v1/elders`
- Auth: Bearer JWT (GUARDIAN)
- Request:
  ```json
  {
    "name": "박도란",
    "ageGroup": "70대",
    "gender": "FEMALE",
    "profileImageUrl": null,
    "greetingComment": "꽃과 산책 이야기를 좋아합니다.",
    "phoneNumber": "010-1111-1111",
    "address": "서울시 종로구 ...",
    "interests": ["산책", "드라마"],
    "preferredCallType": "VIDEO",
    "difficultyLevel": "LOW",
    "requestNotes": "천천히 말해주세요."
  }
  ```
- Response (201): `ElderResponse`

#### 내 어르신 목록

- `GET /api/v1/elders/my` → `ElderResponse[]`

#### 어르신 정보 수정

- `PATCH /api/v1/elders/{elderId}` (status, deviceId 변경 불가)

### 7.6 Device

#### 기기 단건 조회

- `GET /api/v1/devices/{deviceId}` (GUARDIAN/ADMIN)
- Response: `DeviceResponse` — `deviceToken` 은 노출하지 않음

#### 어르신 기준 기기 조회

- `GET /api/v1/elders/{elderId}/device` (GUARDIAN/ADMIN)

#### 전용 기기 메인

- `GET /api/v1/device/main`
- Auth: `Authorization: Device {deviceToken}`
- Response:
  ```json
  {
    "elderId": "uuid",
    "elderName": "박도란",
    "todaySchedule": {
      "scheduleId": "uuid",
      "matchId": "uuid",
      "scheduledStartAt": "2026-05-25T14:00:00",
      "scheduledEndAt": "2026-05-25T15:00:00",
      "callType": "VIDEO",
      "youthName": "청년 승인완료"
    },
    "buttons": ["VIDEO_CALL", "AUDIO_CALL", "HELP_REQUEST"],
    "deviceStatus": "REGISTERED"
  }
  ```
  - 오늘 확정 일정이 없으면 `todaySchedule: null`

#### 오늘 일정 조회

- `GET /api/v1/device/elders/{elderId}/today-schedule`
- Auth: Device Token (해당 어르신과 매칭된 기기여야 함, `DEVICE_ACCESS_DENIED`)

### 7.7 Matching / Match

#### 청년용 어르신 목록

- `GET /api/v1/matching/elders`
- Query: `interest`, `preferredCallType`, `difficultyLevel`, `availableFrom`, `availableTo` (모두 optional)
  - `availableFrom`/`availableTo` 가 **둘 다** 지정되면 해당 시간 범위와 겹치는 가능 시간을 가진 어르신만 반환
- Auth: Bearer JWT (YOUTH, APPROVED)
- Response: `MatchingElderListResponse[]` (주소/연락처 등 민감정보 미포함)

#### 청년용 어르신 상세

- `GET /api/v1/matching/elders/{elderId}`
- 어르신 status 가 `AVAILABLE` 일 때만 조회 허용

#### 매칭 생성

- `POST /api/v1/matches`
- Auth: Bearer JWT (YOUTH, APPROVED)
- Request:
  ```json
  {
    "elderId": "uuid",
    "icebreakingMessage": "안녕하세요! 산책 좋아하신다는 이야기 인상깊었어요."
  }
  ```
- Response (201): `MatchResponse` (status=`MATCHED`)
- 에러: `M001`(담당 인원 초과), `M004`(이미 매칭됨), `M005`(인사말 누락), `M006`(어르신 비활성)

#### 내 매칭 목록

- `GET /api/v1/matches/my` (YOUTH/GUARDIAN) → `MatchSummaryResponse[]`

#### 담당 인원 현황

- `GET /api/v1/matches/limit/me` (YOUTH)
- Response:
  ```json
  {
    "youthId": "uuid",
    "currentMatchCount": 2,
    "maxMatchCount": 5,
    "remainingMatchCount": 3,
    "canMatch": true
  }
  ```

#### 매칭 상세

- `GET /api/v1/matches/{matchId}` (YOUTH/GUARDIAN/ADMIN) → `MatchDetailResponse`

### 7.8 AvailableTime

#### 청년 가능 시간 등록

- `POST /api/v1/available-times/youth`
- Auth: Bearer JWT (YOUTH)
- Request:
  ```json
  {
    "startTime": "2026-05-25T14:00:00",
    "endTime": "2026-05-25T15:00:00"
  }
  ```
- Response (201): `AvailableTimeResponse`
- 에러: `AT002`(start≥end), `AT003`(중복)

#### 어르신 가능 시간 등록

- `POST /api/v1/elders/{elderId}/available-times`
- Auth: Bearer JWT (GUARDIAN, 본인이 등록한 어르신만)
- Request: 동일

#### 가능 시간 조회

- `GET /api/v1/available-times?ownerType=YOUTH&ownerId={uuid}`
- `ownerType`: `YOUTH` | `ELDER`
- `ownerType` 과 `ownerId` 는 모두 필수 (`AT005`)

### 7.9 Schedule

#### 일정 생성

- `POST /api/v1/schedules`
- Auth: Bearer JWT (YOUTH/ADMIN)
- Request:
  ```json
  {
    "matchId": "uuid",
    "scheduledStartAt": "2026-05-25T14:00:00",
    "scheduledEndAt": "2026-05-25T15:00:00"
  }
  ```
- 양측 가능 시간에 포함되어야 함 (`SC008`), 기존 일정과 충돌 금지 (`SC001`), MATCHED 상태 매칭만 (`SC007`)
- Response (201): `ScheduleResponse` (status=`CONFIRMED`)

#### 내 일정 목록

- `GET /api/v1/schedules/my` (YOUTH/GUARDIAN/ADMIN)

#### 일정 취소

- `PATCH /api/v1/schedules/{scheduleId}/cancel`
- Request (선택):
  ```json
  { "cancelReason": "어르신 컨디션 난조" }
  ```
- 이미 취소/완료된 일정은 차단 (`SC005`, `SC006`)

### 7.10 Call

#### 화상/음성 통화 시작

- `POST /api/v1/calls/video` / `/audio`
- Auth: `Authorization: Device {deviceToken}`
- Request:
  ```json
  {
    "matchId": "uuid",
    "scheduleId": "uuid"
  }
  ```
  - `scheduleId` 는 optional. 스케줄 없이도 통화 시작 가능 (즉시 통화).
- Response (201): `CallLogResponse` (status=`PENDING`)
- 에러: `CL006`(일정 미확정), `CL004`/`CL005`(matchId/scheduleId 불일치)

#### 통화 종료

- `PATCH /api/v1/calls/{callLogId}/end`
- Auth: `Authorization: Device {deviceToken}` **또는** `Authorization: Bearer {YOUTH JWT}`
- Request (선택):
  ```json
  { "endAt": "2026-05-25T15:05:00" }
  ```
- 이미 종료된 통화는 `CL003`

### 7.11 ActivityRecord

#### 활동 기록 작성

- `POST /api/v1/activity-records`
- Auth: Bearer JWT (YOUTH)
- Request:
  ```json
  {
    "matchId": "uuid",
    "scheduleId": "uuid",
    "callLogId": "uuid",
    "isCompleted": true,
    "actualStartAt": null,
    "actualEndAt": null,
    "durationMinutes": null,
    "notes": "산책 사진 이야기 즐겁게 나눴습니다."
  }
  ```
  - `callLogId` 가 있으면 그 통화의 startAt/endAt 으로 자동 계산
  - `callLogId` 없이 수동 입력하려면 `actualStartAt`, `actualEndAt` 또는 `durationMinutes` 제공
- Response (201):
  ```json
  {
    "activityRecordId": "uuid",
    "durationMinutes": 45,
    "totalDurationMinutes": 45
  }
  ```
- 에러: `AR001`/`AR004`(중복), `AR006`(CallLog 미종료), `AR010`(매칭 ENDED)

#### 활동 기록 목록

- `GET /api/v1/activity-records` (YOUTH/GUARDIAN/ADMIN)
- Response: `ActivityRecordSummaryResponse[]`

### 7.12 VolunteerStats

#### 내 누적 통계

- `GET /api/v1/youth/volunteer-stats/me`
- Auth: Bearer JWT (YOUTH)
- Response:
  ```json
  {
    "youthId": "uuid",
    "totalDurationMinutes": 720,
    "totalHours": 12,
    "totalCertifiedHours": 10,
    "availableCertificateHours": 2
  }
  ```

### 7.13 Certificate

#### 증명서 발급

- `POST /api/v1/youth/certificates`
- Auth: Bearer JWT (YOUTH)
- Request:
  ```json
  { "requestedHours": 10 }
  ```
  - `availableCertificateHours` 이상의 시간을 요청하면 `CT003`
  - 기준 시간 (10시간) 미만 누적 시 `CT003`
- Response (201): `CertificateResponse`
  ```json
  {
    "certificateId": "uuid",
    "certificateSerial": "DRDR-2026-0001",
    "title": "도란도란 사회참여 증명서",
    "youthId": "uuid",
    "certifiedHours": 10,
    "pdfUrl": null,
    "issuedAt": "2026-05-25T15:30:00"
  }
  ```
  - 현재 `pdfUrl` 은 null (PDF 실제 생성은 후순위)

#### 내 증명서 목록

- `GET /api/v1/youth/certificates/me` → `CertificateResponse[]`

### 7.14 Report

#### 신고 접수

- `POST /api/v1/reports`
- Auth: Bearer JWT (YOUTH/GUARDIAN)
- Request:
  ```json
  {
    "matchId": "uuid",
    "scheduleId": null,
    "targetUserId": "uuid",
    "targetElderId": null,
    "reportType": "ABUSIVE_LANGUAGE",
    "content": "통화 중 부적절한 표현이 있었습니다."
  }
  ```
  - `reportType` 은 `ReportType` enum (코드 참고)
- Response (201): `ReportResponse`

#### 관리자 신고 목록 / 처리

- `GET /api/v1/admin/reports?status=PENDING` → `AdminReportResponse[]`
- `PATCH /api/v1/admin/reports/{reportId}`
  ```json
  { "status": "RESOLVED", "adminMemo": "양측 가이드 안내 완료" }
  ```
- 이미 처리된 신고를 같은 상태로 다시 처리할 수 없음 (`R002`)

### 7.15 MatchTerminationRequest

#### 매칭 중단 요청

- `POST /api/v1/matches/{matchId}/termination-requests`
- Auth: Bearer JWT (YOUTH/GUARDIAN)
- Request:
  ```json
  { "reason": "어르신 건강 악화로 더 이상 진행이 어렵습니다." }
  ```
- 동일 매칭에 이미 REQUESTED 가 있으면 `MT002`

#### 관리자 중단 요청 목록 / 처리

- `GET /api/v1/admin/match-termination-requests?status=REQUESTED`
- `PATCH /api/v1/admin/match-termination-requests/{requestId}`
  ```json
  { "status": "APPROVED", "adminMemo": "보호자 의사 확인" }
  ```
  - `APPROVED` 시 매칭의 status 가 `ENDED` 로 변경됨
  - 이미 처리된 요청 재처리 차단 (`MT003`)

### 7.16 HelpRequest

#### 도움 요청 생성 (Device)

- `POST /api/v1/help-requests`
- Auth: `Authorization: Device {deviceToken}`
- Request (선택):
  ```json
  {
    "requestType": "GENERAL",
    "deviceStatus": { "battery": 32, "wifi": "weak" }
  }
  ```
  - body 가 null 이어도 생성됨 (어르신이 버튼만 누른 경우)
- Response (201): `HelpRequestResponse`

#### 관리자 도움 요청 목록 / 처리

- `GET /api/v1/admin/help-requests?status=PENDING`
- `PATCH /api/v1/admin/help-requests/{helpRequestId}`
  ```json
  { "status": "RESOLVED" }
  ```
  - 이미 처리된 도움 요청 재처리 차단 (`H002`)

---

## 8. 프론트 연동 시 주의사항

1. **Authorization 헤더 prefix**: 사용자 JWT 는 `Bearer `, 전용 기기 토큰은 `Device ` (공백 포함). 절대 혼용 금지.
2. **시간/시간대**: 모든 `LocalDateTime` 필드는 Asia/Seoul 기준의 **wall clock**. ISO-8601 (예: `2026-05-25T14:00:00`) 로 송수신. 타임존 suffix(`+09:00`, `Z`) 를 붙이면 서버 측 LocalDateTime 파서가 거부할 수 있음.
3. **승인 상태 변화**: 청년이 처음 로그인할 때 `approvalStatus` 가 null (프로필 미등록) 일 수 있음. 프론트는 `null` → 프로필 등록 화면, `PENDING` → 대기 화면, `REJECTED` → 사유 표시, `APPROVED` → 메인 화면으로 분기.
4. **매칭 차단**: 청년이 PENDING 상태면 어르신 목록/매칭 API 호출 시 권한 에러. 프론트는 `/users/me` 결과로 선제 차단 권장.
5. **가능 시간 등록 순서**: 일정을 생성하려면 **양측이 모두** 같은 시간대를 등록해야 함. UI 에서 보호자 등록을 강제하거나, 어르신 가능 시간이 비어있을 때 안내 필요.
6. **Device 메인 응답의 `todaySchedule`**: 오늘 KST 기준 확정 일정이 없으면 `null`. 프론트는 null 안전 처리 필수.
7. **증명서**: 현재 `pdfUrl` 은 항상 null. 시리얼/누적시간 표시만 가능.
8. **에러 코드 우선 분기**: HTTP status 외에 응답 body 의 `code` 값으로 분기 (예: 같은 403 이라도 U004/U005/U006 분기).
9. **DTO Enum 값 케이스**: 모두 대문자 SNAKE 또는 UPPER (`MATCHED`, `AVAILABLE`, `REGISTERED`, `VIDEO`, ...). 직렬화 시 enum name 그대로 송수신.
10. **CORS Origin**: 현재 `http://localhost:*`, `http://127.0.0.1:*` 만 허용. 프론트 배포 도메인이 생기면 백엔드 SecurityConfig 의 `setAllowedOriginPatterns` 에 추가 필요.

---

## 9. 현재 미구현 / 후순위 기능

| 항목 | 비고 |
| --- | --- |
| 회원가입 / 비밀번호 변경 / 비밀번호 찾기 | seed/관리자 등록 가정 |
| 토큰 재발급 (`/auth/refresh`) | 만료 시 재로그인 |
| OAuth / SNS 로그인 (카카오 등) | 미구현 |
| 알림 / Push (FCM) | 미구현 |
| 증명서 PDF 실제 생성 | `pdfUrl` 은 null |
| 외부 파일 스토리지 (S3 등) | URL 문자열만 저장 |
| 통화 미디어 시그널링/WebRTC | `CallLog` 만 기록 |
| Notification 도메인 | 미구현 |
| 보호자 연락처 인증 (PhoneVerification) | **v2.0에서 폐기 — 구현/테스트 대상 제외** |
| 결제 / 구독 / 정산 / 빌링 | **v2.0에서 폐기 — 구현/테스트 대상 제외** |

---

## 10. 확인된 테스트 상태

| 명령 | 결과 |
| --- | --- |
| `./gradlew compileJava` | BUILD SUCCESSFUL |
| `./gradlew compileTestJava` | BUILD SUCCESSFUL |
| `./gradlew test` | **28 / 28 통과** (실패 0, 스킵 0) |

세부 TC 매핑은 [BACKEND_QA_CHECKLIST.md](./BACKEND_QA_CHECKLIST.md#6-테스트-커버리지-요약) 참조.

문제 발견 시 우선 `./gradlew test --rerun-tasks --info` 로 로그 확인 후 백엔드 팀에 공유 부탁드립니다.
