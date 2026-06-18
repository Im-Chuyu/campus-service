package com.twilight.campus.service;

import com.twilight.campus.dto.FriendCategoryAssignDTO;
import com.twilight.campus.dto.FriendCategorySaveDTO;
import com.twilight.campus.pojo.FriendCategory;
import com.twilight.campus.vo.FriendVO;

import java.util.List;

public interface FriendService {
    List<FriendVO> list();

    FriendVO search(String username);

    void add(Long friendId);

    void delete(Long friendId);

    int status(Long friendId);

    List<FriendCategory> categories();

    void saveCategory(FriendCategorySaveDTO dto);

    void deleteCategory(Long id);

    void assignCategory(FriendCategoryAssignDTO dto);
}
