# Movie Info API

TMDB(The Movie Database)에서 인기 영화 정보를 가져와 저장하고, 저장된 영화 목록을 제공하는 Spring Boot REST API 서버입니다.

컴퓨터공학과 학술 동아리 MAP의 방학 커리큘럼에서 부원들이 따라 만들 예제로 처음 제작했고, 이후 기능 보강과 AWS 배포까지 확장했습니다.

- API 문서 (Swagger): http://13.125.113.245:8080/swagger-ui/index.html

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1, Spring Data JPA |
| Database | MySQL |
| 외부 API | TMDB API (RestClient) |
| 문서화 | Swagger (springdoc-openapi) |
| 배포 | AWS EC2 (Ubuntu), Docker, Docker Compose |

## 배포 구성

```
          인터넷
             │  :8080 (보안 그룹: 8080 전체 허용, SSH는 내 IP만)
┌────────────▼──────────────── AWS EC2 (t3.micro) ┐
│  Docker Compose                                  │
│   ┌────────────┐      db:3306     ┌───────────┐ │
│   │ movie-app  │ ───────────────▶ │ movie-db  │ │
│   │ Spring Boot│                  │ MySQL 8.0 │ │
│   └────────────┘                  └─────┬─────┘ │
│                                   volume │       │
└──────────────────────────────────────────┼───────┘
                                    movie-db-data
```

- 서버에서 `git clone` 후 `.env`(DB 비밀번호, TMDB 키)를 직접 만들고 `docker compose up -d --build`로 실행합니다
- MySQL 포트는 외부에 열지 않고, 앱 컨테이너만 내부 네트워크로 접속합니다
- 메모리 1GB 인스턴스에서 Gradle 빌드가 가능하도록 2GB 스왑을 추가했습니다

## 트러블슈팅

| 문제 | 원인 | 해결 |
|---|---|---|
| EC2에 SSH 접속이 되지 않음 | 보안 그룹에 SSH(22) 인바운드 규칙이 빠져 있었음 | SSH 규칙을 추가하고 소스를 내 IP로 제한 |
| EC2에서 Docker 빌드 시 `./gradlew: Permission denied` | Windows에서 커밋해 `gradlew`의 실행 권한이 저장되지 않음 | git에 실행 권한을 기록(`git update-index --chmod=+x`)하고 Dockerfile에서도 권한 부여 |
| 같은 영화가 여러 번 저장됨 | 저장 시 중복 여부를 확인하지 않음 | TMDB 영화 ID를 유니크 컬럼으로 저장하고, 이미 있으면 건너뜀 |

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

### Docker로 실행 (앱 + MySQL)

로컬에 MySQL이 없어도 Docker만 있으면 실행할 수 있습니다. `.env`의 `DB_PASSWORD`, `TMDB_API_KEY`를 사용합니다.

```bash
docker compose up --build
```

- `Dockerfile`은 멀티 스테이지 빌드로 구성했습니다. JDK 이미지에서 jar를 만들고, 실행 이미지에는 JRE와 jar만 담습니다
- 앱 컨테이너는 서비스 이름(`db`)으로 MySQL 컨테이너에 접속하고, MySQL이 준비된 뒤에 시작합니다 (healthcheck)
- DB 데이터는 `movie-db-data` 볼륨에 저장되어 컨테이너를 다시 띄워도 유지됩니다
