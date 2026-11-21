package com.map.movie.ai;

import com.map.movie.ai.MovieSearchTools.MovieSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CandidatePromptTest {

    @Test
    @DisplayName("후보 영화 목록과 사용자 요청을 하나의 메시지로 만든다 (LUXIA·Gemini 공통 입력)")
    void buildsUserMessageWithCandidates() {
        List<MovieSummary> candidates = List.of(
                new MovieSummary(1L, "코코", "애니메이션,가족", "2017-10-27", 8.2, "음악을 사랑하는 소년"));

        String message = CandidatePrompt.userMessage(candidates, "가족이랑 볼 영화");

        assertThat(message)
                .startsWith("[후보 영화]\n- 코코 | 장르: 애니메이션,가족 | 평점: 8.2")
                .endsWith("[사용자 요청]\n가족이랑 볼 영화");
    }
}
