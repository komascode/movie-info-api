package com.map.movie.service;

import com.map.movie.dto.ImportResponse;
import com.map.movie.dto.MovieResponse;
import com.map.movie.dto.TmdbMovie;
import com.map.movie.dto.TmdbResponse;
import com.map.movie.entity.Movie;
import com.map.movie.exception.MovieNotFoundException;
import com.map.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private static final String TMDB_POPULAR_URL =
            "https://api.themoviedb.org/3/movie/popular?language=ko-KR&page=1&api_key={apiKey}";

    private final MovieRepository movieRepository;
    private final RestClient restClient = RestClient.create();

    @Value("${tmdb.api-key}")
    private String apiKey;

    /**
     * TMDB 인기 영화 목록을 가져와 저장한다. 이미 저장된 영화(tmdbId 기준)는 건너뛴다.
     */
    @Transactional
    public ImportResponse importPopularMovies() {
        TmdbResponse response = restClient.get()
                .uri(TMDB_POPULAR_URL, apiKey)
                .retrieve()
                .body(TmdbResponse.class);

        List<TmdbMovie> fetched = (response == null || response.getResults() == null)
                ? List.of()
                : response.getResults();

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
