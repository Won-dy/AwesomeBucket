package dyveloper.awesomebucket.web.controller;

import dyveloper.awesomebucket.domain.login.LoginService;
import dyveloper.awesomebucket.domain.user.User;
import dyveloper.awesomebucket.domain.user.UserService;
import dyveloper.awesomebucket.web.dto.ErrorResultDto;
import dyveloper.awesomebucket.web.dto.ResultDto;
import dyveloper.awesomebucket.web.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final UserService userService;

    @PostMapping("login")
    public ResponseEntity login(@RequestBody UserDto.LoginRequestDto userLoginDto) {

        int result = loginService.login(userLoginDto.getEmail(), userLoginDto.getPassword());

        if (result == 1) {  // ID 틀림
            return new ResponseEntity<>(new ErrorResultDto(404, "Do not exist ID", "아이디가 존재하지 않습니다"), HttpStatus.NOT_FOUND);
        } else if (result == 2) {  // PW 틀림
            return new ResponseEntity<>(new ErrorResultDto(404, "Do not exist Password", "비밀번호가 틀립니다"), HttpStatus.NOT_FOUND);
        } else {  // 로그인 성공
            // 회원 ID를 data로 넘겨줌
            User loginUser = userService.findByEmail(userLoginDto.getEmail());
            Long id = loginUser.getId();
            UserDto.LoginResponseDto data = new UserDto.LoginResponseDto(id);

            return new ResponseEntity<>(new ResultDto(200, "Login Success", data), HttpStatus.OK);
        }
    }

}
