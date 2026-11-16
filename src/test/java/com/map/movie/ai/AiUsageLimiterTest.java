package com.map.movie.ai;

import com.map.movie.exception.AiQuotaExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiUsageLimiterTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("한도 안에서는 남은 횟수를 돌려주고, 한도를 넘으면 예외가 발생한다")
    void blocksAfterLimit() {
        AiUsageLimiter limiter = new AiUsageLimiter(2, fixedClock("2026-11-15T10:00:00Z"));

        assertThat(limiter.acquire("1.1.1.1")).isEqualTo(1);
        assertThat(limiter.acquire("1.1.1.1")).isZero();
        assertThatThrownBy(() -> limiter.acquire("1.1.1.1"))
                .isInstanceOf(AiQuotaExceededException.class);
    }

    @Test
    @DisplayName("사용자(IP)마다 따로 센다")
    void countsPerClient() {
        AiUsageLimiter limiter = new AiUsageLimiter(1, fixedClock("2026-11-15T10:00:00Z"));

        limiter.acquire("1.1.1.1");

        // 다른 IP는 영향을 받지 않는다
        assertThat(limiter.acquire("2.2.2.2")).isZero();
    }

    @Test
    @DisplayName("날짜가 바뀌면 횟수가 초기화된다")
    void resetsNextDay() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-15T10:00:00Z"));
        AiUsageLimiter limiter = new AiUsageLimiter(1, clock);

        limiter.acquire("1.1.1.1");
        assertThatThrownBy(() -> limiter.acquire("1.1.1.1"))
                .isInstanceOf(AiQuotaExceededException.class);

        clock.now = Instant.parse("2026-11-16T10:00:00Z"); // 다음 날

        assertThat(limiter.acquire("1.1.1.1")).isZero();
    }

    private static Clock fixedClock(String instant) {
        return Clock.fixed(Instant.parse(instant), SEOUL);
    }

    // 테스트 중에 시간을 바꿀 수 있는 가짜 시계
    private static class MutableClock extends Clock {
        Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        @Override
        public ZoneId getZone() {
            return SEOUL;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
