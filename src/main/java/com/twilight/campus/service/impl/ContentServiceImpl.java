package com.twilight.campus.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.ContentAddDTO;
import com.twilight.campus.dto.ContentQueryDTO;
import com.twilight.campus.dto.ContentReturnAuditDTO;
import com.twilight.campus.dto.ContentUpdateDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.AuditRecordMapper;
import com.twilight.campus.mapper.ActivitySubCategoryMapper;
import com.twilight.campus.mapper.CategoryMapper;
import com.twilight.campus.mapper.CommentMapper;
import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.mapper.UserMapper;
import com.twilight.campus.pojo.Category;
import com.twilight.campus.pojo.Comment;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.pojo.ActivitySubCategory;
import com.twilight.campus.service.AutoAuditService;
import com.twilight.campus.service.ContentCounterBufferService;
import com.twilight.campus.service.ContentService;
import com.twilight.campus.service.MessageService;
import com.twilight.campus.utils.AuthUtil;
import com.twilight.campus.utils.PageUtil;
import com.twilight.campus.utils.UserContext;
import com.twilight.campus.vo.PageResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ContentServiceImpl implements ContentService {

    private static final Pattern UPLOAD_CONTENT_URL_PATTERN = Pattern.compile(
        "(?:https?://[^\\s\"'<>)]*)?/uploads/content/[^\\s\"'<>),]+|uploads/content/[^\\s\"'<>),]+",
        Pattern.CASE_INSENSITIVE
    );

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ActivitySubCategoryMapper activitySubCategoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    @Autowired
    private AutoAuditService autoAuditService;

    @Autowired
    private ContentCounterBufferService counterBufferService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${campus.upload.path:uploads}")
    private String uploadPath;

    @Override
    public List<Content> list(ContentQueryDTO query) {
        applyListVisibility(query);
        return contentMapper.selectList(query);
    }

    @Override
    public PageResultVO<Content> page(ContentQueryDTO query) {
        applyListVisibility(query);
        int page = PageUtil.safePage(query.getPage());
        int pageSize = PageUtil.safePageSize(query.getPageSize());
        query.setPage(page);
        query.setPageSize(pageSize);
        query.setOffset(PageUtil.offset(page, pageSize));
        Long total = contentMapper.countList(query);
        return PageResultVO.of(contentMapper.selectPage(query), total == null ? 0L : total, page, pageSize);
    }

    private void applyListVisibility(ContentQueryDTO query) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser != null && currentUser.getRoleId() != null && currentUser.getRoleId().equals(1L)) {
            query.setIncludePrivate(true);
        } else {
            query.setIncludePrivate(false);
        }
    }

    @Override
    public Content getById(Long id) {
        Content content = contentMapper.selectById(id);
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }
        SysUser currentUser = UserContext.getUser();
        if (!canViewContent(content, currentUser)) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "无权限查看该私密内容");
        }
        if (!isAdmin(currentUser)) {
            if (!counterBufferService.recordContentViewDelta(id, 1)) {
                contentMapper.increaseViewCount(id);
            }
            content.setViewCount((content.getViewCount() == null ? 0 : content.getViewCount()) + 1);
        }
        if (!canViewAuditReason(content, currentUser)) {
            content.setAuditReason(null);
        }
        return content;
    }

    @Override
    public void add(ContentAddDTO dto) {
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "分类不存在");
        }

        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        Content content = new Content();
        BeanUtils.copyProperties(dto, content);
        content.setUserId(currentUser.getId());
        content.setActivitySubCategoryId(resolveActivitySubCategoryId(dto.getActivitySubCategoryId(), category));
        content.setIsPrivate(resolvePrivateFlag(content.getIsPrivate(), category));
        applyVisibilityAndAuditDecision(content);
        content.setIsTop(0);
        content.setViewCount(0);
        content.setLikeCount(0);
        content.setFavoriteCount(0);
        content.setCommentCount(0);

        contentMapper.insert(content);
        if (Integer.valueOf(1).equals(content.getIsPrivate())) {
            return;
        }
        if (Integer.valueOf(0).equals(content.getStatus())) {
            notifyAdminsForAudit(content, "new");
        } else if (Integer.valueOf(1).equals(content.getStatus())) {
            notifyAuthorAutoApproved(content, "new");
        }
    }

    @Override
    public void update(ContentUpdateDTO dto) {
        Content oldContent = contentMapper.selectById(dto.getId());
        if (oldContent == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "分类不存在");
        }

        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        if (!oldContent.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只能修改自己的内容");
        }

        Content content = new Content();
        BeanUtils.copyProperties(dto, content);
        content.setUserId(oldContent.getUserId());
        content.setActivitySubCategoryId(resolveActivitySubCategoryId(dto.getActivitySubCategoryId(), category));
        content.setIsPrivate(resolvePrivateFlag(content.getIsPrivate(), category));
        applyVisibilityAndAuditDecision(content);
        contentMapper.update(content);
        deleteRemovedContentResourceFiles(oldContent, content);

        if (Integer.valueOf(1).equals(content.getIsPrivate())) {
            return;
        }
        if (Integer.valueOf(0).equals(content.getStatus())) {
            notifyAdminsForAudit(content, "update");
        } else if (Integer.valueOf(1).equals(content.getStatus())) {
            notifyAuthorAutoApproved(content, "update");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        Content oldContent = contentMapper.selectById(id);
        if (oldContent == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        boolean isAdmin = currentUser.getRoleId() != null && currentUser.getRoleId().equals(1L);
        if (!isAdmin && !oldContent.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只能删除自己的内容");
        }

        Set<String> resourceUrls = collectContentResourceUrls(oldContent);
        resourceUrls.addAll(collectCommentResourceUrls(id));

        contentMapper.deleteById(id);
        auditRecordMapper.deleteByContentId(id);
        messageService.deleteByRelatedId(id);
        deleteResourceUrls(resourceUrls);
    }

    @Override
    public void toggleTop(Long id) {
        AuthUtil.checkAdmin();
        Content content = contentMapper.selectById(id);
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        int nextTop = Integer.valueOf(1).equals(content.getIsTop()) ? 0 : 1;
        contentMapper.updateTopStatus(id, nextTop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnToAudit(ContentReturnAuditDTO dto) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }
        if (currentUser.getRoleId() == null || !currentUser.getRoleId().equals(1L)) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "无权限操作");
        }

        Content content = contentMapper.selectById(dto.getContentId());
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        String reason = dto.getReason() == null ? null : dto.getReason().trim();
        if (reason == null || reason.isEmpty()) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "打回原因不能为空");
        }

        Content updateContent = new Content();
        updateContent.setId(content.getId());
        updateContent.setAuditReason(reason);
        contentMapper.returnToAudit(updateContent);

        String title = "你的帖子需要修改";
        String body = "你的帖子《" + content.getTitle() + "》已被管理员打回，请根据原因修改后重新提交。原因：" + reason;
        messageService.sendToUser(content.getUserId(), title, body, "SYSTEM", content.getId());
    }

    @Override
    public List<Content> myList(Long userId) {
        return contentMapper.selectByUserId(userId);
    }

    private void notifyAdminsForAudit(Content content, String action) {
        List<SysUser> admins = userMapper.selectList(null, 1, 1L);
        if (admins == null || admins.isEmpty()) {
            return;
        }

        String title = "new".equals(action) ? "有新帖子待审核" : "帖子修改后重新待审核";
        String body = "帖子《" + content.getTitle() + "》需要审核，点击查看处理。";
        for (SysUser admin : admins) {
            messageService.sendToUser(admin.getId(), title, body, "AUDIT", content.getId());
        }
    }

    private void notifyAuthorAutoApproved(Content content, String action) {
        String title = "你的帖子已通过自动审核";
        String body = "new".equals(action)
                ? "你发布的帖子《" + content.getTitle() + "》已通过自动审核，可以正常展示。"
                : "你修改后的帖子《" + content.getTitle() + "》已通过自动审核，可以正常展示。";
        messageService.sendToUser(content.getUserId(), title, body, "SYSTEM", content.getId());
    }

    private void applyAuditDecision(Content content) {
        if (!autoAuditService.isEnabled()) {
            content.setStatus(0);
            content.setAuditReason(null);
            return;
        }

        AutoAuditService.AuditDecision decision = autoAuditService.review(content.getTitle(), content.getContent());
        if (decision.isPass()) {
            content.setStatus(1);
            content.setAuditReason(null);
        } else {
            content.setStatus(0);
            content.setAuditReason(decision.getReason());
        }
    }

    private void applyVisibilityAndAuditDecision(Content content) {
        if (Integer.valueOf(1).equals(content.getIsPrivate())) {
            content.setStatus(1);
            content.setAuditReason(null);
            return;
        }
        applyAuditDecision(content);
    }

    private boolean canViewAuditReason(Content content, SysUser currentUser) {
        if (content == null || content.getAuditReason() == null || content.getAuditReason().trim().isEmpty()) {
            return false;
        }
        if (currentUser == null) {
            return false;
        }
        boolean isAdmin = currentUser.getRoleId() != null && currentUser.getRoleId().equals(1L);
        return isAdmin || currentUser.getId().equals(content.getUserId());
    }

    private Integer normalizePrivateFlag(Integer value) {
        return Integer.valueOf(1).equals(value) ? 1 : 0;
    }

    private Integer resolvePrivateFlag(Integer value, Category category) {
        if (category != null && "二手交易".equals(category.getName())) {
            return 0;
        }
        return normalizePrivateFlag(value);
    }

    private Long resolveActivitySubCategoryId(Long activitySubCategoryId, Category category) {
        if (category == null || !"活动通知".equals(category.getName())) {
            return null;
        }
        if (activitySubCategoryId == null) {
            return null;
        }

        ActivitySubCategory activitySubCategory = activitySubCategoryMapper.selectById(activitySubCategoryId);
        if (activitySubCategory == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "活动通知分类不存在");
        }
        if (!Integer.valueOf(1).equals(activitySubCategory.getStatus())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "活动通知分类已停用");
        }
        return activitySubCategoryId;
    }

    private boolean canViewContent(Content content, SysUser currentUser) {
        if (!Integer.valueOf(1).equals(content.getIsPrivate())) {
            return true;
        }
        if (currentUser == null) {
            return false;
        }
        return isAdmin(currentUser) || currentUser.getId().equals(content.getUserId());
    }

    private boolean isAdmin(SysUser user) {
        return user != null && user.getRoleId() != null && user.getRoleId().equals(1L);
    }

    private void deleteContentResourceFiles(Content content) {
        deleteResourceUrls(collectContentResourceUrls(content));
    }

    private Set<String> collectCommentResourceUrls(Long contentId) {
        Set<String> urls = new LinkedHashSet<>();
        if (contentId == null) {
            return urls;
        }
        List<Comment> comments = commentMapper.selectAllByContentId(contentId);
        if (comments == null || comments.isEmpty()) {
            return urls;
        }
        for (Comment comment : comments) {
            collectUploadUrlsFromText(urls, comment.getCommentText());
        }
        return urls;
    }

    private void deleteRemovedContentResourceFiles(Content oldContent, Content newContent) {
        Set<String> oldUrls = collectContentResourceUrls(oldContent);
        Set<String> newUrls = collectContentResourceUrls(newContent);
        oldUrls.removeAll(newUrls);
        deleteResourceUrls(oldUrls);
    }

    private Set<String> collectContentResourceUrls(Content content) {
        Set<String> urls = new LinkedHashSet<>();
        if (content == null) {
            return urls;
        }
        addUrl(urls, content.getCoverImg());
        collectMediaUrls(urls, content.getMediaUrls());
        return urls;
    }

    private void deleteResourceUrls(Set<String> urls) {
        Path contentDirectory = Paths.get(uploadPath).toAbsolutePath().normalize().resolve("content").normalize();
        for (String url : urls) {
            Path filePath = resolveContentUploadPath(url, contentDirectory);
            if (filePath == null) {
                continue;
            }
            try {
                Files.deleteIfExists(filePath);
            } catch (Exception ignored) {
                // 文件清理失败不影响数据库删除，避免残留文件阻塞业务操作。
            }
        }
    }

    private void collectMediaUrls(Set<String> urls, String mediaUrls) {
        if (mediaUrls == null || mediaUrls.trim().isEmpty()) {
            return;
        }
        String text = mediaUrls.trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            try {
                JsonNode root = objectMapper.readTree(text);
                if (root.isArray()) {
                    for (JsonNode item : root) {
                        if (item.isTextual()) {
                            addUrl(urls, item.asText());
                        } else if (item.hasNonNull("url")) {
                            addUrl(urls, item.get("url").asText());
                        }
                    }
                    return;
                }
            } catch (Exception ignored) {
            }
            collectLooseMediaUrls(urls, text);
            return;
        }
        addUrl(urls, text);
    }

    private void collectLooseMediaUrls(Set<String> urls, String text) {
        String body = text.substring(1, text.length() - 1);
        for (String item : body.split(",")) {
            addUrl(urls, item.replace("\"", "").replace("'", "").trim());
        }
    }

    private void collectUploadUrlsFromText(Set<String> urls, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        Matcher matcher = UPLOAD_CONTENT_URL_PATTERN.matcher(text);
        while (matcher.find()) {
            addUrl(urls, matcher.group());
        }
    }

    private void addUrl(Set<String> urls, String url) {
        if (url != null && !url.trim().isEmpty()) {
            urls.add(url.trim());
        }
    }

    private Path resolveContentUploadPath(String url, Path contentDirectory) {
        try {
            String path = url;
            if (path.startsWith("http://") || path.startsWith("https://")) {
                path = URI.create(path).getPath();
            }
            String fileName = null;
            int markerIndex = path.indexOf("/uploads/content/");
            if (markerIndex >= 0) {
                fileName = path.substring(markerIndex + "/uploads/content/".length());
            } else if (path.startsWith("uploads/content/")) {
                fileName = path.substring("uploads/content/".length());
            } else if (path.startsWith("/content/")) {
                fileName = path.substring("/content/".length());
            } else if (path.startsWith("content/")) {
                fileName = path.substring("content/".length());
            }
            if (fileName == null) {
                return null;
            }
            int queryIndex = fileName.indexOf('?');
            if (queryIndex >= 0) {
                fileName = fileName.substring(0, queryIndex);
            }
            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
            if (fileName.contains("/") || fileName.contains("\\") || fileName.isBlank()) {
                return null;
            }
            Path filePath = contentDirectory.resolve(fileName).normalize();
            return filePath.startsWith(contentDirectory) ? filePath : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
