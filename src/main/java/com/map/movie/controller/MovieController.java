package com.map.movie.controller;

import com.map.movie.entity.Movie;
import com.map.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
public class MovieController {

    private final MovieRepository movieRepository;

    @GetMapping("/movies")
    public List<Movie> getMovies() {
        return movieRepository.findAll();
    }
}