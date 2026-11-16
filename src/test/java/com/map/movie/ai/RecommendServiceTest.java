package com.map.movie.ai;

import com.map.movie.ai.MovieRecommender.RecommendResult;
import com.map.movie.dto.RecommendResponse;
import com.map.movie.exception.AiQuotaExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RecommendServiceTest {

    // 어떤 LLM이든 상관없이 "추천 담당자" 역할만 가짜로 끼운다
    @Mock
    private MovieRecommender movieRecommender;

    @Mock
    private AiUsageLimiter usageLimiter;

    @InjectMocks
    private RecommendService recommendService;

    @Test
    @DisplayName("사용량을 확인한 뒤 추천 결과와 남은 횟수를 응답한다")
    void recommend() {
        given(usageLimiter.acquire("1.1.1.1")).willReturn(19);
        given(movieRecommender.recommend("액션 영화")).willReturn(
                new RecommendResult("고스트 솔저를 추천합니다.", "test-model", 100, 20));

        RecommendResponse response = recommendService.recommend("액션 영화", "1.1.1.1");

        assertThat(response.answer()).isEqualTo("고스트 솔저를 추천합니다.");
        assertThat(response.model()).isEqualTo("test-model");
        assertThat(response.remainingToday()).isEqualTo(19);
    }

    @Test
    @DisplayName("사용 한도를 넘으면 LLM을 호출하지 않는다 (비용 발생 없음)")
    void doesNotCallLlmWhenQuotaExceeded() {
        given(usageLimiter.acquire("1.1.1.1")).willThrow(new AiQuotaExceededException(20));

        assertThatThrownBy(() -> recommendService.recommend("액션 영화", "1.1.1.1"))
                .isInstanceOf(AiQuotaExceededException.class);
        then(movieRecommender).should(never()).recommend(any());
    }
}
