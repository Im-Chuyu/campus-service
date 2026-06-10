package com.twilight.campus.service;

import com.twilight.campus.dto.UserLoginDTO;
import com.twilight.campus.dto.ResetPasswordDTO;
import com.twilight.campus.dto.UserRegisterDTO;
import com.twilight.campus.dto.UserUpdateDTO;
import com.twilight.campus.dto.UserUpdatePasswordDTO;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.vo.PageResultVO;

import java.util.List;

public interface UserService {

    void register(UserRegisterDTO dto);

    String login(UserLoginDTO dto);

    void updatePassword(UserUpdatePasswordDTO dto);

    void resetPassword(ResetPasswordDTO dto);

    void validateCodeTarget(String scene, String channel, String target);

    SysUser getUserInfo(Long userId);

    void updateUser(UserUpdateDTO user);

    List<SysUser> list(String keyword, Integer status, Long roleId);

    PageResultVO<SysUser> page(String keyword, Integer status, Long roleId, Integer page, Integer pageSize);

    void updateStatus(Long id, Integer status);

    void deleteById(Long id);
}
