package com.map.movie.ai.luxia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 솔트룩스 LUXIA Cloud의 Chat API와의 통신만 담당한다.
 * Spring AI에 LUXIA 연동 모듈이 없어 RestClient로 직접 호출한다.
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "luxia")
public class LuxiaClient {

    private static final String CHAT_URL = "https://bridge.luxiacloud.com/luxia/v1/chat";

    private final RestClient restClient = RestClient.create();
    private final String apiKey;
    private final String model;

    public LuxiaClient(@Value("${luxia.api-key}") String apiKey,
                       @Value("${luxia.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public ChatResponse chat(String systemPrompt, String userMessage) {
        ChatRequest request = new ChatRequest(model, List.of(
                new Message("system", systemPrompt),
                new Message("user", userMessage)
        ), 0.3, 1024);

        return restClient.post()
                .uri(CHAT_URL)
                .header("apikey", apiKey)   // LUXIA는 Authorization 대신 apikey 헤더를 쓴다
                .body(request)
                .retrieve()
                .body(ChatResponse.class);
    }

    // 요청·응답 JSON과 같은 모양의 record (필드 이름이 JSON 키와 같아야 자동 변환된다)
    public record ChatRequest(String model, List<Message> messages, Double temperature, Integer max_completion_tokens) {
    }

    public record Message(String role, String content) {
    }

    public record ChatResponse(String model, List<Choice> choices, Usage usage) {

        public String text() {
            return (choices == null || choices.isEmpty()) ? "" : choices.get(0).message().content();
        }
    }

    public record Choice(Message message) {
    }

    public record Usage(Integer prompt_tokens, Integer completion_tokens) {
    }
}
