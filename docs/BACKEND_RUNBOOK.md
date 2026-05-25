# 도란도란 Backend Runbook

> v2.0 백엔드 로컬/개발 환경 실행 절차. 운영 환경 변수 값/시크릿은 본 문서에 포함하지 않는다.

## Requirements

| 도구 | 버전 |
| --- | --- |
| Java | **21** (JDK 21, Temurin/OpenJDK 호환) |
| Gradle | wrapper (`./gradlew`) — 별도 설치 불필요 |
| MySQL | **8.x** (운영/로컬 부트런) |
| Docker | 선택 사항 (로컬 MySQL 컨테이너) |

테스트 실행만 한다면 Java 21 만 있으면 된다. (H2 인메모리 사용)

---

## Local Run

### 1. MySQL 준비

**옵션 A — Docker (권장)**

부모 저장소 `/workspace` 의 [`docker-compose.yml`](../../docker-compose.yml) 사용:

```bash
cd /workspace
cp .env.example .env       # 최초 1회. 비밀번호 변경 원하면 .env 수정
docker compose up -d       # MySQL 컨테이너 기동
```

기본 접속 정보:

| 항목 | 값 |
| --- | --- |
| host | `localhost` |
| port | `3306` |
| db | `dorandoran` |
| user | `dorandoran` |
| password | `dorandoran1234!` |
| timezone | `Asia/Seoul` (+09:00) |

**옵션 B — 로컬 설치 MySQL**

```sql
CREATE DATABASE dorandoran
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'dorandoran'@'%' IDENTIFIED BY '<your-password>';
GRANT ALL PRIVILEGES ON dorandoran.* TO 'dorandoran'@'%';
FLUSH PRIVILEGES;
```

MySQL 서버 타임존을 `+09:00` 으로 설정하기를 권장 (`default-time-zone='+09:00'`).

### 2. 환경변수

[`application.yaml`](../src/main/resources/application.yaml) 에서 사용하는 환경변수:

| 변수 | 기본값 | 비고 |
| --- | --- | --- |
| `DB_HOST` | `localhost` | MySQL 호스트 |
| `DB_PORT` | `3306` | MySQL 포트 |
| `DB_NAME` | `dorandoran` | 스키마 이름 |
| `DB_USERNAME` | `dorandoran` | DB 사용자 |
| `DB_PASSWORD` | `dorandoran1234!` | DB 비밀번호 |
| `JWT_SECRET` | (로컬 기본값) | **운영 배포 시 반드시 32바이트 이상의 시크릿으로 교체** |

> 실제 운영 비밀번호 / 시크릿은 본 문서에 적지 않는다. 운영 시크릿은 별도 보안 채널/시크릿 매니저로 관리.

### 3. 부트런

```bash
cd /workspace/backend
./gradlew bootRun
```

기동 후:

- 서버: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- 헬스 체크: `http://localhost:8080/actuator/health`

부트런 시 `DataInitializer` (Profile=`!test`) 가 다음 seed 를 자동 생성한다.

- ADMIN: `admin@test.com`
- YOUTH: `youth@test.com`, `youth_approved@test.com`, `youth_pending@test.com`, `youth_rejected@test.com`, `youth_banned@test.com`
- GUARDIAN: `guardian@test.com`
- 어르신 `박도란`, Device `SEED-TABLET-0001` (token: `seed-device-token-0001`)
- YOUTH/ELDER 가능 시간 (D+1 14:00~15:00 KST)
- 모든 계정 비밀번호: `test1234!`

> 자세한 핸드오프 정보는 [`FRONTEND_API_HANDOFF.md`](./FRONTEND_API_HANDOFF.md) 참조.

---

## Database

- 운영 yaml: `spring.jpa.hibernate.ddl-auto=update` (MVP 단계 정책)
- 운영 yaml: Hibernate JDBC time_zone = `Asia/Seoul`
- 테스트 yaml: `ddl-auto=create-drop`, H2 in-memory + MySQL 모드

