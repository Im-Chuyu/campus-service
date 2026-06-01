package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.UserLoginDTO;
import com.twilight.campus.dto.UserRegisterDTO;
import com.twilight.campus.dto.UserUpdatePasswordDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.UserMapper;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.UserService;
import com.twilight.campus.utils.JwtUtil;
import com.twilight.campus.utils.PasswordUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void register(UserRegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "两次密码不一致");
        }

        SysUser existUser = userMapper.selectByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setGender(0);
        user.setRoleId(2L);
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public String login(UserLoginDTO dto) {
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 账号禁用判断
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 密码校验
        if (!PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "密码错误");
        }

        // 这里建议 token 里带 userId、username、roleId
        return JwtUtil.createToken(user.getId(), user.getUsername(), String.valueOf(user.getRoleId()));
    }


    @Override
    public void updatePassword(UserUpdatePasswordDTO dto) {
        SysUser user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }

        if (!PasswordUtil.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "原密码错误");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "两次新密码不一致");
        }

        user.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        userMapper.updatePassword(user);
    }

    @Override
    public SysUser getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    @Override
    public void updateUser(SysUser user) {
        SysUser dbUser = userMapper.selectById(user.getId());
        if (dbUser == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }
        userMapper.updateUser(user);
    }
}
