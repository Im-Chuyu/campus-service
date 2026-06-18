package com.twilight.campus.service.impl;

import com.twilight.campus.constant.RedisKeyConstant;
import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.service.OptionalRedisService;
import com.twilight.campus.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration REGISTER_CODE_COOLDOWN = Duration.ofSeconds(60);
    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, CodeRecord> localCodes = new ConcurrentHashMap<>();
    private final Map<String, Instant> localCooldowns = new ConcurrentHashMap<>();

    @Autowired
    private OptionalRedisService redisService;

    @Override
    public String sendCode(String scene, String channel, String target) {
        String normalizedTarget = normalizeTarget(target);
        assertSendAllowed(scene, channel, normalizedTarget);
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        String key = codeKey(scene, channel, normalizedTarget);
        if (redisService.enabled()) {
            redisService.set(key, code, CODE_TTL);
        } else {
            localCodes.put(key, new CodeRecord(code, Instant.now().plus(CODE_TTL)));
        }

        // 目前项目没有短信/邮件服务配置，开发阶段先返回并记录验证码；接入真实服务时替换这里的发送逻辑即可。
        log.info("Verification code generated, scene={}, channel={}, target={}, code={}", scene, channel, normalizedTarget, code);
        return code;
    }

    @Override
    public void verifyAndConsume(String scene, String channel, String target, String code) {
        String normalizedTarget = normalizeTarget(target);
        String key = codeKey(scene, channel, normalizedTarget);
        String savedCode = redisService.enabled() ? redisService.get(key) : getLocalCode(key);
        if (savedCode == null || savedCode.isEmpty()) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "验证码已过期，请重新获取");
        }
        if (code == null || !savedCode.equals(code.trim())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "验证码不正确");
        }

        if (redisService.enabled()) {
            redisService.delete(key);
        } else {
            localCodes.remove(key);
        }
    }

    private String getLocalCode(String key) {
        CodeRecord record = localCodes.get(key);
        if (record == null) {
            return null;
        }
        if (record.expireAt().isBefore(Instant.now())) {
            localCodes.remove(key);
            return null;
        }
        return record.code();
    }

    private String codeKey(String scene, String channel, String target) {
        return RedisKeyConstant.VERIFICATION_CODE_PREFIX + scene + ":" + channel + ":" + target;
    }

    private String cooldownKey(String scene, String channel, String target) {
        return RedisKeyConstant.VERIFICATION_COOLDOWN_PREFIX + scene + ":" + channel + ":" + target;
    }

    private void assertSendAllowed(String scene, String channel, String target) {
        if (!"register".equals(scene)) {
            return;
        }
        String key = cooldownKey(scene, channel, target);
        if (redisService.enabled()) {
            if (redisService.get(key) != null) {
                throw new BusinessException(ResultCodeConstant.TOO_MANY_REQUESTS, "验证码发送过于频繁，请60秒后再试");
            }
            redisService.set(key, "1", REGISTER_CODE_COOLDOWN);
            return;
        }

        Instant expireAt = localCooldowns.get(key);
        Instant now = Instant.now();
        if (expireAt != null && expireAt.isAfter(now)) {
            long seconds = Math.max(1, Duration.between(now, expireAt).toSeconds());
            throw new BusinessException(ResultCodeConstant.TOO_MANY_REQUESTS, "验证码发送过于频繁，请" + seconds + "秒后再试");
        }
        localCooldowns.put(key, now.plus(REGISTER_CODE_COOLDOWN));
    }

    private String normalizeTarget(String target) {
        if (target == null) {
            return "";
        }
        return target.trim().toLowerCase();
    }

    private record CodeRecord(String code, Instant expireAt) {
    }
}