### 초기화

```bash
docker compose down -v   # 컨테이너 + 볼륨 모두 삭제 → 스키마 재생성
docker compose up -d
```

---

## Test

```bash
./gradlew test                 # 캐시 활용
./gradlew test --rerun-tasks   # 강제 재실행
```

- 프로파일: `test`
- 데이터소스: H2 in-memory (`jdbc:h2:mem:dorandoran_test;MODE=MySQL`)
- JVM 타임존: `Asia/Seoul` (`build.gradle` 의 `systemProperty 'user.timezone'` 에서 고정)
- 총 28건 통합 테스트
- 리포트: `build/reports/tests/test/index.html`

`application-test.yaml` 은 **테스트 전용** 이다. 운영 부트런(`bootRun`)에는 사용되지 않는다.

---

## Swagger

기동 후 `http://localhost:8080/swagger-ui.html` 에서 전 도메인 API 를 확인할 수 있다. (springdoc-openapi)

OpenAPI 스펙 JSON 이 필요하면 `http://localhost:8080/v3/api-docs` 를 받아 사용한다.

---

## Troubleshooting

### Java 21 이 없는 경우

```bash
# macOS (sdkman)
sdk install java 21-tem
sdk use java 21-tem

# Linux (Temurin)
# https://adoptium.net/ 에서 OS 별 패키지 설치

java -version
# openjdk version "21.x.x"
```

`./gradlew --version` 으로 Gradle 이 인식한 JDK 가 21 인지 확인.

### MySQL 연결 실패 (`Communications link failure`)

1. 컨테이너가 살아있는지 확인: `docker compose ps`
2. 포트 충돌(3306) 확인: `lsof -i:3306`
3. `.env` 의 `MYSQL_*` 값과 `application.yaml` 환경변수가 일치하는지 확인
4. 타임존 이슈: MySQL 서버 TZ 가 다르면 JDBC URL 의 `serverTimezone=Asia/Seoul` 그대로 사용 권장

### `./gradlew test` 는 되는데 `bootRun` 이 실패

- 거의 대부분 **MySQL 미기동/접속 실패** 가 원인. 테스트는 H2 인메모리라 영향 없음.
- 로그에서 `Hikari` / `Communications link failure` / `Access denied` 를 확인.
- `application.yaml` 의 `spring.datasource.url` 호스트/포트/DB 이름 점검.

### Device token 호출 방법

```bash
curl -X GET http://localhost:8080/api/v1/device/main \
  -H 'Authorization: Device seed-device-token-0001'
```

`Bearer` 가 아니라 `Device` 임에 주의. 자세한 흐름은 [FRONTEND_API_HANDOFF.md](./FRONTEND_API_HANDOFF.md#44-인증-방식) 4.4 절 참고.

### 청년 로그인이 403 으로 차단됨

`AuthUserResponse.status` / `approvalStatus` 확인:

| 응답 | code | 조치 |
| --- | --- | --- |
| `status=SUSPENDED` | `U004` | 관리자 제재 해제 필요 (현재 UI 미제공) |
| `approvalStatus=PENDING` | `U005` | 관리자가 `/admin/youths/{id}/approval` 로 승인 |
| `approvalStatus=REJECTED` | `U006` | 관리자가 같은 API 로 재승인 |

### 시간대 관련 이상 (예: 오늘 일정 안 보임)

- 서버는 항상 `Asia/Seoul` 기준으로 "오늘" 을 계산한다 (`DeviceMainService.SERVICE_ZONE`).
- 호스트 TZ 가 UTC 인데 `LocalDateTime.now()` 를 그대로 던지면 KST 자정을 넘는 경우가 생긴다.
- 프론트는 사용자가 의도한 KST wall clock 으로 ISO-8601 LocalDateTime (`yyyy-MM-ddTHH:mm:ss`) 을 전송한다.
