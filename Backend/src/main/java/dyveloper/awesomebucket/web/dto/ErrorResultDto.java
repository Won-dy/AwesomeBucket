package dyveloper.awesomebucket.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResultDto<T> {

    private int status;  // 상태 코드
    private String error;  // 이유
    private String message;  // 메시지
    private T data;  // 데이터

    public ErrorResultDto(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
