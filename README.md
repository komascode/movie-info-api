# Movie Info API

TMDB에서 인기 영화 정보를 가져와 저장·조회하고, 자연어 요청으로 영화를 추천하는 Spring Boot REST API 서버입니다.

- API 문서 (Swagger): http://13.209.73.123:8080/swagger-ui/index.html

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1, Spring Data JPA |
| Database | MySQL 8 |
| AI | 솔트룩스 LUXIA Cloud, Spring AI + Google Gemini |
| 외부 API | TMDB API |
| 배포 | AWS EC2, Docker Compose |
| CI/CD | GitHub Actions, GitHub Container Registry |

## API

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/tmdb/save` | TMDB 인기 영화 저장. 이미 있는 영화는 최신 정보로 갱신 |
| GET | `/movies` | 영화 목록 조회 (`?title=`로 제목 검색) |
| GET | `/movies/{id}` | 영화 상세 조회. 없으면 404 |
| POST | `/recommend` | 자연어 요청으로 저장된 영화 중에서 추천. IP당 하루 20회 |

## 구조

```
Controller ─▶ Service ─▶ Repository ─▶ MySQL
                 ├─▶ TmdbClient ─▶ TMDB API
                 └─▶ MovieRecommender ─▶ LUXIA / Gemini
```

## AI 영화 추천

```
POST /recommend  { "message": "평점 높은 액션 영화 하나만" }
   │
   ├─ 사용량 확인 (IP당 하루 20회, 초과 시 429)
   │
   └─ MovieRecommender (설정에 따라 하나가 주입됨)
        ├─ LUXIA  : 서버가 DB에서 후보 영화를 조회해 요청과 함께 전달 → LLM은 그 안에서 선택
        └─ Gemini : LLM이 searchMovies 도구를 호출해 DB에서 후보 조회 (Function Calling)
```

- **모델 교체**: `MovieRecommender` 인터페이스와 `@ConditionalOnProperty`로, 설정(`AI_PROVIDER`)만 바꿔 모델을 교체합니다
- **환각 방지**: DB에 있는 영화 안에서만 추천합니다
- **비용 관리**: 호출 횟수·입력 길이를 제한하고, LLM에 넘기는 줄거리는 150자로 줄였습니다

응답 시간 (같은 질문 기준, 평균)

| 모델 | 호출 방식 | 응답 시간 |
|---|---|---|
| gemini-3.5-flash | 도구 호출 | 39.4초 |
| gemini-3.5-flash-lite | 도구 호출 | 3.2초 |
| gemini-3.5-flash-lite | 후보 전달 | 1.5초 |
| luxia3 (LUXIA Cloud) | 후보 전달 | 2.0초 |

## CI/CD

```
push (main)
  ├─ test       : MySQL 컨테이너를 띄워 테스트
  ├─ build-push : Docker 이미지 빌드 → GHCR 업로드
  └─ deploy     : EC2에 SSH 접속 → 앱 컨테이너만 새 이미지로 교체
```

- 배포하는 동안에만 GitHub Actions 러너 IP를 보안 그룹에 허용하고, 끝나면 삭제합니다 (최소 권한 IAM)
- API 키는 GitHub Secrets로 관리합니다

## 실행 방법

`.env`에 값을 채운 뒤 Docker Compose로 실행합니다. (`.env`는 저장소에 올라가지 않습니다)

```properties
DB_PASSWORD=your_password
TMDB_API_KEY=your_tmdb_api_key
LUXIA_API_KEY=your_luxia_api_key
GEMINI_API_KEY=your_gemini_api_key
```

```bash
docker compose up --build
```
