package com.bmgf.DTO;

import jakarta.validation.constraints.NotBlank;

public record JoinRequest(
        @NotBlank(message = "房间ID不能为空")
        String roomId,

        @NotBlank(message = "密码不能为空")
        String password
) {}