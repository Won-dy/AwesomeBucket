package dyveloper.awesomebucket.domain.user;

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

}
