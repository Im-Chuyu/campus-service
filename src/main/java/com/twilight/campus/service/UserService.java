package com.twilight.campus.service;

import com.twilight.campus.dto.UserLoginDTO;
import com.twilight.campus.dto.UserRegisterDTO;
import com.twilight.campus.dto.UserUpdateDTO;
import com.twilight.campus.dto.UserUpdatePasswordDTO;
import com.twilight.campus.pojo.SysUser;

import java.util.List;

public interface UserService {

    void register(UserRegisterDTO dto);

    String login(UserLoginDTO dto);

    void updatePassword(UserUpdatePasswordDTO dto);

    SysUser getUserInfo(Long userId);

    void updateUser(UserUpdateDTO user);

    List<SysUser> list(String keyword, Integer status, Long roleId);

    void updateStatus(Long id, Integer status);
}
