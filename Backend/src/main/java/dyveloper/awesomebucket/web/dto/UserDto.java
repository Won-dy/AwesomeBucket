package dyveloper.awesomebucket.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public class UserDto {

    /**
     * 로그인 DTO
     */
    @Getter
    public static class LoginRequestDto {
        private String email;  // 이메일
        private String password;  // 비밀번호
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class LoginResponseDto {
        private long id;  // 회원 ID
    }

}
