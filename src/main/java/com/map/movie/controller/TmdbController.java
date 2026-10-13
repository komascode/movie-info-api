package com.map.movie.controller;

import com.map.movie.dto.ImportResponse;
import com.map.movie.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "TMDB", description = "TMDB 영화 가져오기")
@RestController
@RequiredArgsConstructor
public class TmdbController {

    private final MovieService movieService;

    @Operation(summary = "인기 영화 가져오기", description = "TMDB 인기 영화를 가져와 저장합니다. 이미 저장된 영화는 건너뜁니다.")
    @PostMapping("/tmdb/save")
    public ImportResponse saveMovies() {
        return movieService.importPopularMovies();
    }
}
