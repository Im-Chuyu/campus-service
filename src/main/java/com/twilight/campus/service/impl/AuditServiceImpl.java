package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.AuditHandleDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.AuditRecordMapper;
import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.pojo.AuditRecord;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.AuditService;
import com.twilight.campus.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核业务实现类
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    @Override
    public List<Content> waitAuditList() {
        checkAdmin();
        return contentMapper.selectWaitAuditList();
    }

    @Override
    public void handleAudit(AuditHandleDTO dto) {
        checkAdmin();

        // 1. 查内容是否存在
        Content content = contentMapper.selectById(dto.getContentId());
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        // 2. 只能审核待审核内容
        if (!Integer.valueOf(0).equals(content.getStatus())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "该内容已审核过，不能重复审核");
        }

        // 3. 校验审核结果
        if (!Integer.valueOf(1).equals(dto.getAuditResult()) && !Integer.valueOf(2).equals(dto.getAuditResult())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "审核结果参数错误");
        }

        // 4. 更新内容状态
        Content updateContent = new Content();
        updateContent.setId(content.getId());
        updateContent.setStatus(dto.getAuditResult());
        updateContent.setAuditReason(dto.getAuditReason());
        contentMapper.updateAuditStatus(updateContent);

        // 5. 保存审核记录
        SysUser currentUser = UserContext.getUser();

        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setContentId(content.getId());
        auditRecord.setAuditorId(currentUser.getId());
        auditRecord.setAuditResult(dto.getAuditResult());
        auditRecord.setAuditReason(dto.getAuditReason());

        auditRecordMapper.insert(auditRecord);
    }

    @Override
    public List<AuditRecord> listByContentId(Long contentId) {
        checkAdmin();
        return auditRecordMapper.selectByContentId(contentId);
    }

    /**
     * 校验当前用户是否为管理员
     */
    private void checkAdmin() {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        // 这里默认 roleId = 1 是管理员，你可以按自己数据库实际情况调整
        if (currentUser.getRoleId() == null || !currentUser.getRoleId().equals(1L)) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "无权限操作");
        }
    }
}
