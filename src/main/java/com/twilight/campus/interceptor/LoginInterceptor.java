package com.twilight.campus.interceptor;

import com.twilight.campus.constant.JwtClaimsConstant;
import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.UserMapper;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.utils.JwtUtil;
import com.twilight.campus.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        try {
            Claims claims = JwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());

            // 关键：根据 userId 查完整用户
            SysUser user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "用户不存在或已被删除");
            }

            // 账号禁用判断
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new BusinessException(ResultCodeConstant.FORBIDDEN, "账号已被禁用");
            }

            UserContext.setUser(user);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "登录已过期，请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.removeUser();
    }
}
