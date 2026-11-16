package com.map.movie.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Gemini(Spring AI)로 추천한다. LLM이 searchMovies 도구를 스스로 호출해 DB에서 후보를 찾는다.
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiRecommender implements MovieRecommender {

    private static final String SYSTEM_PROMPT = """
            너는 영화 추천 도우미다.
            - 추천은 반드시 searchMovies 도구로 조회한 영화 안에서만 한다. 목록에 없는 영화는 절대 지어내지 않는다.
            - 사용자의 요청에서 장르나 평점 조건을 파악해 도구를 호출한다. 조건이 애매하면 조건 없이 조회한 뒤 줄거리를 보고 고른다.
            - 1~3편을 추천하고, 각 영화마다 왜 요청에 맞는지 한두 문장으로 설명한다.
            - 한국어로 답한다.
            """;

    private final ChatClient chatClient;

    // ChatClient.Builder는 Spring AI가 설정을 보고 만들어 주입한다
    public GeminiRecommender(ChatClient.Builder chatClientBuilder, MovieSearchTools movieSearchTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(movieSearchTools)
                .build();
    }

    @Override
    public RecommendResult recommend(String message) {
        ChatResponse response = chatClient.prompt()
                .user(message)
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
