package com.map.movie.ai;

import com.map.movie.dto.RecommendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecommendService {

    private static final String SYSTEM_PROMPT = """
            너는 영화 추천 도우미다.
            - 추천은 반드시 searchMovies 도구로 조회한 영화 안에서만 한다. 목록에 없는 영화는 절대 지어내지 않는다.
            - 사용자의 요청에서 장르나 평점 조건을 파악해 도구를 호출한다. 조건이 애매하면 조건 없이 조회한 뒤 줄거리를 보고 고른다.
            - 1~3편을 추천하고, 각 영화마다 왜 요청에 맞는지 한두 문장으로 설명한다.
            - 한국어로 답한다.
            """;

    private final ChatClient chatClient;
    private final AiUsageLimiter usageLimiter;

    // ChatClient.Builder는 Spring AI가 설정(application.properties)을 보고 만들어 주입한다.
    // 어떤 회사 모델을 쓰는지는 여기서 몰라도 된다 → 설정만 바꾸면 모델 교체 가능
    public RecommendService(ChatClient.Builder chatClientBuilder,
                            MovieSearchTools movieSearchTools,
                            AiUsageLimiter usageLimiter) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(movieSearchTools)
                .build();
        this.usageLimiter = usageLimiter;
    }

    public RecommendResponse recommend(String message, String clientKey) {
        int remaining = usageLimiter.acquire(clientKey);

        long start = System.currentTimeMillis();
        ChatResponse response = chatClient.prompt()
                .user(message)
                .call()
                .chatResponse();
        long latencyMs = System.currentTimeMillis() - start;

        String answer = response.getResult().getOutput().getText();
        Usage usage = response.getMetadata().getUsage();
        Integer promptTokens = usage.getPromptTokens();
        Integer completionTokens = usage.getCompletionTokens();

        // 실행 로그: 응답 지연과 토큰 사용량(=비용)을 남긴다
        log.info("[AI recommend] client={} latency={}ms promptTokens={} completionTokens={} remaining={}",
                clientKey, latencyMs, promptTokens, completionTokens, remaining);

        return new RecommendResponse(answer, latencyMs, promptTokens, completionTokens, remaining);
    }
}
