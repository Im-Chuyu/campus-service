package com.twilight.campus.controller;

import com.twilight.campus.dto.FriendCategoryAssignDTO;
import com.twilight.campus.dto.FriendCategorySaveDTO;
import com.twilight.campus.pojo.FriendCategory;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.FriendService;
import com.twilight.campus.vo.FriendVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping("/list")
    public Result<List<FriendVO>> list() {
        return Result.success(friendService.list());
    }

    @GetMapping("/search")
    public Result<FriendVO> search(String username) {
        return Result.success(friendService.search(username));
    }

    @GetMapping("/status/{friendId}")
    public Result<Integer> status(@PathVariable Long friendId) {
        return Result.success(friendService.status(friendId));
    }

    @GetMapping("/category/list")
    public Result<List<FriendCategory>> categories() {
        return Result.success(friendService.categories());
    }

    @PostMapping("/category")
    public Result<?> saveCategory(@Valid @RequestBody FriendCategorySaveDTO dto) {
        friendService.saveCategory(dto);
        return Result.success("保存分类成功", null);
    }

    @DeleteMapping("/category/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        friendService.deleteCategory(id);
        return Result.success("删除分类成功", null);
    }

    @PutMapping("/category/assign")
    public Result<?> assignCategory(@Valid @RequestBody FriendCategoryAssignDTO dto) {
        friendService.assignCategory(dto);
        return Result.success("移动好友成功", null);
    }

    @PostMapping("/{friendId}")
    public Result<?> add(@PathVariable Long friendId) {
        friendService.add(friendId);
        return Result.success("添加好友成功", null);
    }

    @DeleteMapping("/{friendId}")
    public Result<?> delete(@PathVariable Long friendId) {
        friendService.delete(friendId);
        return Result.success("已取消好友", null);
    }
}
