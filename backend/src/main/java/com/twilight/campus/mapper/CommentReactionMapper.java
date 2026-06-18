package com.twilight.campus.mapper;

import com.twilight.campus.pojo.CommentReaction;
import com.twilight.campus.vo.CommentReactionStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentReactionMapper {

    CommentReaction selectByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    int insert(CommentReaction commentReaction);

    int updateReactionType(CommentReaction commentReaction);

    int deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    List<CommentReactionStatVO> selectStatsByCommentIds(@Param("commentIds") List<Long> commentIds, @Param("userId") Long userId);
}
