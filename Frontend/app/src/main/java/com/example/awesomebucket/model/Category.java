package com.example.awesomebucket.model;

import com.example.awesomebucket.model.user.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private Long id;  // 카테고리 ID

    private String name;  // 카테고리명
    private boolean isDefault;  // 디폴트 여부

    private User user;  // 회원

}
