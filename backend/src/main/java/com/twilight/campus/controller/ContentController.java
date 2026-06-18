package com.twilight.campus.controller;

import com.twilight.campus.dto.ContentAddDTO;
import com.twilight.campus.dto.ContentQueryDTO;
import com.twilight.campus.dto.ContentReturnAuditDTO;
import com.twilight.campus.dto.ContentUpdateDTO;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.ContentService;
import com.twilight.campus.utils.UserContext;
import com.twilight.campus.vo.PageResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容控制器
 */
@RestController
@RequestMapping("/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    /**
     * 内容列表
     */
    @GetMapping("/list")
    public Result<List<Content>> list(ContentQueryDTO query) {
        return Result.success(contentService.list(query));
    }

    @GetMapping("/page")
    public Result<PageResultVO<Content>> page(ContentQueryDTO query) {
        return Result.success(contentService.page(query));
    }

    /**
     * 内容详情
     */
    @GetMapping("/public/{id}")
    public Result<Content> getPublicById(@PathVariable Long id) {
        return Result.success(contentService.getById(id));
    }

    @GetMapping("/{id}")
    public Result<Content> getById(@PathVariable Long id) {
        return Result.success(contentService.getById(id));
    }

    /**
     * 发布内容
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody @Valid ContentAddDTO dto) {
        contentService.add(dto);
        return Result.success("发布成功", null);
    }

    /**
     * 修改内容
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody @Valid ContentUpdateDTO dto) {
        contentService.update(dto);
        return Result.success("修改成功", null);
    }

    /**
     * 删除内容
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteById(@PathVariable Long id) {
        contentService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 管理员切换内容置顶状态
     */
    @PutMapping("/toggleTop/{id}")
    public Result<?> toggleTop(@PathVariable Long id) {
        contentService.toggleTop(id);
        return Result.success("操作成功", null);
    }

    /**
     * 管理员打回内容，要求发布者修改
     */
    @PutMapping("/returnAudit")
    public Result<?> returnAudit(@RequestBody @Valid ContentReturnAuditDTO dto) {
        contentService.returnToAudit(dto);
        return Result.success("已打回待审核", null);
    }

    /**
     * 我的发布内容
     */
    @GetMapping("/myList")
    public Result<List<Content>> myList() {
        SysUser currentUser = UserContext.getUser();
        return Result.success(contentService.myList(currentUser.getId()));
    }
}
