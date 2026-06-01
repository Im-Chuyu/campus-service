package com.twilight.campus.mapper;

import com.twilight.campus.pojo.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> selectAll();

    Category selectById(Long id);

    Category selectByName(String name);

    int insert(Category category);

    int update(Category category);

    int deleteById(Long id);
}
