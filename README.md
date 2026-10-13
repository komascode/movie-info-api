# Movie Info API

TMDB(The Movie Database)에서 인기 영화 정보를 가져와 저장하고, 저장된 영화 목록을 제공하는 Spring Boot REST API 서버입니다.

컴퓨터공학과 학술 동아리 MAP의 방학 커리큘럼에서 부원들이 따라 만들 예제로 처음 제작했고, 이후 배포 환경까지 확장하고 있습니다.

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1, Spring Data JPA |
| Database | MySQL |
| 외부 API | TMDB API |

## API

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/tmdb/save` | TMDB 인기 영화 목록을 가져와 DB에 저장 |
| GET | `/movies` | 저장된 영화 목록 조회 |

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
