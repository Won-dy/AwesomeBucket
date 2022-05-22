package dyveloper.awesomebucket.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BadRequestURIException extends RuntimeException {
    // URI가 올바르지 않음 (요청 파라미터, 파라미터 값 등)
    public BadRequestURIException(String message) {
        super(message);
    }
}
