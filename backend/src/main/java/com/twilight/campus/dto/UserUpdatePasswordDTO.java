package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdatePasswordDTO {

    @NotBlank(message = "接收方式不能为空")
    @Pattern(regexp = "^(phone|email)$", message = "接收方式不正确")
    private String channel;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d_!@#$%^&*()\\-+=]{8,20}$",
            message = "密码必须包含字母和数字，长度8-20位"
    )
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
