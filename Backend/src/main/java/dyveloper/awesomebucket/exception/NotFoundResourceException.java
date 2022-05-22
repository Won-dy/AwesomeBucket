package dyveloper.awesomebucket.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotFoundResourceException extends RuntimeException {
    // 요청 리소스가 서버에 없음
    public NotFoundResourceException(String message) {
        super(message);
    }
}
