package com.example.awesomebucket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class UserDto {

    /**
     * 로그인 DTO
     */
    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequestDto {
        private String email;  // 이메일
        private String password;  // 비밀번호
    }

    @Getter
    @ToString
    public static class LoginResponseDto {
        private long id;  // 회원 ID
    }

    /**
     * 이메일 인증 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class EmailAuthRequestDto {
        private String type;  // 종류 [회원가입, 비밀번호 찾기]
        private String email;  // 이메일
    }

    @Getter
    public static class EmailAuthResponseDto {
        private String authenticationCode;  // 인증 번호
    }

}
