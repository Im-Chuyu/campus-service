package com.twilight.campus.service;

import com.twilight.campus.dto.CategorySaveDTO;
import com.twilight.campus.pojo.Category;

import java.util.List;

public interface CategoryService {

    List<Category> list();

    Category getById(Long id);

    void add(CategorySaveDTO dto);

    void update(CategorySaveDTO dto);

    void deleteById(Long id);
}
