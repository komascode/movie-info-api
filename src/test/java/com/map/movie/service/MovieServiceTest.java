package com.map.movie.service;

import com.map.movie.client.TmdbClient;
import com.map.movie.dto.ImportResponse;
import com.map.movie.dto.MovieResponse;
import com.map.movie.dto.TmdbMovie;
import com.map.movie.entity.Movie;
import com.map.movie.exception.MovieNotFoundException;
import com.map.movie.repository.MovieRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

// Spring을 띄우지 않고 Mockito만 사용하는 단위 테스트
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    // 진짜 DB 대신 쓰는 가짜 Repository
    @Mock
    private MovieRepository movieRepository;

    // 진짜 TMDB 서버 대신 쓰는 가짜 TmdbClient
    @Mock
    private TmdbClient tmdbClient;

    // 위의 가짜들을 생성자로 넣어서 MovieService를 만든다
    @InjectMocks
    private MovieService movieService;

    @Test
    @DisplayName("있는 id로 조회하면 영화 정보를 응답으로 변환해 돌려준다")
    void getMovie_found() {
        // given: 가짜 DB가 1번 영화를 돌려주도록 정해 둔다
        Movie movie = movie(550L, "파이트 클럽", "/poster.jpg");
        given(movieRepository.findById(1L)).willReturn(Optional.of(movie));

        // when
        MovieResponse response = movieService.getMovie(1L);

        // then
        assertThat(response.tmdbId()).isEqualTo(550L);
        assertThat(response.title()).isEqualTo("파이트 클럽");
        assertThat(response.posterPath()).isEqualTo("/poster.jpg");
    }

    @Test
    @DisplayName("없는 id로 조회하면 MovieNotFoundException이 발생한다")
    void getMovie_notFound() {
        // given: 가짜 DB가 "없음"이라고 대답하도록 정해 둔다
        given(movieRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> movieService.getMovie(999L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("id=999");
    }

    @Test
    @DisplayName("제목 없이 조회하면 전체 영화 목록을 돌려준다")
    void getMovies_all() {
        // given: 가짜 DB가 전체 조회 시 영화 2편을 돌려주도록 정해 둔다
        Movie movie1 = movie(550L, "파이트 클럽", "/a.jpg");
        Movie movie2 = movie(680L, "펄프 픽션", "/b.jpg");
        given(movieRepository.findAll()).willReturn(List.of(movie1, movie2));

        // when: 제목 없이(null) 조회
        List<MovieResponse> result = movieService.getMovies(null);

        // then: 2편이 나오고, 검색 메서드는 한 번도 불리지 않아야 한다
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("파이트 클럽");
        then(movieRepository).should(never()).findByTitleContaining(any());
    }

    @Test
    @DisplayName("제목으로 조회하면 제목에 검색어가 들어간 영화만 돌려준다")
    void getMovies_byTitle() {
        // given: "클럽"으로 검색하면 1편을 돌려주도록 정해 둔다
        Movie movie = movie(550L, "파이트 클럽", "/a.jpg");
        given(movieRepository.findByTitleContaining("클럽")).willReturn(List.of(movie));

        // when
        List<MovieResponse> result = movieService.getMovies("클럽");

        // then: 1편이 나오고, 전체 조회는 불리지 않아야 한다
        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("파이트 클럽");
        then(movieRepository).should(never()).findAll();
    }

    @Test
    @DisplayName("TMDB 영화를 가져올 때 새 영화는 저장하고, 이미 있는 영화는 정보를 갱신한다")
    void importPopularMovies_savesNewAndUpdatesExisting() {
        // given: TMDB가 3편을 주고, 그중 2번(tmdbId=2)은 예전 정보로 이미 DB에 있다
        given(tmdbClient.fetchPopularMovies()).willReturn(List.of(
                tmdbMovie(1L, "영화1"),
                tmdbMovie(2L, "영화2"),
                tmdbMovie(3L, "영화3")
        ));
        Movie oldMovie2 = movie(2L, "옛날 제목", "/old.jpg");
        given(movieRepository.findByTmdbId(1L)).willReturn(Optional.empty());
        given(movieRepository.findByTmdbId(2L)).willReturn(Optional.of(oldMovie2));
        given(movieRepository.findByTmdbId(3L)).willReturn(Optional.empty());

        // when
        ImportResponse response = movieService.importPopularMovies();

        // then: 3편 중 2편은 새로 저장, 1편은 갱신
        assertThat(response.fetchedCount()).isEqualTo(3);
        assertThat(response.savedCount()).isEqualTo(2);
        assertThat(response.updatedCount()).isEqualTo(1);
        then(movieRepository).should(times(2)).save(any(Movie.class));

        // 이미 있던 영화는 save 없이 객체의 값만 최신으로 바뀌어 있어야 한다 (변경 감지 대상)
        assertThat(oldMovie2.getTitle()).isEqualTo("영화2");
        assertThat(oldMovie2.getGenres()).isEqualTo("드라마,로맨스");
        assertThat(oldMovie2.getVoteAverage()).isEqualTo(7.5);
    }

    @Test
    @DisplayName("TMDB가 빈 목록을 주면 아무것도 저장하지 않는다")
    void importPopularMovies_empty() {
        // given
        given(tmdbClient.fetchPopularMovies()).willReturn(List.of());

        // when
        ImportResponse response = movieService.importPopularMovies();

        // then
        assertThat(response.fetchedCount()).isZero();
        assertThat(response.savedCount()).isZero();
        then(movieRepository).should(never()).save(any(Movie.class));
    }

    // 테스트용 Movie를 간단히 만드는 도우미 메서드 (상세 정보는 비워 둔다)
    private Movie movie(Long tmdbId, String title, String posterPath) {
        return new Movie(tmdbId, title, posterPath, null, null, null, null);
    }

    // 테스트용 TmdbMovie를 간단히 만드는 도우미 메서드 (장르: 18=드라마, 10749=로맨스)
    private TmdbMovie tmdbMovie(Long id, String title) {
        TmdbMovie movie = new TmdbMovie();
        movie.setId(id);
        movie.setTitle(title);
        movie.setPoster_path("/" + id + ".jpg");
        movie.setOverview(title + " 줄거리");
        movie.setRelease_date("2026-01-01");
        movie.setVote_average(7.5);
        movie.setGenre_ids(List.of(18, 10749));
        return movie;
    }
}
