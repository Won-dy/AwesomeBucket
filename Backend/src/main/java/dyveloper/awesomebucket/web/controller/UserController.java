package dyveloper.awesomebucket.web.controller;

import dyveloper.awesomebucket.domain.user.UserService;
import dyveloper.awesomebucket.exception.DuplicateResourceException;
import dyveloper.awesomebucket.exception.NotFoundResourceException;
import dyveloper.awesomebucket.web.dto.ErrorResultDto;
import dyveloper.awesomebucket.web.dto.ResultDto;
import dyveloper.awesomebucket.web.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     */
    @PostMapping("/users")
    public ResponseEntity join(@RequestBody UserDto.JoinRequestDto joinRequestDto) {
        try {
            userService.join(joinRequestDto.getEmail(), joinRequestDto.getPassword(), joinRequestDto.getName());
        } catch (DuplicateResourceException e) {
            return new ResponseEntity<>(new ErrorResultDto(400, "Email Already Exists", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ResultDto(200, "Signup Success"), HttpStatus.OK);
    }

    /**
     * 비밀번호 재설정 API
     */
    @PatchMapping("/users/pw/edit")
    public ResponseEntity changePassword(@RequestBody UserDto.LoginRequestDto loginRequestDto) {
        try {
            userService.changePassword(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        } catch (NotFoundResourceException nfre) {
            return new ResponseEntity<>(new ErrorResultDto(404, "Not Found Email", nfre.getMessage()), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new ResultDto(200, "Change Password Success"), HttpStatus.OK);
    }
}
