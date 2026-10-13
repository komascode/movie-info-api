package com.map.movie.controller;

import com.map.movie.dto.TmdbMovie;
import com.map.movie.dto.TmdbResponse;
import com.map.movie.entity.Movie;
import com.map.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
public class TmdbController {

    private final MovieRepository movieRepository;

    @Value("${tmdb.api-key}")
    private String apiKey;

    @GetMapping("/tmdb/save")
    public String saveMovies() {

        String url =
                "https://api.themoviedb.org/3/movie/popular?language=ko-KR&page=1&api_key="
                        + apiKey;

        RestTemplate restTemplate = new RestTemplate();

        TmdbResponse response =
                restTemplate.getForObject(url, TmdbResponse.class);

        for (TmdbMovie tmdbMovie : response.getResults()) {

            Movie movie = new Movie();

            movie.setTitle(tmdbMovie.getTitle());

            movie.setPosterPath(tmdbMovie.getPoster_path());

            movieRepository.save(movie);
        }

        return "저장 완료";
    }
}