package com.twilight.campus.controller;

import com.twilight.campus.dto.UserLoginDTO;
import com.twilight.campus.dto.ResetPasswordDTO;
import com.twilight.campus.dto.UserRegisterDTO;
import com.twilight.campus.dto.UserUpdateDTO;
import com.twilight.campus.dto.UserUpdatePasswordDTO;
import com.twilight.campus.dto.VerificationCodeSendDTO;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.UserService;
import com.twilight.campus.service.VerificationCodeService;
import com.twilight.campus.utils.UserContext;
import com.twilight.campus.vo.PageResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @PostMapping("/sendCode")
    public Result<String> sendCode(@RequestBody @Valid VerificationCodeSendDTO dto) {
        userService.validateCodeTarget(dto.getScene(), dto.getChannel(), dto.getTarget());
        String code = verificationCodeService.sendCode(dto.getScene(), dto.getChannel(), dto.getTarget());
        return Result.success("验证码：" + code, code);
    }

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
        return Result.success("密码修改成功", null);
    }

    @PostMapping("/resetPassword")
    public Result<?> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return Result.success("密码重置成功", null);
    }

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

    @GetMapping("/list")
    public Result<List<SysUser>> list(String keyword, Integer status, Long roleId) {
        return Result.success(userService.list(keyword, status, roleId));
    }

    @GetMapping("/page")
    public Result<PageResultVO<SysUser>> page(String keyword,
                                              Integer status,
                                              Long roleId,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.page(keyword, status, roleId, page, pageSize));
    }

    @PutMapping("/status/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        userService.updateStatus(id, status);
        return Result.success(status == 1 ? "账号已启用" : "账号已禁用", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/update")
    public Result<?> updateUser(@RequestBody @Valid UserUpdateDTO user) {
        userService.updateUser(user);
        return Result.success("修改成功", null);
    }
}
