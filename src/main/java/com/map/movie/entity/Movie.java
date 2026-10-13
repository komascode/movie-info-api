package com.map.movie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TMDB의 영화 ID. 같은 영화가 중복 저장되지 않도록 유니크 제약을 건다
    @Column(unique = true)
    private Long tmdbId;

    private String title;

    private String posterPath;

    public Movie(Long tmdbId, String title, String posterPath) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.posterPath = posterPath;
    }
}
