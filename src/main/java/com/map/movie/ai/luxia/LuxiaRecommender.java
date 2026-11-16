package com.map.movie.ai.luxia;

import com.map.movie.ai.MovieRecommender;
import com.map.movie.ai.MovieSearchTools;
import com.map.movie.ai.MovieSearchTools.MovieSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LUXIA로 추천한다.
 * LUXIA Chat API는 도구 호출(Function Calling)을 지원하지 않으므로,
 * 서버가 DB에서 후보 영화를 먼저 조회해 질문과 함께 넘기고 LLM은 그 안에서 고르기만 한다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "luxia")
public class LuxiaRecommender implements MovieRecommender {

    static final String SYSTEM_PROMPT = """
            너는 영화 추천 도우미다.
            - 반드시 [후보 영화] 목록 안에서만 추천한다. 목록에 없는 영화는 절대 지어내지 않는다.
            - 사용자의 요청에 맞는 영화를 1~3편 고르고, 각 영화마다 왜 요청에 맞는지 한두 문장으로 설명한다.
            - 맞는 영화가 없으면 없다고 솔직하게 말하고 가장 가까운 영화를 안내한다.
            - 한국어로 답한다.
            """;

    private final LuxiaClient luxiaClient;
    private final MovieSearchTools movieSearchTools;

    @Override
    public RecommendResult recommend(String message) {
        // 1) 서버가 후보를 먼저 조회 (평점순 상위, 줄거리는 짧게 잘라 토큰 절약)
        List<MovieSummary> candidates = movieSearchTools.searchMovies(null, null);

        // 2) 후보 목록을 질문과 함께 넘긴다
        String userMessage = "[후보 영화]\n" + toLines(candidates) + "\n\n[사용자 요청]\n" + message;
        LuxiaClient.ChatResponse response = luxiaClient.chat(SYSTEM_PROMPT, userMessage);

        LuxiaClient.Usage usage = response.usage();
        return new RecommendResult(
                response.text(),
                response.model(),
                usage == null ? null : usage.prompt_tokens(),
                usage == null ? null : usage.completion_tokens());
    }

    private static String toLines(List<MovieSummary> movies) {
        return movies.stream()
                .map(m -> "- %s | 장르: %s | 평점: %s | 개봉: %s | 줄거리: %s"
                        .formatted(m.title(), m.genres(), m.voteAverage(), m.releaseDate(), m.overview()))
                .collect(Collectors.joining("\n"));
    }
}
