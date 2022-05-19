package com.example.awesomebucket.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ErrorResultDto<T> {

    private int status;  // 상태 코드
    private String error;  // 에러 이유
    private String message;  // 에러 메시지
    private T data;  // 데이터
}
