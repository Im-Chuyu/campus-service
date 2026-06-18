package com.twilight.campus.mapper;

import com.twilight.campus.pojo.PrivateMessage;
import com.twilight.campus.vo.PrivateChatSessionVO;
import com.twilight.campus.vo.PrivateMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrivateMessageMapper {
    int insert(PrivateMessage message);

    List<PrivateMessageVO> selectConversation(@Param("userId") Long userId, @Param("peerId") Long peerId);

    List<PrivateChatSessionVO> selectSessions(Long userId);

    List<PrivateMessageVO> selectConversationAfterId(@Param("userId") Long userId,
                                                     @Param("peerId") Long peerId,
                                                     @Param("afterId") Long afterId);

    int markConversationRead(@Param("userId") Long userId, @Param("peerId") Long peerId);

    int countUnreadByUserId(Long userId);

    int markAllReadByUserId(Long userId);

    int deleteConversationOverflow(@Param("userId") Long userId, @Param("peerId") Long peerId);
}
