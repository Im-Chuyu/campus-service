package com.twilight.campus.utils;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.pojo.SysUser;

public class AuthUtil {

    /**
     * 判断是否已登录
     */
    public static SysUser getLoginUser() {
        SysUser user = UserContext.getUser();
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }
        return user;
    }

    /**
     * 判断是否管理员
     */
    public static void checkAdmin() {
        SysUser user = getLoginUser();
        if (user.getRoleId() == null || !user.getRoleId().equals(1L)) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "无权限操作");
        }
    }

    /**
     * 判断是否超级管理员
     */
    public static void checkSuperAdmin() {
        SysUser user = getLoginUser();
        if (user.getRoleId() == null || !user.getRoleId().equals(1L) || !"admin".equals(user.getUsername())) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只有超级管理员admin可以操作");
        }
    }

    public static boolean isSuperAdmin(SysUser user) {
        return user != null && user.getRoleId() != null && user.getRoleId().equals(1L) && "admin".equals(user.getUsername());
    }
}
