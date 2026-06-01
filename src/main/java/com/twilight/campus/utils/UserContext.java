package com.twilight.campus.utils;

import com.twilight.campus.pojo.SysUser;

/**
 * 当前登录用户上下文
 */
public class UserContext {

    private static final ThreadLocal<SysUser> THREAD_LOCAL = new ThreadLocal<>();

    public static void setUser(SysUser user) {
        THREAD_LOCAL.set(user);
    }

    public static SysUser getUser() {
        return THREAD_LOCAL.get();
    }

    public static void removeUser() {
        THREAD_LOCAL.remove();
    }
}
