package com.twilight.campus.controller;

import com.twilight.campus.dto.AutoAuditConfigDTO;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.AutoAuditService;
import com.twilight.campus.vo.AutoAuditConfigVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit/auto")
public class AutoAuditController {

    @Autowired
    private AutoAuditService autoAuditService;

    @GetMapping("/config")
    public Result<AutoAuditConfigVO> getConfig() {
        return Result.success(autoAuditService.getConfig());
    }

    @PutMapping("/config")
    public Result<?> updateConfig(@RequestBody @Valid AutoAuditConfigDTO dto) {
        autoAuditService.updateConfig(dto);
        return Result.success("自动审核开关已更新", null);
    }
}
