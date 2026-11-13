package com.map.movie.client;

import com.map.movie.dto.TmdbMovie;
import com.map.movie.dto.TmdbResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * TMDB(외부 API)와의 통신만 담당한다.
 * 서비스는 이 클래스를 주입받아 사용하므로, 테스트에서는 가짜(Mock)로 바꿔 끼울 수 있다.
 */
@Component
public class TmdbClient {

    private static final String POPULAR_URL =
            "https://api.themoviedb.org/3/movie/popular?language=ko-KR&page=1&api_key={apiKey}";

    private final RestClient restClient = RestClient.create();
    private final String apiKey;

    public TmdbClient(@Value("${tmdb.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * TMDB 인기 영화 목록을 가져온다. 응답이 비어 있으면 빈 목록을 돌려준다.
     */
    public List<TmdbMovie> fetchPopularMovies() {
        TmdbResponse response = restClient.get()
                .uri(POPULAR_URL, apiKey)
                .retrieve()
                .body(TmdbResponse.class);

        if (response == null || response.getResults() == null) {
            return List.of();
        }
        return response.getResults();
    }
}
