package com.twilight.campus.controller;

import com.twilight.campus.dto.ReportAddDTO;
import com.twilight.campus.dto.ReportHandleDTO;
import com.twilight.campus.dto.ReportQueryDTO;
import com.twilight.campus.pojo.Report;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 举报控制器
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 提交举报
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody @Valid ReportAddDTO dto) {
        reportService.add(dto);
        return Result.success("举报提交成功", null);
    }

    /**
     * 查询我的举报
     */
    @GetMapping("/myList")
    public Result<List<Report>> myList() {
        return Result.success(reportService.myList());
    }

    /**
     * 待处理举报列表（管理员）
     */
    @GetMapping("/pendingList")
    public Result<List<Report>> pendingList() {
        return Result.success(reportService.pendingList());
    }

    /**
     * 处理举报（管理员）
     */
    @PostMapping("/handle")
    public Result<?> handle(@RequestBody @Valid ReportHandleDTO dto) {
        reportService.handle(dto);
        return Result.success("举报处理成功", null);
    }

    /**
     * 举报详情
     */
    @GetMapping("/{id}")
    public Result<Report> getById(@PathVariable Long id) {
        return Result.success(reportService.getById(id));
    }

    /**
     * 条件查询举报列表（管理员）
     */
    @GetMapping("/list")
    public Result<List<Report>> list(ReportQueryDTO query) {
        return Result.success(reportService.list(query));
    }
}
