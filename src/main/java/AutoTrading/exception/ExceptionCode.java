package AutoTrading.exception;

import lombok.Getter;

@Getter
public enum ExceptionCode {
    NOT_EXIST_UUID(404, "UUID가 존재하지 않습니다.");

    private final int status;
    private final String message;

    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
