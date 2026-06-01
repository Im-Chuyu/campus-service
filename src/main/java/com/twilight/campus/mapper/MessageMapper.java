package com.twilight.campus.mapper;

import com.twilight.campus.dto.MessageQueryDTO;
import com.twilight.campus.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    /**
     * 新增消息
     */
    int insert(Message message);

    /**
     * 查询当前用户消息列表
     */
    List<Message> selectByUserId(Long userId);

    /**
     * 查询当前用户未读消息数
     */
    int countUnreadByUserId(Long userId);

    /**
     * 根据ID查询消息
     */
    Message selectById(Long id);

    /**
     * 标记已读
     */
    int markRead(Long id);

    /**
     * 删除消息
     */
    int deleteById(Long id);

    /**
     * 条件查询消息
     */
    List<Message> selectList(MessageQueryDTO query);
}
