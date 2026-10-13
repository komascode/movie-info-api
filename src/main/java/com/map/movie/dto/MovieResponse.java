package com.map.movie.dto;

import com.map.movie.entity.Movie;

public record MovieResponse(Long id, Long tmdbId, String title, String posterPath) {

    public static MovieResponse from(Movie movie) {
        return new MovieResponse(movie.getId(), movie.getTmdbId(), movie.getTitle(), movie.getPosterPath());
    }
}
