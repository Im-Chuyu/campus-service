package com.twilight.campus.controller;

import com.twilight.campus.dto.CategorySaveDTO;
import com.twilight.campus.pojo.Category;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分类列表
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    /**
     * 分类详情
     */
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /**
     * 新增分类
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody CategorySaveDTO dto) {
        categoryService.add(dto);
        return Result.success("新增分类成功", null);
    }

    /**
     * 修改分类
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody CategorySaveDTO dto) {
        categoryService.update(dto);
        return Result.success("修改分类成功", null);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteById(@PathVariable Long id) {
        categoryService.deleteById(id);
        return Result.success("删除分类成功", null);
    }
}
