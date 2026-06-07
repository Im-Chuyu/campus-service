package com.twilight.campus.service.impl;

import com.twilight.campus.constant.JwtClaimsConstant;
import com.twilight.campus.constant.RedisKeyConstant;
import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.UserLoginDTO;
import com.twilight.campus.dto.UserRegisterDTO;
import com.twilight.campus.dto.UserUpdateDTO;
import com.twilight.campus.dto.UserUpdatePasswordDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.UserMapper;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.UserService;
import com.twilight.campus.utils.JwtUtil;
import com.twilight.campus.utils.PasswordUtil;
import com.twilight.campus.utils.RedisUtil;
import com.twilight.campus.utils.UserContext;
import com.twilight.campus.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    //用户注册
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

        // 生成token
        String token = JwtUtil.createToken(
                user.getId(),
                user.getUsername(),
                String.valueOf(user.getRoleId())
        );

        // 存入Redis，覆盖旧token，安装了redis后取消注释即可
        //String key = RedisKeyConstant.LOGIN_TOKEN_PREFIX + user.getId();
        //RedisUtil.set(key, token, JwtClaimsConstant.JWT_TTL, TimeUnit.MILLISECONDS);

        return token;
    }



    //修改密码
    @Override
    public void updatePassword(UserUpdatePasswordDTO dto) {
        // 1. 获取当前登录用户
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        // 2. 判断两次新密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "两次新密码不一致");
        }

        // 3. 判断新密码不能和旧密码一样
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "新密码不能和旧密码相同");
        }

        // 4. 校验旧密码是否正确
        if (!PasswordUtil.matches(dto.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "原密码错误");
        }

        // 5. 加密新密码
        String encodedPassword = PasswordUtil.encode(dto.getNewPassword());

        // 6. 更新数据库
        SysUser updateUser = new SysUser();
        updateUser.setId(currentUser.getId());
        updateUser.setPassword(encodedPassword);

        userMapper.updatePassword(updateUser);
    }


    //查询用户
    @Override
    public SysUser getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    //更新用户
    @Override
    public void updateUser(UserUpdateDTO dto) {
        // 1. 获取当前登录用户
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        // 2. 查数据库中的用户
        SysUser user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }

        // 3. 更新允许修改的字段
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }

        // 4. 更新数据库
        userMapper.updateUser(user);
    }

    @Override
    public List<SysUser> list(String keyword, Integer status, Long roleId) {
        AuthUtil.checkAdmin();
        return userMapper.selectList(keyword, status, roleId);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        AuthUtil.checkAdmin();
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "用户状态不正确");
        }

        SysUser currentUser = AuthUtil.getLoginUser();
        if (currentUser.getId().equals(id)) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "不能修改自己的账号状态");
        }

        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }

        userMapper.updateStatus(id, status);
    }

}
