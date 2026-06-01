package com.twilight.campus.service;

import com.twilight.campus.dto.AuditHandleDTO;
import com.twilight.campus.pojo.AuditRecord;
import com.twilight.campus.pojo.Content;

import java.util.List;

public interface AuditService {

    /**
     * 待审核内容列表
     */
    List<Content> waitAuditList();

    /**
     * 审核处理：通过/拒绝
     */
    void handleAudit(AuditHandleDTO dto);

    /**
     * 查询某条内容的审核记录
     */
    List<AuditRecord> listByContentId(Long contentId);
}
