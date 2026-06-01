package com.twilight.campus.service;

import com.twilight.campus.pojo.LikeRecord;

import java.util.List;

public interface LikeRecordService {

    /**
     * 点赞
     */
    void add(Long contentId);

    /**
     * 取消点赞
     */
    void delete(Long contentId);

    /**
     * 我的点赞记录
     */
    List<LikeRecord> myList();
}
