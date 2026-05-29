package com.bmgf.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "用户未找到")
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userId) {
        super("找不到ID为 " + userId + " 的用户");
    }
}
