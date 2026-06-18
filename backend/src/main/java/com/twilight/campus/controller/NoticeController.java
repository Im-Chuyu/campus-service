package com.twilight.campus.controller;

import com.twilight.campus.dto.NoticeQueryDTO;
import com.twilight.campus.dto.NoticeSaveDTO;
import com.twilight.campus.pojo.Notice;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.NoticeService;
import com.twilight.campus.vo.PageResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 公告列表（管理员）
     */
    @GetMapping("/list")
    public Result<List<Notice>> list(NoticeQueryDTO query) {
        return Result.success(noticeService.list(query));
    }

    @GetMapping("/page")
    public Result<PageResultVO<Notice>> page(NoticeQueryDTO query) {
        return Result.success(noticeService.page(query));
    }

    /**
     * 已发布公告列表（前台/游客可用）
     */
    @GetMapping("/publishedList")
    public Result<List<Notice>> publishedList() {
        return Result.success(noticeService.publishedList());
    }

    /**
     * 公告详情
     */
    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable Long id) {
        return Result.success(noticeService.getById(id));
    }

    /**
     * 新增公告
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody @Valid NoticeSaveDTO dto) {
        noticeService.add(dto);
        return Result.success("公告发布成功", null);
    }

    /**
     * 修改公告
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody @Valid NoticeSaveDTO dto) {
        noticeService.update(dto);
        return Result.success("公告修改成功", null);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteById(@PathVariable Long id) {
        noticeService.deleteById(id);
        return Result.success("公告删除成功", null);
    }

    /**
     * 切换置顶状态
     */
    @PutMapping("/toggleTop/{id}")
    public Result<?> toggleTop(@PathVariable Long id) {
        noticeService.toggleTop(id);
        return Result.success("操作成功", null);
    }
}
