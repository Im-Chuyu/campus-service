package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改密码DTO
 */
@Data
public class UserUpdatePasswordDTO {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d_!@#$%^&*()\\-+=]{8,20}$",
            message = "密码必须包含字母和数字，且长度为8-20位"
    )
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
