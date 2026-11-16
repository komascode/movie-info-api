package com.map.movie.ai;

import com.map.movie.ai.MovieSearchTools.MovieSummary;
import com.map.movie.entity.Movie;
import com.map.movie.repository.MovieRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MovieSearchToolsTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieSearchTools movieSearchTools;

    @Test
    @DisplayName("장르를 주면 장르로 조회하고, 평점이 높은 순으로 돌려준다")
    void searchByGenre() {
        given(movieRepository.findByGenresContaining("드라마")).willReturn(List.of(
                movie("영화A", "드라마", 6.0),
                movie("영화B", "드라마,로맨스", 8.5)
        ));

        List<MovieSummary> result = movieSearchTools.searchMovies("드라마", null);

        assertThat(result).extracting(MovieSummary::title).containsExactly("영화B", "영화A");
        then(movieRepository).should(never()).findAll();
    }

    @Test
    @DisplayName("최소 평점을 주면 그보다 낮은 영화는 빠진다")
    void filterByMinRating() {
        given(movieRepository.findAll()).willReturn(List.of(
                movie("낮음", "코미디", 5.0),
                movie("높음", "코미디", 8.0),
                movie("평점없음", "코미디", null)
        ));

        List<MovieSummary> result = movieSearchTools.searchMovies(null, 7.0);

        assertThat(result).extracting(MovieSummary::title).containsExactly("높음");
    }

    @Test
    @DisplayName("줄거리는 LLM에 넘기기 전에 150자로 자른다")
    void truncatesOverview() {
        Movie longMovie = new Movie(1L, "긴영화", "/p.jpg", "가".repeat(300), "드라마", "2026-01-01", 7.0);
        given(movieRepository.findAll()).willReturn(List.of(longMovie));

        MovieSummary summary = movieSearchTools.searchMovies(null, null).get(0);

        assertThat(summary.overview()).hasSize(151).endsWith("…");
    }

    private static Movie movie(String title, String genres, Double voteAverage) {
        return new Movie((long) title.hashCode(), title, "/p.jpg", "줄거리", genres, "2026-01-01", voteAverage);
    }
}
