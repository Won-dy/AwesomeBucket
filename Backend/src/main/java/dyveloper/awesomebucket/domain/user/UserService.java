package dyveloper.awesomebucket.domain.user;

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

}
