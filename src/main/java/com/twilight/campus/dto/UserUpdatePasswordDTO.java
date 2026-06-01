package com.twilight.campus.dto;

import lombok.Data;

/**
 * 修改密码DTO
 */
@Data
public class UserUpdatePasswordDTO {
    private Long userId;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
