package com.twilight.campus.service;

import com.twilight.campus.dto.UserLoginDTO;
import com.twilight.campus.dto.UserRegisterDTO;
import com.twilight.campus.dto.UserUpdateDTO;
import com.twilight.campus.dto.UserUpdatePasswordDTO;
import com.twilight.campus.pojo.SysUser;

public interface UserService {

    void register(UserRegisterDTO dto);

    String login(UserLoginDTO dto);

    void updatePassword(UserUpdatePasswordDTO dto);

    SysUser getUserInfo(Long userId);

    void updateUser(UserUpdateDTO user);
}
