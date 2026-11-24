package com.map.movie.ai.luxia;

import com.map.movie.ai.MovieRecommender.RecommendResult;
import com.map.movie.ai.CandidateRetriever;
import com.map.movie.ai.MovieSearchTools.MovieSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LuxiaRecommenderTest {

    @Mock
    private LuxiaClient luxiaClient;

    @Mock
    private CandidateRetriever candidateRetriever;

    @InjectMocks
    private LuxiaRecommender luxiaRecommender;

    @Test
    @DisplayName("질문으로 검색한 후보 영화를 사용자 요청과 함께 LUXIA에 넘기고, 응답을 결과로 바꾼다")
    void sendsCandidatesWithUserMessage() {
        // given: DB 후보 2편, LUXIA는 정해진 답을 준다
        given(candidateRetriever.retrieve("가족이랑 볼 영화")).willReturn(List.of(
                new MovieSummary(1L, "코코", "애니메이션,가족", "2017-10-27", 8.2, "음악을 사랑하는 소년"),
                new MovieSummary(2L, "블랙 볼", "드라마,전쟁", "2026-01-01", 8.0, "세 남성의 삶")
        ));
        given(luxiaClient.chat(anyString(), anyString())).willReturn(new LuxiaClient.ChatResponse(
                "luxia3-llm-8b",
                List.of(new LuxiaClient.Choice(new LuxiaClient.Message("assistant", "코코를 추천합니다."))),
                new LuxiaClient.Usage(500, 40)));

        // when
        RecommendResult result = luxiaRecommender.recommend("가족이랑 볼 영화");

        // then: 넘긴 메시지에 후보와 요청이 모두 들어 있어야 한다
        ArgumentCaptor<String> userMessage = ArgumentCaptor.forClass(String.class);
        then(luxiaClient).should().chat(eq(LuxiaRecommender.SYSTEM_PROMPT), userMessage.capture());
        assertThat(userMessage.getValue())
                .contains("[후보 영화]", "코코", "블랙 볼", "[사용자 요청]", "가족이랑 볼 영화");

        assertThat(result.answer()).isEqualTo("코코를 추천합니다.");
        assertThat(result.model()).isEqualTo("luxia3-llm-8b");
        assertThat(result.promptTokens()).isEqualTo(500);
        assertThat(result.completionTokens()).isEqualTo(40);
    }
}
