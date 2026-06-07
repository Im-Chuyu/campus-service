package com.twilight.campus.controller;

import com.twilight.campus.pojo.Favorite;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.FavoriteService;
import com.twilight.campus.vo.FavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 收藏内容
     */
    @PostMapping("/add/{contentId}")
    public Result<?> add(@PathVariable Long contentId) {
        favoriteService.add(contentId);
        return Result.success("收藏成功", null);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/delete/{contentId}")
    public Result<?> delete(@PathVariable Long contentId) {
        favoriteService.delete(contentId);
        return Result.success("取消收藏成功", null);
    }

    /**
     * 我的收藏
     */
    @GetMapping("/myList")
    public Result<List<FavoriteVO>> myList() {
        return Result.success(favoriteService.myList());
    }
}
