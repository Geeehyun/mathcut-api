# mathcut-api

수학 교재 이미지 편집기 **MathCut**의 백엔드 API 서버입니다.

- 프론트엔드: [mathcut-front](https://github.com/Geeehyun/mathcut-front)
- 서비스 URL: https://mathcut.emotion-flowerbed.my

## 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.0 |
| Build | Gradle 8.13 |
| DB | MariaDB (AWS RDS) |
| Cache | Redis (DB 1) |
| Auth | JWT (Access 1h / Refresh 14d) |
| AI | OpenAI GPT-4o Vision |

## 실행 방법

### 로컬 개발

```bash
./gradlew bootRun
```

`application-local.yml` 기준으로 실행됩니다. (`localhost:9090`)

### 환경 변수 (프로덕션)

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `OPENAI_API_KEY` | OpenAI API 키 | 필수 |
| `OPENAI_MODEL` | 사용할 모델 | `gpt-4o` |
| `JWT_SECRET` | JWT 서명 키 (32자+) | 필수 |
| `JWT_EXPIRATION_MS` | Access Token 만료 | `3600000` (1h) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh Token 만료 | `1209600000` (14d) |
| `DB_USERNAME` | DB 계정 | 필수 |
| `DB_PASSWORD` | DB 비밀번호 | 필수 |
| `REDIS_HOST` | Redis 호스트 | 필수 |
| `REDIS_PORT` | Redis 포트 | `6379` |
| `REDIS_PASSWORD` | Redis 비밀번호 | - |
| `CORS_ALLOWED_ORIGINS` | 허용 Origin | `http://localhost:4000` |

## 프로젝트 구조

```
src/main/java/com/vision/mathcut/
├── config/          # Security, JWT, CORS, Redis 설정
├── controller/      # AuthController, CutController, AISketchController
├── domain/          # User, Cut (JPA Entity)
├── dto/             # Request / Response DTO
├── repository/      # UserRepository, CutRepository
└── service/         # 비즈니스 로직, AI 스케치, 프롬프트 빌더
```

## API 목록

| Method | URL | 인증 | 설명 |
|--------|-----|------|------|
| POST | `/api/auth/register` | ✗ | 회원가입 |
| POST | `/api/auth/login` | ✗ | 로그인 |
| POST | `/api/auth/refresh` | ✗ | 토큰 갱신 |
| POST | `/api/auth/logout` | ✓ | 로그아웃 |
| POST | `/api/ai/sketch` | ✗ | AI 스케치 분석 |
| GET | `/api/cuts` | ✓ | 내 컷 목록 |
| GET | `/api/cuts/{id}` | ✓ | 컷 상세 |
| POST | `/api/cuts` | ✓ | 컷 저장 |
| PUT | `/api/cuts/{id}` | ✓ | 컷 수정 |
| DELETE | `/api/cuts/{id}` | ✓ | 컷 삭제 |

자세한 API 명세는 [API-SPEC.md](./API-SPEC.md)를 참고하세요.

## Nginx 설정

`/api/` 경로는 Spring Boot(9090), 나머지는 Vue 프론트(4000)로 프록시합니다.

```nginx
location /api/ {
    proxy_pass http://localhost:9090;
}
location / {
    proxy_pass http://localhost:4000;
}
```
