package com.map.movie.ai;

import com.map.movie.ai.MovieSearchTools.MovieSummary;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Gemini를 "후보 먼저 넘기기" 방식으로 호출한다 (도구 호출 없음).
 * LUXIA와 같은 호출 방식으로 맞춰, 응답 시간 차이가 모델 때문인지 호출 방식 때문인지 나눠 측정하기 위한 구현체.
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini-prefetch")
public class GeminiPrefetchRecommender implements MovieRecommender {

    private final ChatClient chatClient;
    private final MovieSearchTools movieSearchTools;

    public GeminiPrefetchRecommender(ChatClient.Builder chatClientBuilder, MovieSearchTools movieSearchTools) {
        // 도구를 등록하지 않는다: LLM과의 왕복은 1번뿐
        this.chatClient = chatClientBuilder
                .defaultSystem(CandidatePrompt.SYSTEM_PROMPT)
                .build();
        this.movieSearchTools = movieSearchTools;
    }

    @Override
    public RecommendResult recommend(String message) {
        List<MovieSummary> candidates = movieSearchTools.searchMovies(null, null);

        ChatResponse response = chatClient.prompt()
                .user(CandidatePrompt.userMessage(candidates, message))
                .call()
                .chatResponse();

        Usage usage = response.getMetadata().getUsage();
        return new RecommendResult(
                response.getResult().getOutput().getText(),
                response.getMetadata().getModel(),
                usage.getPromptTokens(),
                usage.getCompletionTokens());
    }
}
