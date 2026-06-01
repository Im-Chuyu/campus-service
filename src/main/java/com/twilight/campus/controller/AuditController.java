package com.twilight.campus.controller;

import com.twilight.campus.dto.AuditHandleDTO;
import com.twilight.campus.pojo.AuditRecord;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审核控制器
 */
@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    /**
     * 待审核内容列表
     */
    @GetMapping("/waitList")
    public Result<List<Content>> waitList() {
        return Result.success(auditService.waitAuditList());
    }

    /**
     * 审核处理
     */
    @PostMapping("/handle")
    public Result<?> handle(@RequestBody @Valid AuditHandleDTO dto) {
        auditService.handleAudit(dto);
        return Result.success("审核处理成功", null);
    }

    /**
     * 查询某条内容的审核记录
     */
    @GetMapping("/record/{contentId}")
    public Result<List<AuditRecord>> listByContentId(@PathVariable Long contentId) {
        return Result.success(auditService.listByContentId(contentId));
    }
}
