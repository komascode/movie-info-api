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

    // 줄거리는 길어서 기본 VARCHAR(255)를 넘을 수 있으므로 TEXT로 저장한다
    @Column(columnDefinition = "TEXT")
    private String overview;

    // "드라마,로맨스"처럼 쉼표로 이어 붙인 장르 이름
    private String genres;

    // TMDB가 주는 "2026-03-01" 형식 그대로 저장 (빈 값이 올 수 있어 문자열로 둔다)
    private String releaseDate;

    // TMDB 평점 (0~10)
    private Double voteAverage;

    public Movie(Long tmdbId, String title, String posterPath,
                 String overview, String genres, String releaseDate, Double voteAverage) {
        this.tmdbId = tmdbId;
        updateDetails(title, posterPath, overview, genres, releaseDate, voteAverage);
    }

    /**
     * 이미 저장된 영화의 정보를 최신 TMDB 값으로 바꾼다.
     * 트랜잭션 안에서 호출하면 save()를 따로 부르지 않아도 JPA가 변경을 감지해 UPDATE한다.
     */
    public void updateDetails(String title, String posterPath,
                              String overview, String genres, String releaseDate, Double voteAverage) {
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.genres = genres;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
    }
}
