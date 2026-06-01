package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.NoticeQueryDTO;
import com.twilight.campus.dto.NoticeSaveDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.NoticeMapper;
import com.twilight.campus.pojo.Notice;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.NoticeService;
import com.twilight.campus.utils.AuthUtil;
import com.twilight.campus.utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告业务实现类
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public List<Notice> list(NoticeQueryDTO query) {
        // 管理员才能查全部公告
        AuthUtil.checkAdmin();
        return noticeMapper.selectList(query);
    }

    @Override
    public List<Notice> publishedList() {
        // 用户和游客都可以看已发布公告
        return noticeMapper.selectPublishedList();
    }

    @Override
    public Notice getById(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "公告不存在");
        }

        // 如果是草稿，只有管理员能看
        if (notice.getStatus() != null && notice.getStatus() == 0) {
            SysUser currentUser = UserContext.getUser();
            if (currentUser == null || currentUser.getRoleId() == null || !currentUser.getRoleId().equals(1L)) {
                throw new BusinessException(ResultCodeConstant.FORBIDDEN, "公告未发布，暂无权限查看");
            }
        }

        return notice;
    }

    @Override
    public void add(NoticeSaveDTO dto) {
        AuthUtil.checkAdmin();

        SysUser currentUser = UserContext.getUser();

        Notice notice = new Notice();
        BeanUtils.copyProperties(dto, notice);
        notice.setPublisherId(currentUser.getId());

        noticeMapper.insert(notice);
    }

    @Override
    public void update(NoticeSaveDTO dto) {
        AuthUtil.checkAdmin();

        if (dto.getId() == null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "公告ID不能为空");
        }

        Notice oldNotice = noticeMapper.selectById(dto.getId());
        if (oldNotice == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "公告不存在");
        }

        Notice notice = new Notice();
        BeanUtils.copyProperties(dto, notice);
        noticeMapper.update(notice);
    }

    @Override
    public void deleteById(Long id) {
        AuthUtil.checkAdmin();

        Notice oldNotice = noticeMapper.selectById(id);
        if (oldNotice == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "公告不存在");
        }

        noticeMapper.deleteById(id);
    }

    @Override
    public void toggleTop(Long id) {
        AuthUtil.checkAdmin();

        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "公告不存在");
        }

        Notice updateNotice = new Notice();
        updateNotice.setId(id);
        updateNotice.setTitle(notice.getTitle());
        updateNotice.setContent(notice.getContent());
        updateNotice.setStatus(notice.getStatus());
        updateNotice.setIsTop(notice.getIsTop() != null && notice.getIsTop() == 1 ? 0 : 1);

        noticeMapper.update(updateNotice);
    }
}
