package com.bmgf.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank(message = "房间名称不能为空")
        @Size(min = 2, max = 20, message = "房间名称长度需在2-20个字符之间")
        String name,

        @NotBlank(message = "必须选择房间类别")
        String category,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度需在6-20个字符之间")
        String password
) {

    // 可选：自定义构建器方法
    public static CreateRoomRequest of(String name, String category, String password) {
        return new CreateRoomRequest(name, category, password);
    }
}
