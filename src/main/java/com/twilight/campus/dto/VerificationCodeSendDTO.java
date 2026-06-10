package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerificationCodeSendDTO {

    @NotBlank(message = "验证码用途不能为空")
    @Pattern(regexp = "^(register|reset_password|change_password)$", message = "验证码用途不正确")
    private String scene;

    @NotBlank(message = "接收方式不能为空")
    @Pattern(regexp = "^(phone|email)$", message = "接收方式不正确")
    private String channel;

    @NotBlank(message = "接收账号不能为空")
    private String target;
}
