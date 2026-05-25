# 도란도란 Backend

카카오 테크포임팩트 클래스 x 안무서운회사 - 도란도란 프로젝트 백엔드 리포지토리.

## 빠른 시작

```bash
# 1) MySQL 기동 (부모 저장소 docker-compose.yml 사용)
cd /workspace && docker compose up -d

# 2) 부트런
cd /workspace/backend && ./gradlew bootRun
# → http://localhost:8080  (Swagger: /swagger-ui.html)

# 3) 테스트
./gradlew test
```

자세한 절차/환경변수/트러블슈팅: [docs/BACKEND_RUNBOOK.md](docs/BACKEND_RUNBOOK.md)

## 문서

| 문서 | 내용 |
| --- | --- |
| [docs/FRONTEND_API_HANDOFF.md](docs/FRONTEND_API_HANDOFF.md) | 프론트엔드 연동 가이드 — API 목록, 인증, 테스트 계정, 역할별 플로우 |
| [docs/BACKEND_RUNBOOK.md](docs/BACKEND_RUNBOOK.md) | 백엔드 실행/테스트/트러블슈팅 절차 |
| [docs/BACKEND_QA_CHECKLIST.md](docs/BACKEND_QA_CHECKLIST.md) | Step K-0 최종 QA 결과 (테스트/Security/폐기 코드 잔재) |

## 스택

- Java 21, Spring Boot 3.5
- Spring Security + JWT (jjwt 0.12)
- Spring Data JPA + MySQL 8 (테스트: H2)
- springdoc-openapi (Swagger UI)
- JUnit 5

## 인증 요약

- 일반 사용자 (YOUTH/GUARDIAN/ADMIN): `Authorization: Bearer {JWT}`
- 어르신 전용 기기: `Authorization: Device {deviceToken}`
- 자세한 분기: [FRONTEND_API_HANDOFF.md §4.4](docs/FRONTEND_API_HANDOFF.md#44-인증-방식)
