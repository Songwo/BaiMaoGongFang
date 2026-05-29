package com.bmgf.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "聊天室未找到")
public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String roomId) {
        super("找不到ID为 " + roomId + " 的聊天室");
    }
}
