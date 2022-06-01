package com.example.awesomebucket;

public class MyConstant {

    public static final String URL = "http://192.168.55.182:8080/";
    public static final String PREFERENCE_FILE_USER = "LoginUserInfo";
    public static final long NO_ID = -2147483648;  // 값이 없을 때 설정할 기본 ID 값

    // 정규표현식
    public static final String emailPattern = "^(.+)@(.+)$";  // 이메일 패턴 정규식
    public static final String pwPattern = "^((?=.*\\d)(?=.*[a-zA-Z]).{8,20})$";  // 비밀번호 패턴(영문, 숫자를 포함한 8~20자) 정규식
    public static final String blankPattern = "(\\s)";  // 공백 문자 정규식

}
