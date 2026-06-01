package com.twilight.campus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String nickname;
    private String phone;
    private String email;
    private Integer gender;
    private String avatar;
}
