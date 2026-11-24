package com.map.movie.ai.luxia;

import com.map.movie.ai.CandidatePrompt;
import com.map.movie.ai.CandidateRetriever;
import com.map.movie.ai.MovieRecommender;
import com.map.movie.ai.MovieSearchTools.MovieSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LUXIA로 추천한다.
 * LUXIA Chat API는 도구 호출(Function Calling)을 지원하지 않으므로,
 * 서버가 DB에서 후보 영화를 먼저 조회해 질문과 함께 넘기고 LLM은 그 안에서 고르기만 한다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "luxia")
public class LuxiaRecommender implements MovieRecommender {

    static final String SYSTEM_PROMPT = CandidatePrompt.SYSTEM_PROMPT;

    private final LuxiaClient luxiaClient;
    private final CandidateRetriever candidateRetriever;

    @Override
    public RecommendResult recommend(String message) {
        // 1) 질문의 장르·평점 조건으로 DB에서 후보를 찾는다 (줄거리는 짧게 잘라 토큰 절약)
        List<MovieSummary> candidates = candidateRetriever.retrieve(message);

        // 2) 후보 목록을 질문과 함께 넘긴다
        String userMessage = CandidatePrompt.userMessage(candidates, message);
        LuxiaClient.ChatResponse response = luxiaClient.chat(SYSTEM_PROMPT, userMessage);

        LuxiaClient.Usage usage = response.usage();
        return new RecommendResult(
                response.text(),
                response.model(),
                usage == null ? null : usage.prompt_tokens(),
                usage == null ? null : usage.completion_tokens());
    }
}
