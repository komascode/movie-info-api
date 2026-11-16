package com.map.movie.ai;

/**
 * 자연어 요청을 받아 영화를 추천하는 역할.
 * 어떤 LLM으로 추천할지는 구현체가 정하고, 설정(ai.provider)에 따라 하나가 주입된다.
 * - GeminiRecommender: LLM이 검색 도구를 직접 호출 (Function Calling)
 * - LuxiaRecommender: 서버가 후보를 먼저 조회해 LLM에 넘김 (도구 호출 미지원 모델용)
 */
public interface MovieRecommender {

    RecommendResult recommend(String message);

    /**
     * @param answer           추천 문장
     * @param model            실제로 답한 모델 이름
     * @param promptTokens     입력 토큰 수
     * @param completionTokens 출력 토큰 수
     */
    record RecommendResult(String answer, String model, Integer promptTokens, Integer completionTokens) {
    }
}
