package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20位之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_.&-]{3,19}$", message = "用户名必须以字母开头，只能包含字母、数字、下划线、短横线、点号和&")
    private String username;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 16, message = "昵称长度不能超过16个字符")
    private String nickname;
    @Size(max = 50, message = "个性签名不能超过50个字符")
    private String signature;
    private String phone;
    private Integer phoneVisible;
    private String email;
    private Integer emailVisible;
    private String wechat;
    private Integer wechatVisible;
    private String qq;
    private Integer qqVisible;
    private Integer privateChatEnabled;
    private Integer gender;
    private String avatar;
    private String profileBackground;
}
