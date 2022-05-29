package dyveloper.awesomebucket.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BucketListDto {

    /**
     * 버킷리스트 목록 조회 DTO
     */
    @ToString
    @Getter
    @AllArgsConstructor
    public static class FindResponseDto {  // 버킷리스트 목록 조회
        private Long id;  // 버킷리스트 ID
        private String title;  // 버킷리스트명
        private int importance;  // 중요도
        private int achievementRate;  // 달성률
        private LocalDate targetDate;  // 목표일
        private String categoryName;  // 카테고리명
    }

    /**
     * 버킷리스트 상세 조회 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class FindDetailResponseDto {
        private Long id;  // 버킷리스트 ID
        private String title;  // 버킷리스트명
        private LocalDateTime registeredDate;  // 등록일시
        private int importance;  // 중요도
        private int achievementRate;  // 달성률
        private LocalDate achievementDate;  // 달성일
        private LocalDate targetDate;  // 목표일
        private String memo;  // 메모
        private String categoryName;  // 카테고리명
    }

    /**
     * 버킷리스트 등록, 수정 DTO
     */
    @Getter
    public static class CreateUpdateRequestDto {
        private Long userId;  // 회원 ID
        private String categoryName;  // 카테고리명

        private String title;  // 버킷리스트명
        private String memo;  // 메모
        private int importance;  // 중요도
        private int achievementRate;  // 달성률
        private LocalDate targetDate;  // 목표일
        private LocalDate achievementDate;  // 달성일
    }

}
