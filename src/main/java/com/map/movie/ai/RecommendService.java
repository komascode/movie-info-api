package com.map.movie.ai;

import com.map.movie.ai.MovieRecommender.RecommendResult;
import com.map.movie.dto.RecommendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 추천 요청의 공통 처리: 사용량 제한 → 추천 → 실행 로그.
 * 실제 추천은 설정에 따라 주입된 MovieRecommender(Gemini 또는 LUXIA)가 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final MovieRecommender movieRecommender;
    private final AiUsageLimiter usageLimiter;

    public RecommendResponse recommend(String message, String clientKey) {
        int remaining = usageLimiter.acquire(clientKey);

        long start = System.currentTimeMillis();
        RecommendResult result = movieRecommender.recommend(message);
        long latencyMs = System.currentTimeMillis() - start;

        // 실행 로그: 어떤 모델이, 얼마나 걸려서, 토큰을 얼마나 썼는지(=비용) 남긴다
        log.info("[AI recommend] model={} client={} latency={}ms promptTokens={} completionTokens={} remaining={}",
                result.model(), clientKey, latencyMs, result.promptTokens(), result.completionTokens(), remaining);

        return new RecommendResponse(result.answer(), result.model(), latencyMs,
                result.promptTokens(), result.completionTokens(), remaining);
    }
}
