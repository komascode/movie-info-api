package com.map.movie.service;

import com.map.movie.client.TmdbClient;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final TmdbClient tmdbClient;

    /**
     * TMDB 인기 영화 목록을 가져와 저장한다. 이미 저장된 영화(tmdbId 기준)는 건너뛴다.
     */
    @Transactional
    public ImportResponse importPopularMovies() {
        List<TmdbMovie> fetched = tmdbClient.fetchPopularMovies();

        int savedCount = 0;
        for (TmdbMovie tmdbMovie : fetched) {
            if (movieRepository.existsByTmdbId(tmdbMovie.getId())) {
                continue;
            }
            movieRepository.save(new Movie(tmdbMovie.getId(), tmdbMovie.getTitle(), tmdbMovie.getPoster_path()));
            savedCount++;
        }
        return new ImportResponse(fetched.size(), savedCount);
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
