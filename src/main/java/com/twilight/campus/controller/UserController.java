package com.twilight.campus.controller;

import com.twilight.campus.constant.RedisKeyConstant;
import com.twilight.campus.dto.UserLoginDTO;
import com.twilight.campus.dto.UserRegisterDTO;
import com.twilight.campus.dto.UserUpdateDTO;
import com.twilight.campus.dto.UserUpdatePasswordDTO;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.UserService;
import com.twilight.campus.utils.RedisUtil;
import com.twilight.campus.utils.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    //注册用户
    @PostMapping("/register")
    public Result<?> register(@RequestBody @Valid UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", null);
    }

    //登录
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginDTO dto) {
        String token = userService.login(dto);
        return Result.success("登录成功", token);
    }

    //更新密码
    @PostMapping("/updatePassword")
    public Result<?> updatePassword(@RequestBody @Valid UserUpdatePasswordDTO dto) {
        userService.updatePassword(dto);
        return Result.success("修改密码成功", null);
    }

    //查询用户
    @GetMapping("/info/{userId}")
    public Result<SysUser> getUserInfo(@PathVariable Long userId) {
        SysUser user = userService.getUserInfo(userId);
        return Result.success(user);
    }
    @GetMapping("/info")
    public Result<SysUser> getMyInfo() {
        SysUser user = userService.getUserInfo(UserContext.getUser().getId());
        return Result.success(user);
    }


    //更新用户信息
    @PutMapping("/update")
    public Result<?> updateUser(@RequestBody UserUpdateDTO user) {
        userService.updateUser(user);
        return Result.success("修改成功", null);
    }

    //退出登录
//    @PostMapping("/logout")
//    public Result<?> logout() {
//        SysUser currentUser = UserContext.getUser();
//        if (currentUser != null) {
//            RedisUtil.delete(RedisKeyConstant.LOGIN_TOKEN_PREFIX + currentUser.getId());
//        }
//        return Result.success("退出成功", null);
//    }
//
}
