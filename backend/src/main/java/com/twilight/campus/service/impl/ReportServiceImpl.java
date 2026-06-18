package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.ReportAddDTO;
import com.twilight.campus.dto.ReportHandleDTO;
import com.twilight.campus.dto.ReportQueryDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.mapper.ReportMapper;
import com.twilight.campus.mapper.UserMapper;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.pojo.Report;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.MessageService;
import com.twilight.campus.service.ReportService;
import com.twilight.campus.utils.AuthUtil;
import com.twilight.campus.utils.PageUtil;
import com.twilight.campus.utils.UserContext;
import com.twilight.campus.vo.PageResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 举报业务实现类
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @Override
    public void add(ReportAddDTO dto) {
        // 1. 判断登录用户
        SysUser currentUser = AuthUtil.getLoginUser();

        // 2. 判断内容是否存在
        Content content = contentMapper.selectById(dto.getContentId());
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }
        if (!canViewContent(content, currentUser)) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "无权限举报该私密内容");
        }

        // 3. 同一用户对同一内容只限制重复提交待处理举报；管理员处理后可以再次举报。
        if (reportMapper.countPendingByUserAndContent(currentUser.getId(), dto.getContentId()) > 0) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "你已经举报过该内容，请等待管理员处理");
        }

        // 4. 组装举报对象
        Report report = new Report();
        BeanUtils.copyProperties(dto, report);
        report.setUserId(currentUser.getId());
        report.setStatus(0); // 待处理

        reportMapper.insert(report);
        notifyAdminsForReport(report, content, currentUser);
    }

    @Override
    public List<Report> myList() {
        SysUser currentUser = AuthUtil.getLoginUser();
        return reportMapper.selectByUserId(currentUser.getId());
    }

    @Override
    public List<Report> pendingList() {
        AuthUtil.checkAdmin();
        return reportMapper.selectPendingList();
    }

    @Override
    public void handle(ReportHandleDTO dto) {
        // 1. 必须管理员
        AuthUtil.checkAdmin();

        // 2. 查举报是否存在
        Report report = reportMapper.selectById(dto.getReportId());
        if (report == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "举报不存在");
        }

        // 3. 只能处理待处理举报
        if (!Integer.valueOf(0).equals(report.getStatus())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "该举报已处理过");
        }

        // 4. 更新处理结果
        SysUser currentUser = UserContext.getUser();

        Report updateReport = new Report();
        updateReport.setId(report.getId());
        updateReport.setStatus(1);
        updateReport.setHandlerId(currentUser.getId());
        updateReport.setHandleResult(dto.getHandleResult());

        reportMapper.updateHandle(updateReport);
    }

    @Override
    public Report getById(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "举报不存在");
        }
        return report;
    }

    @Override
    public List<Report> list(ReportQueryDTO query) {
        AuthUtil.checkAdmin();
        return reportMapper.selectList(query);
    }

    @Override
    public PageResultVO<Report> page(ReportQueryDTO query) {
        AuthUtil.checkAdmin();
        int page = PageUtil.safePage(query.getPage());
        int pageSize = PageUtil.safePageSize(query.getPageSize());
        query.setPage(page);
        query.setPageSize(pageSize);
        query.setOffset(PageUtil.offset(page, pageSize));
        Long total = reportMapper.countList(query);
        return PageResultVO.of(reportMapper.selectPage(query), total == null ? 0L : total, page, pageSize);
    }

    private void notifyAdminsForReport(Report report, Content content, SysUser reporter) {
        List<SysUser> admins = userMapper.selectList(null, 1, 1L);
        if (admins == null || admins.isEmpty()) {
            return;
        }

        String reporterName = reporter.getNickname() != null && !reporter.getNickname().trim().isEmpty()
                ? reporter.getNickname()
                : reporter.getUsername();
        String title = "有新的内容举报";
        String body = "用户" + reporterName + "举报了帖子《" + content.getTitle() + "》，类型："
                + report.getReportType() + "，原因：" + report.getReportReason();
        for (SysUser admin : admins) {
            messageService.sendToUser(admin.getId(), title, body, "REPORT", content.getId());
        }
    }

    private boolean canViewContent(Content content, SysUser currentUser) {
        if (!Integer.valueOf(1).equals(content.getIsPrivate())) {
            return true;
        }
        boolean isAdmin = currentUser.getRoleId() != null && currentUser.getRoleId().equals(1L);
        return isAdmin || currentUser.getId().equals(content.getUserId());
    }
}
