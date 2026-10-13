# Movie Info API

TMDB(The Movie Database)에서 인기 영화 정보를 가져와 저장하고, 저장된 영화 목록을 제공하는 Spring Boot REST API 서버입니다.

컴퓨터공학과 학술 동아리 MAP의 방학 커리큘럼에서 부원들이 따라 만들 예제로 처음 제작했고, 이후 배포 환경까지 확장하고 있습니다.

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1, Spring Data JPA |
| Database | MySQL |
| 외부 API | TMDB API (RestClient) |
| 문서화 | Swagger (springdoc-openapi) |

## 구조

```
Controller ─▶ Service ─▶ Repository ─▶ MySQL
                 │
                 └─▶ TMDB API
```

- `MovieService`가 TMDB 호출과 저장, 조회 로직을 담당하고, 컨트롤러는 요청과 응답만 다룹니다
- 엔티티를 그대로 내보내지 않고 응답 전용 객체(`MovieResponse`)로 변환합니다
- 존재하지 않는 영화를 조회하면 `GlobalExceptionHandler`가 404와 에러 메시지를 반환합니다

## API

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/tmdb/save` | TMDB 인기 영화를 가져와 저장. 이미 저장된 영화(TMDB ID 기준)는 건너뜀 |
| GET | `/movies` | 저장된 영화 목록 조회 |
| GET | `/movies?title={검색어}` | 제목에 검색어가 들어간 영화 조회 |
| GET | `/movies/{id}` | 영화 상세 조회. 없으면 404 |

서버 실행 후 `/swagger-ui/index.html`에서 API를 직접 호출해 볼 수 있습니다.

### 응답 예시

`POST /tmdb/save`

```json
{ "fetchedCount": 20, "savedCount": 20 }
```

같은 요청을 다시 보내면 이미 저장된 영화는 건너뛰어 `savedCount`가 0이 됩니다.

`GET /movies/{id}` (없는 id)

```json
{ "status": 404, "message": "영화를 찾을 수 없습니다. id=999999" }
```

## 실행 방법

### 요구 사항

- Java 17
- MySQL 8
- TMDB API 키 ([발급 안내](https://developer.themoviedb.org/docs/getting-started))

### 환경변수 설정

프로젝트 루트에 `.env` 파일을 만들고 값을 채웁니다. 형식은 `.env.example`을 참고하세요. `.env`는 `.gitignore`에 포함되어 있어 저장소에 올라가지 않습니다.

```properties
DB_URL=jdbc:mysql://localhost:3306/movie
DB_USERNAME=root
DB_PASSWORD=your_password
TMDB_API_KEY=your_tmdb_api_key
```

배포 환경에서는 `.env` 파일 대신 같은 이름의 환경변수로 주입할 수 있습니다.

### 실행

```bash
./gradlew bootRun
```
