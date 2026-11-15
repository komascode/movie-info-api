package com.map.movie.dto;

import com.map.movie.entity.Movie;

public record MovieResponse(Long id, Long tmdbId, String title, String posterPath,
                            String overview, String genres, String releaseDate, Double voteAverage) {

    public static MovieResponse from(Movie movie) {
        return new MovieResponse(movie.getId(), movie.getTmdbId(), movie.getTitle(), movie.getPosterPath(),
                movie.getOverview(), movie.getGenres(), movie.getReleaseDate(), movie.getVoteAverage());
    }
}
