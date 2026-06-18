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
import com.twilight.campus.service.MessageService;
import com.twilight.campus.utils.PageUtil;
import com.twilight.campus.utils.UserContext;
import com.twilight.campus.vo.PageResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    @Autowired
    private MessageService messageService;

    @Override
    public List<Content> waitAuditList() {
        checkAdmin();
        return contentMapper.selectWaitAuditList();
    }

    @Override
    public PageResultVO<Content> waitAuditPage(Integer page, Integer pageSize) {
        checkAdmin();
        int safePage = PageUtil.safePage(page);
        int safePageSize = PageUtil.safePageSize(pageSize);
        Long total = contentMapper.countWaitAuditList();
        return PageResultVO.of(
            contentMapper.selectWaitAuditPage(PageUtil.offset(safePage, safePageSize), safePageSize),
            total == null ? 0L : total,
            safePage,
            safePageSize
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAudit(AuditHandleDTO dto) {
        checkAdmin();

        Content content = contentMapper.selectById(dto.getContentId());
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        if (!Integer.valueOf(0).equals(content.getStatus())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "该内容已审核过，不能重复审核");
        }

        if (!Integer.valueOf(1).equals(dto.getAuditResult()) && !Integer.valueOf(2).equals(dto.getAuditResult())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "审核结果参数错误");
        }

        String auditReason = dto.getAuditReason() == null ? null : dto.getAuditReason().trim();
        if (auditReason != null && auditReason.isEmpty()) {
            auditReason = null;
        }
        if (Integer.valueOf(2).equals(dto.getAuditResult()) && auditReason == null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "拒绝通过时，审核原因不能为空");
        }

        Content updateContent = new Content();
        updateContent.setId(content.getId());
        updateContent.setStatus(dto.getAuditResult());
        updateContent.setAuditReason(auditReason);
        contentMapper.updateAuditStatus(updateContent);

        SysUser currentUser = UserContext.getUser();
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setContentId(content.getId());
        auditRecord.setAuditorId(currentUser.getId());
        auditRecord.setAuditResult(dto.getAuditResult());
        auditRecord.setAuditReason(auditReason);
        auditRecordMapper.insert(auditRecord);

        String title;
        String contentText;
        if (Integer.valueOf(1).equals(dto.getAuditResult())) {
            title = "你的帖子已审核通过";
            contentText = "你发布的帖子《" + content.getTitle() + "》已审核通过，可以正常展示。";
        } else {
            String reason = auditReason == null ? "未填写" : auditReason;
            title = "你的帖子审核未通过";
            contentText = "你发布的帖子《" + content.getTitle() + "》审核未通过，原因：" + reason;
        }
        messageService.sendToUser(content.getUserId(), title, contentText, "SYSTEM", content.getId());
    }

    @Override
    public List<AuditRecord> listByContentId(Long contentId) {
        checkAdmin();
        return auditRecordMapper.selectByContentId(contentId);
    }

    private void checkAdmin() {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }
        if (currentUser.getRoleId() == null || !currentUser.getRoleId().equals(1L)) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "无权限操作");
        }
    }
}
