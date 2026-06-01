package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.CategorySaveDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.CategoryMapper;
import com.twilight.campus.pojo.Category;
import com.twilight.campus.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> list() {
        return categoryMapper.selectAll();
    }

    @Override
    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    @Override
    public void add(CategorySaveDTO dto) {
        Category exist = categoryMapper.selectByName(dto.getName());
        if (exist != null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "分类名称已存在");
        }

        Category category = new Category();
        BeanUtils.copyProperties(dto, category);

        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }

        categoryMapper.insert(category);
    }

    @Override
    public void update(CategorySaveDTO dto) {
        Category oldCategory = categoryMapper.selectById(dto.getId());
        if (oldCategory == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "分类不存在");
        }

        Category sameNameCategory = categoryMapper.selectByName(dto.getName());
        if (sameNameCategory != null && !sameNameCategory.getId().equals(dto.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "分类名称已存在");
        }

        Category category = new Category();
        BeanUtils.copyProperties(dto, category);

        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }

        categoryMapper.update(category);
    }

    @Override
    public void deleteById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "分类不存在");
        }

        categoryMapper.deleteById(id);
    }
}
