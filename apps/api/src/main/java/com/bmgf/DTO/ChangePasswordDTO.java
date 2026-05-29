package com.bmgf.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordDTO {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String id;
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String currentPassword;
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String newPassword;
}
