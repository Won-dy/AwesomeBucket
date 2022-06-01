package com.example.awesomebucket.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;  // 회원 ID

    private String email;  // 이메일
    private String password;  // 비밀번호

    private String name;  // 이름

}
