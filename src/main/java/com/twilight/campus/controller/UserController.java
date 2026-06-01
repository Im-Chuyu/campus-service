package com.twilight.campus.controller;

import com.twilight.campus.dto.UserLoginDTO;
import com.twilight.campus.dto.UserRegisterDTO;
import com.twilight.campus.dto.UserUpdatePasswordDTO;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody @Valid UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginDTO dto) {
        String token = userService.login(dto);
        return Result.success("登录成功", token);
    }

    @PostMapping("/updatePassword")
    public Result<?> updatePassword(@RequestBody @Valid UserUpdatePasswordDTO dto) {
        userService.updatePassword(dto);
        return Result.success("修改密码成功", null);
    }

    @GetMapping("/info/{userId}")
    public Result<SysUser> getUserInfo(@PathVariable Long userId) {
        SysUser user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<?> updateUser(@RequestBody SysUser user) {
        userService.updateUser(user);
        return Result.success("修改成功", null);
    }
}
