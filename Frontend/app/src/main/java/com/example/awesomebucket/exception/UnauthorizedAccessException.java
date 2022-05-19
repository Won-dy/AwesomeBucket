package com.example.awesomebucket.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UnauthorizedAccessException extends RuntimeException {
    // 인증되지 않은 사용자가 접근할 때 발생하는 예외
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
