package com.twilight.campus.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改密码DTO
 */
@Data
public class UserUpdatePasswordDTO {

    @NotNull(message = "用户id不能为空")
    private Long userId;
    @NotNull(message = "原密码不能为空")
    private String oldPassword;
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d_!@#$%^&*()\\-+=]{8,20}$", message = "密码必须包含字母和数字，且长度为8-20位")
    private String newPassword;
    @NotNull(message = "确认密码为空")
    private String confirmPassword;
}
