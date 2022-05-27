package dyveloper.awesomebucket.exception;

public class DuplicateResourceException extends RuntimeException {
    // 이미 존재하는 리소스
    public DuplicateResourceException(String message) {
        super(message);
    }
}
