package com.twilight.campus.service;

public interface VerificationCodeService {

    String sendCode(String scene, String channel, String target);

    void verifyAndConsume(String scene, String channel, String target, String code);
}
