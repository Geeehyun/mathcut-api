# mathcut-api API 명세서

**Base URL:** `http://localhost:9090`
**Content-Type:** `application/json`
**인증 방식:** Bearer Token (JWT)

---

## 공통

### 인증 헤더 (로그인 필요 API)
```
Authorization: Bearer {accessToken}
```

### 공통 에러 응답
```json
{ "error": "에러 메시지" }
```

| HTTP 상태 | 설명 |
|-----------|------|
| 400 | 입력값 오류 |
| 401 | 인증 필요 (토큰 없음 / 만료 / 로그아웃) |
| 403 | 접근 권한 없음 (다른 유저의 리소스) |
| 404 | 리소스 없음 |
| 500 | 서버 오류 |

---

## 인증 API

### 회원가입
```
POST /api/auth/register
```
**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "닉네임"
}
```
| 필드 | 타입 | 필수 | 조건 |
|------|------|------|------|
| email | string | O | 이메일 형식 |
| password | string | O | 8자 이상 |
| nickname | string | O | 2~20자 |

**Response 200**
```json
{ "message": "가입되었습니다." }
```
**Response 400** - 이메일 중복
```json
{ "error": "이미 사용 중인 이메일입니다." }
```

---

### 로그인
```
POST /api/auth/login
```
**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response 200**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "nickname": "닉네임"
}
```
| 필드 | 설명 |
|------|------|
| accessToken | 1시간 유효 |
| refreshToken | 14일 유효, Redis 저장 |

**Response 400** - 이메일/비밀번호 오류
```json
{ "error": "이메일 또는 비밀번호가 올바르지 않습니다." }
```

---

### 토큰 갱신
```
POST /api/auth/refresh
```
> 로그인 불필요 (refreshToken으로 인증)

**Request Body**
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response 200** - 새 토큰 쌍 발급 (Refresh Token Rotation)
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "nickname": "닉네임"
}
```
**Response 400** - 유효하지 않거나 만료된 토큰
```json
{ "error": "리프레시 토큰이 만료되었거나 로그아웃된 계정입니다." }
```

---

### 로그아웃
```
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

**Response 204** - No Content
> accessToken 블랙리스트 등록 + refreshToken 삭제

---

## AI 스케치 API

### AI 스케치 분석
```
POST /api/ai/sketch
```
> 로그인 불필요

**Request Body**
```json
{
  "imageDataUrl": "data:image/png;base64,...",
  "forcedShapeType": "triangle-right",
  "userHint": "AC=3cm, CB=6cm, C가 직각"
}
```
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| imageDataUrl | string | O | base64 이미지 (data URL) |
| forcedShapeType | string | O | 도형 타입 |
| userHint | string | X | 추가 힌트 |

**지원 도형 타입**
`triangle` `triangle-right` `triangle-equilateral` `triangle-isosceles`
`rect-square` `rect-rectangle` `rect-rhombus` `rect-parallelogram` `rect-trapezoid`
`circle` `polygon` `free-shape` `point` `segment` `line` `ray` `angle-line` `arrow` `arrow-curve`

**Response 200**
```json
{
  "shapes": [
    {
      "type": "triangle-right",
      "points": [
        {"gridX": 20, "gridY": 23},
        {"gridX": 26, "gridY": 26},
        {"gridX": 20, "gridY": 26}
      ],
      "pointLabels": ["A", "B", "C"],
      "guideVisibility": { "pointName": true, "point": false, "length": true, "angle": true },
      "lengthItems": [...],
      "angleItems": [...]
    }
  ],
  "guides": []
}
```

---

## 컷 API (로그인 필요)

### 내 컷 목록 조회
```
GET /api/cuts
Authorization: Bearer {token}
```

**Response 200**
```json
[
  {
    "id": 1,
    "title": "직각삼각형 문제",
    "thumbnail": "data:image/png;base64,...",
    "createdAt": "2026-03-12T09:22:26",
    "updatedAt": "2026-03-12T09:22:26"
  }
]
```
> 생성일 내림차순 정렬 / thumbnail은 없으면 null

---

### 컷 상세 조회
```
GET /api/cuts/{id}
Authorization: Bearer {token}
```

**Response 200**
```json
{
  "id": 1,
  "title": "직각삼각형 문제",
  "canvasData": {
    "shapes": [...],
    "guides": [...]
  },
  "thumbnail": "data:image/png;base64,...",
  "createdAt": "2026-03-12T09:22:26",
  "updatedAt": "2026-03-12T09:22:26"
}
```
**Response 403** - 다른 유저의 컷
**Response 404** - 컷 없음

---

### 컷 저장
```
POST /api/cuts
Authorization: Bearer {token}
```
**Request Body**
```json
{
  "title": "직각삼각형 문제",
  "canvasData": {
    "shapes": [...],
    "guides": [...]
  },
  "thumbnail": "data:image/png;base64,..."
}
```
| 필드 | 타입 | 필수 | 조건 |
|------|------|------|------|
| title | string | O | 200자 이하 |
| canvasData | object | O | shapes/guides JSON |
| thumbnail | string | X | base64 data URL |

**Response 200**
```json
{ "id": 1 }
```

---

### 컷 수정
```
PUT /api/cuts/{id}
Authorization: Bearer {token}
```
**Request Body** - 저장과 동일

**Response 200**
```json
{ "id": 1 }
```
**Response 403** - 다른 유저의 컷
**Response 404** - 컷 없음

---

### 컷 삭제
```
DELETE /api/cuts/{id}
Authorization: Bearer {token}
```

**Response 204** - No Content
**Response 403** - 다른 유저의 컷
**Response 404** - 컷 없음

---

## 실행 방법

```bash
# 로컬 개발 (application-local.yml 사용)
./gradlew bootRun

# 프로덕션
SPRING_PROFILES_ACTIVE=prod \
  DB_USERNAME=... \
  DB_PASSWORD=... \
  OPENAI_API_KEY=sk-... \
  JWT_SECRET=... \
  REDIS_HOST=... \
  REDIS_PASSWORD=... \
  java -jar mathcut-api.jar

# 환경 변수 전체
SPRING_PROFILES_ACTIVE=prod
DB_USERNAME=...
DB_PASSWORD=...
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4o              # 기본값
JWT_SECRET=...                   # 32자 이상
JWT_EXPIRATION_MS=3600000        # 1시간 (기본값)
JWT_REFRESH_EXPIRATION_MS=1209600000  # 14일 (기본값)
REDIS_HOST=...
REDIS_PORT=6379                  # 기본값
REDIS_PASSWORD=...
CORS_ALLOWED_ORIGINS=https://mathcut.emotion-flowerbed.my
```
