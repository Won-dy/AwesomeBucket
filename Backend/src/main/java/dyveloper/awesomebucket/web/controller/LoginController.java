package dyveloper.awesomebucket.web.controller;

import dyveloper.awesomebucket.domain.login.LoginService;
import dyveloper.awesomebucket.domain.user.User;
import dyveloper.awesomebucket.domain.user.UserService;
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
    public ResponseEntity<ResultDto> login(@RequestBody UserDto.LoginRequestDto userLoginDto) {

        ResultDto resultDto;
        int result = loginService.login(userLoginDto.getEmail(), userLoginDto.getPassword());

        if (result == 1) {  // ID 틀림
            resultDto = new ResultDto(404, "Do not exist ID");
            return new ResponseEntity<>(resultDto, HttpStatus.NOT_FOUND);
        } else if (result == 2) {  // PW 틀림
            resultDto = new ResultDto(404, "Do not exist Password");
            return new ResponseEntity<>(resultDto, HttpStatus.NOT_FOUND);
        } else {  // 로그인 성공
            // 회원 ID를 data로 넘겨줌
            User loginUser = userService.findByEmail(userLoginDto.getEmail());
            Long id = loginUser.getId();
            UserDto.LoginResponseDto data = new UserDto.LoginResponseDto(id);

            resultDto = new ResultDto(200, "Login Success", data);
            return new ResponseEntity<>(resultDto, HttpStatus.OK);
        }
    }

}
