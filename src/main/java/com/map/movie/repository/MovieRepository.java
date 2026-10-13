package com.map.movie.repository;

import com.map.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTmdbId(Long tmdbId);

    List<Movie> findByTitleContaining(String title);
}
