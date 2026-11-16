package com.map.movie.dto;

/**
 * @param answer           AI의 추천 문장
 * @param latencyMs        AI 응답까지 걸린 시간(ms)
 * @param promptTokens     입력 토큰 수
 * @param completionTokens 출력 토큰 수
 * @param remainingToday   오늘 남은 호출 수
 */
public record RecommendResponse(String answer, long latencyMs,
                                Integer promptTokens, Integer completionTokens, int remainingToday) {
}
