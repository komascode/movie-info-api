package com.map.movie.dto;

/**
 * @param fetchedCount TMDB에서 가져온 영화 수
 * @param savedCount   새로 저장한 영화 수
 * @param updatedCount 이미 있어서 정보를 갱신한 영화 수
 */
public record ImportResponse(int fetchedCount, int savedCount, int updatedCount) {
}
