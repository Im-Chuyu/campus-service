package com.twilight.campus.mapper;

import com.twilight.campus.dto.MessageQueryDTO;
import com.twilight.campus.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    int insert(Message message);

    List<Message> selectByUserId(Long userId);

    int countUnreadByUserId(Long userId);

    int markAllReadByUserId(Long userId);

    Message selectById(Long id);

    int markRead(Long id);

    int deleteById(Long id);

    int deleteByRelatedId(Long relatedId);

    int deleteByUserTypeAndRelatedId(@Param("userId") Long userId,
                                     @Param("type") String type,
                                     @Param("relatedId") Long relatedId);

    int markReadByUserTypeAndRelatedId(@Param("userId") Long userId,
                                       @Param("type") String type,
                                       @Param("relatedId") Long relatedId);

    int markReadByUserAndType(@Param("userId") Long userId, @Param("type") String type);

    List<Message> selectList(MessageQueryDTO query);

    Long countList(MessageQueryDTO query);

    List<Message> selectPage(MessageQueryDTO query);
}
