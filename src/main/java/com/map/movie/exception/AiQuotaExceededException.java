package com.map.movie.exception;

public class AiQuotaExceededException extends RuntimeException {

    public AiQuotaExceededException(int dailyLimit) {
        super("오늘 AI 추천 사용 한도(" + dailyLimit + "회)를 모두 사용했습니다. 내일 다시 시도해 주세요.");
    }
}
