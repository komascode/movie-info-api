package com.map.movie.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TmdbMovie {

    private Long id;

    private String title;

    private String poster_path;
}
