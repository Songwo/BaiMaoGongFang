package com.bmgf.util;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,30}$",
            message = "用户名需为3-30位中文、字母、数字或下划线")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "密码需至少8位，包含字母和数字")
    private String password;
    @Email(regexp = "^(?=.{1,64}@)[a-zA-Z0-9_+&*-]+(\\.[a-zA-Z0-9_+&*-]+)*@"
            + "[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,})$",
            message = "邮箱格式不合法")
    private String email;
}