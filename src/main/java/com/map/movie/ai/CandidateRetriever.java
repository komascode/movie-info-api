package com.map.movie.ai;

import com.map.movie.ai.MovieSearchTools.MovieSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 질문에 맞는 후보 영화를 DB에서 찾는다 (RAG의 Retrieval 단계).
 * 질문에서 장르와 평점 조건을 뽑아 그 조건으로 DB를 검색하고,
 * 찾은 후보만 LLM에 넘겨 그 안에서 고르게 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CandidateRetriever {

    static final double HIGH_RATING = 7.0;
    private static final int MAX_CANDIDATES = 15;

    // 질문에 나올 수 있는 표현 → DB에 저장된 장르 이름
    private static final Map<String, String> GENRE_WORDS = new LinkedHashMap<>();

    static {
        for (String genre : List.of("액션", "모험", "애니메이션", "코미디", "범죄", "다큐멘터리", "드라마", "가족",
                "판타지", "역사", "공포", "음악", "미스터리", "로맨스", "SF", "스릴러", "전쟁", "서부")) {
            GENRE_WORDS.put(genre, genre);
        }
        GENRE_WORDS.put("애니", "애니메이션");
        GENRE_WORDS.put("웃긴", "코미디");
        GENRE_WORDS.put("코믹", "코미디");
        GENRE_WORDS.put("다큐", "다큐멘터리");
        GENRE_WORDS.put("무서운", "공포");
        GENRE_WORDS.put("호러", "공포");
        GENRE_WORDS.put("로맨틱", "로맨스");
        GENRE_WORDS.put("공상과학", "SF");
        GENRE_WORDS.put("우주", "SF");
    }

    private static final Pattern RATING_NUMBER = Pattern.compile("평점\\s*(\\d+(?:\\.\\d+)?)");
    private static final List<String> HIGH_RATING_WORDS = List.of("평점 높", "평점이 높", "평점 좋", "평점이 좋", "고평점", "명작", "평이 좋");

    private final MovieSearchTools movieSearchTools;

    /** 질문에서 뽑은 검색 조건 */
    record Condition(List<String> genres, Double minRating) {
    }

    static Condition parse(String message) {
        String text = message == null ? "" : message;

        Set<String> genres = new LinkedHashSet<>();
        String upper = text.toUpperCase();
        GENRE_WORDS.forEach((word, genre) -> {
            if (upper.contains(word.toUpperCase())) {
                genres.add(genre);
            }
        });

        Double minRating = null;
        Matcher m = RATING_NUMBER.matcher(text);
        if (m.find()) {
            double value = Double.parseDouble(m.group(1));
            if (value <= 10) {
                minRating = value;
            }
        } else if (HIGH_RATING_WORDS.stream().anyMatch(text::contains)) {
            minRating = HIGH_RATING;
        }
        return new Condition(new ArrayList<>(genres), minRating);
    }

    public List<MovieSummary> retrieve(String message) {
        Condition c = parse(message);
        List<MovieSummary> result = search(c.genres(), c.minRating());

        // 조건이 너무 좁아 아무것도 없으면 평점 조건부터 풀고, 그래도 없으면 전체 상위로 대신한다
        if (result.isEmpty() && c.minRating() != null) {
            result = search(c.genres(), null);
        }
        if (result.isEmpty() && !c.genres().isEmpty()) {
            result = search(List.of(), null);
        }
        log.info("[retrieve] genres={}, minRating={}, candidates={}", c.genres(), c.minRating(), result.size());
        return result;
    }

    private List<MovieSummary> search(List<String> genres, Double minRating) {
        if (genres.isEmpty()) {
            return movieSearchTools.searchMovies(null, minRating);
        }
        // 장르가 여러 개면 장르별로 찾아 합친다 (중복 제거, 평점 높은 순)
        Map<Long, MovieSummary> merged = new LinkedHashMap<>();
        for (String genre : genres) {
            for (MovieSummary s : movieSearchTools.searchMovies(genre, minRating)) {
                merged.putIfAbsent(s.id(), s);
            }
        }
        return merged.values().stream()
                .sorted(Comparator.comparing(MovieSummary::voteAverage, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MAX_CANDIDATES)
                .toList();
    }
}
