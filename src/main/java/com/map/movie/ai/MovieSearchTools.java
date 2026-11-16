package com.map.movie.ai;

import com.map.movie.entity.Movie;
import com.map.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * LLM이 호출할 수 있는 도구 모음.
 * LLM은 "이 도구를 이런 값으로 불러 줘"라고 요청만 하고, 실제 실행은 이 서버 코드가 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovieSearchTools {

    private static final int MAX_RESULTS = 15;
    private static final int OVERVIEW_LIMIT = 150;

    private final MovieRepository movieRepository;

    @Tool(description = "우리 서비스 DB에 저장된 영화를 검색한다. 추천할 때는 반드시 이 도구의 결과 안에서만 골라야 한다.")
    public List<MovieSummary> searchMovies(
            @ToolParam(description = "장르 이름. 액션, 모험, 애니메이션, 코미디, 범죄, 다큐멘터리, 드라마, 가족, 판타지, 역사, 공포, 음악, 미스터리, 로맨스, SF, 스릴러, 전쟁, 서부 중 하나. 장르 조건이 없으면 비워 둔다.",
                    required = false) String genre,
            @ToolParam(description = "최소 평점(0~10). 조건이 없으면 비워 둔다.", required = false) Double minRating) {

        log.info("[AI tool] searchMovies genre={}, minRating={}", genre, minRating);

        List<Movie> movies = (genre == null || genre.isBlank())
                ? movieRepository.findAll()
                : movieRepository.findByGenresContaining(genre.trim());

        return movies.stream()
                .filter(m -> minRating == null || (m.getVoteAverage() != null && m.getVoteAverage() >= minRating))
                .sorted(Comparator.comparing(Movie::getVoteAverage,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MAX_RESULTS)
                .map(MovieSearchTools::toSummary)
                .toList();
    }

    // LLM에 넘길 때는 꼭 필요한 정보만 짧게 담는다 (토큰 = 비용)
    private static MovieSummary toSummary(Movie m) {
        String overview = m.getOverview();
        if (overview != null && overview.length() > OVERVIEW_LIMIT) {
            overview = overview.substring(0, OVERVIEW_LIMIT) + "…";
        }
        return new MovieSummary(m.getId(), m.getTitle(), m.getGenres(), m.getReleaseDate(), m.getVoteAverage(), overview);
    }

    public record MovieSummary(Long id, String title, String genres, String releaseDate,
                               Double voteAverage, String overview) {
    }
}
