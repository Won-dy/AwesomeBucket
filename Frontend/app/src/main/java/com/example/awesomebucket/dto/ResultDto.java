package com.example.awesomebucket.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResultDto<T> {

    private int status;  // 상태 코드
    private String message;  // 메시지
    private T data;  // 데이터

}
