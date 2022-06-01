package dyveloper.awesomebucket.domain.login;

import dyveloper.awesomebucket.exception.BadRequestURIException;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@AllArgsConstructor
public class EmailSendService {

    private final JavaMailSenderImpl javaMailSender;

    // 이메일 인증을 위한 인증번호 전송
    public String sendCode(String email, String type) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        // 인증번호 생성
        for (int i = 0; i < 3; i++) {  // A~Z 랜덤 3자리 알파벳 생성
            int index = random.nextInt(25) + 65;
            code.append((char) index);
        }
        int numIndex = random.nextInt(8999) + 1000;  // 4자리 정수 생성
        code.append(numIndex);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        if (type.equals("join")) {
            message.setSubject("[AwesomeBucket] 회원가입 인증번호 안내");
        } else if (type.equals("findPw")) {
            message.setSubject("[AwesomeBucket] 비밀번호 찾기 인증번호 안내");
        } else {
            throw new BadRequestURIException("Use 'join' or 'findPw' for 'type'");
        }
        message.setText("아래 인증번호를 인증번호 기입란에 입력하시기 바랍니다.\n\n인증 번호 : " + code);
        javaMailSender.send(message);

        return code.toString();
    }
}
