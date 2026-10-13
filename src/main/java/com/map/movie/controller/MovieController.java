package com.map.movie.controller;

import com.map.movie.dto.MovieResponse;
import com.map.movie.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Movie", description = "저장된 영화 조회")
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @Operation(summary = "영화 목록 조회", description = "title을 주면 제목에 해당 단어가 들어간 영화만 조회합니다.")
    @GetMapping
    public List<MovieResponse> getMovies(@RequestParam(required = false) String title) {
        return movieService.getMovies(title);
    }

    @Operation(summary = "영화 상세 조회", description = "없는 id면 404를 반환합니다.")
    @GetMapping("/{id}")
    public MovieResponse getMovie(@PathVariable Long id) {
        return movieService.getMovie(id);
    }
}
