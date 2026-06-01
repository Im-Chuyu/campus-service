package com.twilight.campus.service;

import com.twilight.campus.pojo.Favorite;

import java.util.List;

public interface FavoriteService {

    /**
     * 收藏内容
     */
    void add(Long contentId);

    /**
     * 取消收藏
     */
    void delete(Long contentId);

    /**
     * 我的收藏
     */
    List<Favorite> myList();
}
