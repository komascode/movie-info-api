package com.map.movie.ai;

import com.map.movie.ai.CandidateRetriever.Condition;
import com.map.movie.ai.MovieSearchTools.MovieSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CandidateRetrieverTest {

    @Mock
    private MovieSearchTools movieSearchTools;

    @InjectMocks
    private CandidateRetriever candidateRetriever;

    @Test
    @DisplayName("질문에서 장르와 '평점 높은' 조건을 뽑는다")
    void parsesGenreAndHighRating() {
        Condition c = CandidateRetriever.parse("평점 높은 액션 영화 하나만");

        assertThat(c.genres()).containsExactly("액션");
        assertThat(c.minRating()).isEqualTo(CandidateRetriever.HIGH_RATING);
    }

    @Test
    @DisplayName("일상 표현도 장르로 바꾸고, '평점 8'처럼 숫자가 있으면 그 값을 쓴다")
    void parsesSynonymsAndRatingNumber() {
        Condition c = CandidateRetriever.parse("무서운 거나 sf 중에 평점 8 이상");

        assertThat(c.genres()).containsExactlyInAnyOrder("공포", "SF");
        assertThat(c.minRating()).isEqualTo(8.0);
    }

    @Test
    @DisplayName("조건이 없으면 장르·평점 모두 비어 있다")
    void noCondition() {
        Condition c = CandidateRetriever.parse("아무거나 추천해줘");

        assertThat(c.genres()).isEmpty();
        assertThat(c.minRating()).isNull();
    }

    @Test
    @DisplayName("질문의 장르로 DB를 검색해 그 결과를 후보로 돌려준다")
    void retrievesByGenre() {
        given(movieSearchTools.searchMovies("공포", null)).willReturn(List.of(summary(1L, "겟 아웃", 7.6)));

        List<MovieSummary> result = candidateRetriever.retrieve("무서운 영화 보고 싶어");

        assertThat(result).extracting(MovieSummary::title).containsExactly("겟 아웃");
    }

    @Test
    @DisplayName("조건에 맞는 영화가 없으면 평점 조건을 풀고, 그래도 없으면 전체 상위로 대신한다")
    void fallsBackWhenNothingMatches() {
        given(movieSearchTools.searchMovies("서부", 7.0)).willReturn(List.of());
        given(movieSearchTools.searchMovies("서부", null)).willReturn(List.of());
        given(movieSearchTools.searchMovies(null, null)).willReturn(List.of(summary(2L, "코코", 8.2)));

        List<MovieSummary> result = candidateRetriever.retrieve("평점 높은 서부 영화");

        assertThat(result).extracting(MovieSummary::title).containsExactly("코코");
    }

    @Test
    @DisplayName("장르가 여러 개면 합치고 중복을 빼서 평점 높은 순으로 돌려준다")
    void mergesMultipleGenres() {
        given(movieSearchTools.searchMovies("코미디", null)).willReturn(List.of(summary(1L, "A", 6.0), summary(3L, "C", 9.0)));
        given(movieSearchTools.searchMovies("로맨스", null)).willReturn(List.of(summary(3L, "C", 9.0), summary(2L, "B", 7.0)));

        List<MovieSummary> result = candidateRetriever.retrieve("웃긴 로맨스 영화");

        assertThat(result).extracting(MovieSummary::title).containsExactly("C", "B", "A");
    }

    private static MovieSummary summary(Long id, String title, Double rating) {
        return new MovieSummary(id, title, "장르", "2026-01-01", rating, "줄거리");
    }
}
