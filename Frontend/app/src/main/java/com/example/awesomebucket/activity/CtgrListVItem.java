package com.example.awesomebucket.activity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CtgrListVItem {  // 각 아이템에 출력될 데이터를 위한 클래스 정의

    // 클래스 멤버 변수 정의
    private long id;  // 카테고리 ID
    private boolean isDefault;  // 디폴트 여부
    private String name;
}
