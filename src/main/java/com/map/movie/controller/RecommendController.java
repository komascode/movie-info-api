package com.map.movie.controller;

import com.map.movie.ai.RecommendService;
import com.map.movie.dto.RecommendRequest;
import com.map.movie.dto.RecommendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI", description = "AI 영화 추천")
@RestController
@RequiredArgsConstructor
public class RecommendController {

    private static final int MAX_MESSAGE_LENGTH = 300;

    private final RecommendService recommendService;

    @Operation(summary = "AI 영화 추천",
            description = "자연어로 원하는 영화를 말하면, AI가 저장된 영화 중에서 골라 추천합니다. IP당 하루 사용 횟수가 제한됩니다.")
    @PostMapping("/recommend")
    public RecommendResponse recommend(@RequestBody RecommendRequest request, HttpServletRequest httpRequest) {
        String message = request.message();
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message를 입력해 주세요.");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            // 긴 입력은 토큰(=비용)을 많이 쓰므로 길이를 제한한다
            throw new IllegalArgumentException("message는 " + MAX_MESSAGE_LENGTH + "자 이하로 입력해 주세요.");
        }
        return recommendService.recommend(message, httpRequest.getRemoteAddr());
    }
}
