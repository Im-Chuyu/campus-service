package com.twilight.campus.interceptor;

import com.twilight.campus.constant.JwtClaimsConstant;
import com.twilight.campus.constant.RedisKeyConstant;
import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.UserMapper;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.OptionalRedisService;
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

    @Autowired
    private OptionalRedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 跨域预检请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (isPublicNoticeDetail(request)) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        try {
            // 1. 解析JWT
            Claims claims = JwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());

            if (redisService.enabled()) {
                validateCurrentLoginToken(userId, token, claims);
            }

            // 5. 根据 userId 查完整用户
            SysUser user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "用户不存在或已被删除");
            }

            // 6. 账号禁用判断
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new BusinessException(ResultCodeConstant.FORBIDDEN, "账号已被禁用");
            }

            // 7. 放入用户上下文
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

    private void validateCurrentLoginToken(Long userId, String token, Claims claims) {
        String deviceId = claims.get(JwtClaimsConstant.DEVICE_ID, String.class);
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "登录状态已过期，请重新登录");
        }

        String redisKey = RedisKeyConstant.LOGIN_TOKEN_PREFIX + userId + ":" + deviceId;
        String redisToken = redisService.get(redisKey);

        if (redisToken == null || redisToken.isEmpty()) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "登录状态已过期，请重新登录");
        }

        if (!redisToken.equals(token)) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "登录状态已更新，请重新登录");
        }
    }

    private boolean isPublicNoticeDetail(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path.matches("/notice/\\d+");
    }
}
