package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.AutoAuditConfigDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.SysConfigMapper;
import com.twilight.campus.pojo.SysConfig;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.AutoAuditService;
import com.twilight.campus.utils.UserContext;
import com.twilight.campus.vo.AutoAuditConfigVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class AutoAuditServiceImpl implements AutoAuditService {

    private static final String AUTO_AUDIT_KEY = "AUTO_AUDIT_ENABLED";
    private static final String AUTO_AUDIT_DESCRIPTION = "内容自动审核开关";
    private static final List<String> SUSPICIOUS_KEYWORDS = Arrays.asList(
            "诈骗", "刷单", "贷款", "博彩", "赌博", "裸聊", "色情",
            "违禁", "枪支", "毒品", "发票"
    );

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Override
    public AutoAuditConfigVO getConfig() {
        return new AutoAuditConfigVO(isEnabled(), canUpdate(UserContext.getUser()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(AutoAuditConfigDTO dto) {
        SysUser currentUser = UserContext.getUser();
        if (!canUpdate(currentUser)) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只有最高级管理员admin可以修改自动审核开关");
        }

        SysConfig config = new SysConfig();
        config.setConfigKey(AUTO_AUDIT_KEY);
        config.setConfigValue(Boolean.TRUE.equals(dto.getEnabled()) ? "true" : "false");
        config.setDescription(AUTO_AUDIT_DESCRIPTION);

        SysConfig oldConfig = sysConfigMapper.selectByKey(AUTO_AUDIT_KEY);
        if (oldConfig == null) {
            sysConfigMapper.insert(config);
        } else {
            sysConfigMapper.updateByKey(config);
        }
    }

    @Override
    public boolean isEnabled() {
        SysConfig config = sysConfigMapper.selectByKey(AUTO_AUDIT_KEY);
        if (config == null || config.getConfigValue() == null || config.getConfigValue().trim().isEmpty()) {
            return true;
        }
        return Boolean.parseBoolean(config.getConfigValue());
    }

    @Override
    public AuditDecision review(String title, String content) {
        String text = ((title == null ? "" : title) + " " + (content == null ? "" : content)).toLowerCase();
        for (String keyword : SUSPICIOUS_KEYWORDS) {
            if (text.contains(keyword.toLowerCase())) {
                return new AuditDecision(false, "自动审核命中可疑词：" + keyword);
            }
        }
        return new AuditDecision(true, "自动审核通过");
    }

    private boolean canUpdate(SysUser user) {
        return user != null
                && user.getRoleId() != null
                && user.getRoleId().equals(1L)
                && "admin".equals(user.getUsername());
    }
}
