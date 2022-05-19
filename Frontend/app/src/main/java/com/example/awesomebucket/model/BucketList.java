package com.example.awesomebucket.model;

import com.example.awesomebucket.model.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BucketList {

    private Long id;  // 버킷리스트 ID

    private String title;  // 버킷리스트명
    private String memo;  // 메모

    private int importance;  // 중요도
    private int achievementRate;  // 달성률

    private LocalDate targetDate;  // 목표일
    private LocalDate achievementDate;  // 달성일

    private boolean isDeleted;  // 삭제 여부

    private User user;  // 회원

    private Category category;  // 카테고리

    private LocalDateTime registeredDate;  // 등록일시
    private LocalDateTime lastModifiedDate;  // 최종 수정일시

}
