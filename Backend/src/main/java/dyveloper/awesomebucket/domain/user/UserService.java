package dyveloper.awesomebucket.domain.user;

import dyveloper.awesomebucket.exception.DuplicateResourceException;
import dyveloper.awesomebucket.exception.NotFoundResourceException;
import dyveloper.awesomebucket.exception.UnauthorizedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 이메일로 회원 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // 로그인한 User 조회 - 없을 시 인증되지 않은 접근
    public User findLoginUserById(Long id) throws UnauthorizedAccessException {
        return userRepository.findById(id).orElseThrow(UnauthorizedAccessException::new);
    }

    // 회원가입
    public void join(String email, String password, String name) {
        if (findByEmail(email) != null) {
            throw new DuplicateResourceException("이미 가입된 이메일입니다");
        }
        User user = User.builder().email(email).password(password).name(name).build();
        userRepository.save(user);
    }

    // 가입 여부 확인
    public void isJoined(String email, String type) {
        User user = findByEmail(email);

        if (type.equals("join") && user != null) {  // 이미 존재하는 이메일로 회원가입 시도
            throw new DuplicateResourceException("이미 가입된 이메일입니다");
        }

        if (type.equals("findPw") && user == null) {  // 존재하지 않는 이메일로 비밀번호 변경 시도
            throw new NotFoundResourceException("가입된 이메일이 없습니다");
        }
    }

}
