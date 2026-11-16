package com.map.movie.client;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TMDB는 장르를 숫자 ID로만 준다. 고정된 TMDB 영화 장르 목록으로 한글 이름을 붙인다.
 */
public final class TmdbGenres {

    private static final Map<Integer, String> NAMES = Map.ofEntries(
            Map.entry(28, "액션"), Map.entry(12, "모험"), Map.entry(16, "애니메이션"),
            Map.entry(35, "코미디"), Map.entry(80, "범죄"), Map.entry(99, "다큐멘터리"),
            Map.entry(18, "드라마"), Map.entry(10751, "가족"), Map.entry(14, "판타지"),
            Map.entry(36, "역사"), Map.entry(27, "공포"), Map.entry(10402, "음악"),
            Map.entry(9648, "미스터리"), Map.entry(10749, "로맨스"), Map.entry(878, "SF"),
            Map.entry(10770, "TV 영화"), Map.entry(53, "스릴러"), Map.entry(10752, "전쟁"),
            Map.entry(37, "서부")
    );

    private TmdbGenres() {
    }

    /** [18, 10749] → "드라마,로맨스" (모르는 ID는 무시) */
    public static String toNames(List<Integer> ids) {
        if (ids == null) {
            return "";
        }
        return ids.stream()
                .map(NAMES::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
    }
}
