package com.twilight.campus.controller;

import com.twilight.campus.dto.CommentAddDTO;
import com.twilight.campus.pojo.Comment;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.CommentService;
import com.twilight.campus.vo.CommentVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 添加评论
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody @Valid CommentAddDTO dto) {
        commentService.add(dto);
        return Result.success("评论成功", null);
    }

    /**
     * 查询某个内容下的评论列表
     */
    @GetMapping("/list/{contentId}")
    public Result<List<CommentVO>> listByContentId(@PathVariable Long contentId) {
        return Result.success(commentService.listByContentId(contentId));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteById(@PathVariable Long id) {
        commentService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
