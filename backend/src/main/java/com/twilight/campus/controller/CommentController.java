package com.twilight.campus.controller;

import com.twilight.campus.dto.CommentAddDTO;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.CommentService;
import com.twilight.campus.vo.CommentVO;
import com.twilight.campus.vo.PageResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    public Result<?> add(@RequestBody @Valid CommentAddDTO dto) {
        commentService.add(dto);
        return Result.success("评论成功", null);
    }

    @GetMapping("/list/{contentId}")
    public Result<List<CommentVO>> listByContentId(@PathVariable Long contentId) {
        return Result.success(commentService.listByContentId(contentId));
    }

    @GetMapping("/page/{contentId}")
    public Result<PageResultVO<CommentVO>> pageByContentId(@PathVariable Long contentId,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(commentService.pageByContentId(contentId, page, pageSize));
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteById(@PathVariable Long id) {
        commentService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/react/{commentId}/{reactionType}")
    public Result<?> react(@PathVariable Long commentId, @PathVariable Integer reactionType) {
        commentService.react(commentId, reactionType);
        return Result.success("操作成功", null);
    }
    @PutMapping("/toggleTop/{commentId}")
    public Result<?> toggleTop(@PathVariable Long commentId) {
        commentService.toggleTop(commentId);
        return Result.success("操作成功", null);
    }
}
