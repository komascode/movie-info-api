package com.map.movie.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TMDB 응답의 영화 한 편. 필드 이름은 TMDB JSON 키와 같아야 자동으로 채워진다.
 */
@Getter
@Setter
public class TmdbMovie {

    private Long id;

    private String title;

    private String poster_path;

    private String overview;

    private String release_date;

    private Double vote_average;

    private List<Integer> genre_ids;
}
