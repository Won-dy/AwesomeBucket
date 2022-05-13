package dyveloper.awesomebucket.domain.login;

import dyveloper.awesomebucket.domain.user.User;
import dyveloper.awesomebucket.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserService userService;

    public int login(String email, String password) {
        User findUser = userService.findByEmail(email);
        if (findUser == null) {  // ID가 틀림
            return 1;
        } else {
            if (findUser.getPassword().equals(password)) {  // 로그인 성공
                return 0;
            } else {  // PW가 틀림
                return 2;
            }
        }
    }
}
