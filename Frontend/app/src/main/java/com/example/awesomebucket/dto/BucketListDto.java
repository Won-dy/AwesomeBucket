package com.example.awesomebucket.dto;

import lombok.Getter;
import lombok.ToString;

public class BucketListDto {

    /**
     * 버킷 리스트 목록 조회 DTO
     */
    @ToString
    @Getter
    public static class FindResponseDto {
        private Long id;  // 버킷리스트 ID
        private String title;  // 버킷리스트명
        private int importance;  // 중요도
        private int achievementRate;  // 달성률
        private String targetDate;  // 목표일
        private String categoryName;  // 카테고리명
    }
}
