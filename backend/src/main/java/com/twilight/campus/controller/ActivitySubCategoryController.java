package com.twilight.campus.controller;

import com.twilight.campus.dto.ActivitySubCategorySaveDTO;
import com.twilight.campus.pojo.ActivitySubCategory;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.ActivitySubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activitySubCategory")
public class ActivitySubCategoryController {

    @Autowired
    private ActivitySubCategoryService activitySubCategoryService;

    @GetMapping("/list")
    public Result<List<ActivitySubCategory>> list() {
        return Result.success(activitySubCategoryService.list(false));
    }

    @GetMapping("/admin/list")
    public Result<List<ActivitySubCategory>> adminList() {
        return Result.success(activitySubCategoryService.list(true));
    }

    @GetMapping("/{id}")
    public Result<ActivitySubCategory> getById(@PathVariable Long id) {
        return Result.success(activitySubCategoryService.getById(id));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody ActivitySubCategorySaveDTO dto) {
        activitySubCategoryService.add(dto);
        return Result.success("新增活动通知分类成功", null);
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody ActivitySubCategorySaveDTO dto) {
        activitySubCategoryService.update(dto);
        return Result.success("修改活动通知分类成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteById(@PathVariable Long id) {
        activitySubCategoryService.deleteById(id);
        return Result.success("删除活动通知分类成功", null);
    }
}
