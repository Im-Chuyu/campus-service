package com.twilight.campus.mapper;

import com.twilight.campus.pojo.AuditRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AuditRecordMapper {

    /**
     * 新增审核记录
     */
    int insert(AuditRecord auditRecord);

    /**
     * 根据内容ID查询审核记录
     */
    List<AuditRecord> selectByContentId(Long contentId);
}
