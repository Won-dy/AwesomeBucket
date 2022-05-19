package dyveloper.awesomebucket.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

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
    }

}
