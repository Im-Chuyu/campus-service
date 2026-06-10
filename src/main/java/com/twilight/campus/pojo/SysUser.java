package com.twilight.campus.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUser {
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String nickname;
    private String signature;
    private String avatar;
    private String profileBackground;
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
    private Long roleId;
    private Integer status;
    private Integer usernameUpdateYear;
    private Integer usernameUpdateCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
