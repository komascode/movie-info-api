package com.map.movie.ai;

import com.map.movie.ai.MovieSearchTools.MovieSummary;

import java.util.List;
import java.util.stream.Collectors;

/**
 * "후보 먼저 넘기기" 방식의 프롬프트.
 * 서버가 DB에서 후보 영화를 조회해 질문과 함께 넘기고, LLM은 그 안에서 고르기만 한다.
 * LUXIA와 Gemini가 똑같은 입력을 받도록 한곳에 모아 둔다.
 */
public final class CandidatePrompt {

    public static final String SYSTEM_PROMPT = """
            너는 영화 추천 도우미다.
            - 반드시 [후보 영화] 목록 안에서만 추천한다. 목록에 없는 영화는 절대 지어내지 않는다.
            - 사용자의 요청에 맞는 영화를 1~3편 고르고, 각 영화마다 왜 요청에 맞는지 한두 문장으로 설명한다.
            - 맞는 영화가 없으면 없다고 솔직하게 말하고 가장 가까운 영화를 안내한다.
            - 한국어로 답한다.
            """;

    private CandidatePrompt() {
    }

    public static String userMessage(List<MovieSummary> candidates, String message) {
        return "[후보 영화]\n" + toLines(candidates) + "\n\n[사용자 요청]\n" + message;
    }

    private static String toLines(List<MovieSummary> movies) {
        return movies.stream()
                .map(m -> "- %s | 장르: %s | 평점: %s | 개봉: %s | 줄거리: %s"
                        .formatted(m.title(), m.genres(), m.voteAverage(), m.releaseDate(), m.overview()))
                .collect(Collectors.joining("\n"));
    }
}
