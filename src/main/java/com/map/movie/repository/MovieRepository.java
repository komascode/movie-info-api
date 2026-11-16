package com.map.movie.repository;

import com.map.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByTmdbId(Long tmdbId);

    List<Movie> findByTitleContaining(String title);

    // genres 칸("드라마,로맨스")에 해당 장르 이름이 들어간 영화
    List<Movie> findByGenresContaining(String genre);
}
