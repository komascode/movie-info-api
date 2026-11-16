package com.map.movie.ai;

import com.map.movie.exception.AiQuotaExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LLM 호출은 요청마다 비용이 들기 때문에 사용자(IP)별 하루 호출 수를 제한한다.
 * 서버가 한 대라 메모리에 기록한다. 서버가 여러 대가 되면 Redis 같은 공용 저장소로 옮겨야 한다.
 */
@Component
public class AiUsageLimiter {

    private final int dailyLimit;
    private final Clock clock;
    private final Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private LocalDate countDate;

    // 생성자가 두 개라서, Spring이 빈을 만들 때 어떤 생성자를 쓸지 @Autowired로 지정한다
    @Autowired
    public AiUsageLimiter(@Value("${ai.daily-limit}") int dailyLimit) {
        this(dailyLimit, Clock.systemDefaultZone());
    }

    // 테스트에서 날짜를 바꿔 볼 수 있도록 시계를 주입받는 생성자
    AiUsageLimiter(int dailyLimit, Clock clock) {
        this.dailyLimit = dailyLimit;
        this.clock = clock;
        this.countDate = LocalDate.now(clock);
    }

    /**
     * 호출 1회를 기록한다. 오늘 한도를 넘으면 예외를 던진다.
     *
     * @return 오늘 남은 호출 수
     */
    public synchronized int acquire(String clientKey) {
        LocalDate today = LocalDate.now(clock);
        if (!today.equals(countDate)) {
            // 날짜가 바뀌면 모든 사용자의 카운트를 초기화
            counts.clear();
            countDate = today;
        }

        int used = counts.computeIfAbsent(clientKey, k -> new AtomicInteger()).incrementAndGet();
        if (used > dailyLimit) {
            throw new AiQuotaExceededException(dailyLimit);
        }
        return dailyLimit - used;
    }
}
