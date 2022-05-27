package com.example.awesomebucket.dto;

import lombok.Getter;
import lombok.ToString;

public class CategoryDto {

    /**
     * 카테고리 목록 조회 DTO
     */
    @ToString
    @Getter
    public static class FindResponseDto {
        private long id;  // 카테고리 ID
        private String name;  // 카테고리명
        private boolean isDefault;  // 디폴트 여부
    }

}
