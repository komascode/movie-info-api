package com.map.movie.service;

import com.map.movie.client.TmdbClient;
import com.map.movie.client.TmdbGenres;
import com.map.movie.dto.ImportResponse;
import com.map.movie.dto.MovieResponse;
import com.map.movie.dto.TmdbMovie;
import com.map.movie.entity.Movie;
import com.map.movie.exception.MovieNotFoundException;
import com.map.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final TmdbClient tmdbClient;

    /**
     * TMDB 인기 영화 목록을 가져온다.
     * 처음 보는 영화(tmdbId 기준)는 새로 저장하고, 이미 있는 영화는 최신 정보로 갱신한다.
     */
    @Transactional
    public ImportResponse importPopularMovies() {
        List<TmdbMovie> fetched = tmdbClient.fetchPopularMovies();

        int savedCount = 0;
        int updatedCount = 0;
        for (TmdbMovie tmdbMovie : fetched) {
            String genres = TmdbGenres.toNames(tmdbMovie.getGenre_ids());
            Optional<Movie> existing = movieRepository.findByTmdbId(tmdbMovie.getId());

            if (existing.isPresent()) {
                // 변경 감지(dirty checking): 트랜잭션이 끝날 때 JPA가 바뀐 값을 UPDATE한다
                existing.get().updateDetails(tmdbMovie.getTitle(), tmdbMovie.getPoster_path(),
                        tmdbMovie.getOverview(), genres, tmdbMovie.getRelease_date(), tmdbMovie.getVote_average());
                updatedCount++;
            } else {
                movieRepository.save(new Movie(tmdbMovie.getId(), tmdbMovie.getTitle(), tmdbMovie.getPoster_path(),
                        tmdbMovie.getOverview(), genres, tmdbMovie.getRelease_date(), tmdbMovie.getVote_average()));
                savedCount++;
            }
        }
        return new ImportResponse(fetched.size(), savedCount, updatedCount);
    }

    public List<MovieResponse> getMovies(String title) {
        List<Movie> movies = (title == null || title.isBlank())
                ? movieRepository.findAll()
                : movieRepository.findByTitleContaining(title);
        return movies.stream().map(MovieResponse::from).toList();
    }

    public MovieResponse getMovie(Long id) {
        return movieRepository.findById(id)
                .map(MovieResponse::from)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }
}
