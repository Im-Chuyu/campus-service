package com.twilight.campus.dto;

import lombok.Data;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String confirmPassword;
    private String nickname;
    private String phone;
    private String email;
}
