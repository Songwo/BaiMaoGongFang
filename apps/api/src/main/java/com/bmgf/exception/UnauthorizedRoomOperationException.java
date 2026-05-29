package com.bmgf.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "未授权操作")
public class UnauthorizedRoomOperationException extends RuntimeException {
    public UnauthorizedRoomOperationException(String operation) {
        super("无权执行该操作: " + operation);
    }
}
