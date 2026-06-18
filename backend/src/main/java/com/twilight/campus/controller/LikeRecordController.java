package com.twilight.campus.controller;

import com.twilight.campus.pojo.LikeRecord;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.LikeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 点赞控制器
 */
@RestController
@RequestMapping("/like")
public class LikeRecordController {

    @Autowired
    private LikeRecordService likeRecordService;

    /**
     * 点赞
     */
    @PostMapping("/add/{contentId}")
    public Result<?> add(@PathVariable Long contentId) {
        likeRecordService.add(contentId);
        return Result.success("点赞成功", null);
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/delete/{contentId}")
    public Result<?> delete(@PathVariable Long contentId) {
        likeRecordService.delete(contentId);
        return Result.success("取消点赞成功", null);
    }

    /**
     * 我的点赞记录
     */
    @GetMapping("/myList")
    public Result<List<LikeRecord>> myList() {
        return Result.success(likeRecordService.myList());
    }
}
