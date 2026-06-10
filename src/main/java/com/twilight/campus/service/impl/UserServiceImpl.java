package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.constant.RedisKeyConstant;
import com.twilight.campus.constant.JwtClaimsConstant;
import com.twilight.campus.config.CacheProperties;
import com.twilight.campus.dto.ResetPasswordDTO;
import com.twilight.campus.dto.UserLoginDTO;
import com.twilight.campus.dto.UserRegisterDTO;
import com.twilight.campus.dto.UserUpdateDTO;
import com.twilight.campus.dto.UserUpdatePasswordDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.AuditRecordMapper;
import com.twilight.campus.mapper.CommentMapper;
import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.mapper.FavoriteMapper;
import com.twilight.campus.mapper.LikeRecordMapper;
import com.twilight.campus.mapper.NoticeMapper;
import com.twilight.campus.mapper.UserMapper;
import com.twilight.campus.pojo.Comment;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.pojo.LikeRecord;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.ContentService;
import com.twilight.campus.service.OptionalRedisService;
import com.twilight.campus.service.UserService;
import com.twilight.campus.service.VerificationCodeService;
import com.twilight.campus.utils.AuthUtil;
import com.twilight.campus.utils.JwtUtil;
import com.twilight.campus.utils.PageUtil;
import com.twilight.campus.utils.PasswordUtil;
import com.twilight.campus.utils.UserContext;
import com.twilight.campus.vo.PageResultVO;
import com.twilight.campus.vo.FavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.Year;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final int MAX_LOGIN_DEVICES = 3;
    private static final Pattern UPLOAD_CONTENT_URL_PATTERN = Pattern.compile(
        "(?:https?://[^\\s\"'<>)]*)?/uploads/content/[^\\s\"'<>),]+|uploads/content/[^\\s\"'<>),]+",
        Pattern.CASE_INSENSITIVE
    );

    @Value("${campus.upload.path:uploads}")
    private String uploadPath;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ContentService contentService;

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private LikeRecordMapper likeRecordMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CacheProperties cacheProperties;

    @Autowired
    private OptionalRedisService redisService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Override
    public void register(UserRegisterDTO dto) {
        ensureSignatureColumn();
        ensureUsernameUpdateColumns();
        ensureProfileBackgroundColumn();

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "两次密码不一致");
        }
        verificationCodeService.verifyAndConsume("register", "phone", dto.getPhone(), dto.getCode());

        SysUser existUser = userMapper.selectByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "用户名已存在");
        }

        SysUser existPhone = userMapper.selectByPhone(dto.getPhone());
        if (existPhone != null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "手机号已存在");
        }

        String email = dto.getEmail() == null || dto.getEmail().trim().isEmpty() ? null : dto.getEmail().trim();
        if (email != null) {
            SysUser existEmail = userMapper.selectByEmail(email);
            if (existEmail != null) {
                throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "邮箱已存在");
            }
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setNickname(dto.getNickname().trim());
        user.setSignature(null);
        user.setPhone(dto.getPhone());
        user.setPhoneVisible(0);
        user.setEmail(email);
        user.setEmailVisible(0);
        user.setWechatVisible(0);
        user.setQqVisible(0);
        user.setPrivateChatEnabled(1);
        user.setGender(1);
        user.setRoleId(2L);
        user.setStatus(1);
        user.setUsernameUpdateYear(Year.now().getValue());
        user.setUsernameUpdateCount(0);

        userMapper.insert(user);
    }

    @Override
    public String login(UserLoginDTO dto) {
        String account = dto.getUsername() == null ? null : dto.getUsername().trim();
        assertLoginAllowed(account);
        SysUser user = null;
        if (account != null && !account.isEmpty()) {
            user = userMapper.selectByUsername(account);
            if (user == null) {
                user = userMapper.selectByPhone(account);
            }
            if (user == null) {
                user = userMapper.selectByEmail(account);
            }
        }
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "账号已被禁用");
        }

        if (!PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            recordLoginFailure(account);
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "密码错误");
        }

        clearLoginFailure(account);
        String deviceId = normalizeDeviceId(dto.getDeviceId());
        String cachedToken = getReusableLoginToken(user.getId(), deviceId);
        if (cachedToken != null) {
            return cachedToken;
        }

        String token = JwtUtil.createToken(user.getId(), user.getUsername(), String.valueOf(user.getRoleId()), deviceId);
        cacheLoginToken(user.getId(), deviceId, token);
        return token;
    }

    @Override
    public void updatePassword(UserUpdatePasswordDTO dto) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "两次新密码不一致");
        }
        String target = resolveUserVerificationTarget(currentUser, dto.getChannel());
        verificationCodeService.verifyAndConsume("change_password", dto.getChannel(), target, dto.getCode());

        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "新密码不能和旧密码相同");
        }

        if (!PasswordUtil.matches(dto.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "原密码错误");
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(currentUser.getId());
        updateUser.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        userMapper.updatePassword(updateUser);
        clearLoginTokens(currentUser.getId());
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "两次新密码不一致");
        }

        SysUser user = findUserByChannel(dto.getChannel(), dto.getTarget());
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "账号已被禁用");
        }
        verificationCodeService.verifyAndConsume("reset_password", dto.getChannel(), dto.getTarget(), dto.getCode());
        if (PasswordUtil.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "新密码不能和旧密码相同");
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        userMapper.updatePassword(updateUser);
        clearLoginTokens(user.getId());
    }

    @Override
    public void validateCodeTarget(String scene, String channel, String target) {
        if ("register".equals(scene)) {
            if (!"phone".equals(channel)) {
                throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "注册验证码请使用手机号接收");
            }
            if (userMapper.selectByPhone(target) != null) {
                throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "手机号已存在");
            }
            return;
        }

        SysUser user = findUserByChannel(channel, target);
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "账号已被禁用");
        }
    }

    @Override
    public SysUser getUserInfo(Long userId) {
        ensureProfileBackgroundColumn();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }

        SysUser currentUser = UserContext.getUser();
        boolean isSelf = currentUser != null && currentUser.getId().equals(userId);
        boolean isAdmin = currentUser != null && currentUser.getRoleId() != null && currentUser.getRoleId().equals(1L);
        if (isSelf || isAdmin) {
            return user;
        }

        return maskPrivateFields(user);
    }

    @Override
    public void updateUser(UserUpdateDTO dto) {
        ensureSignatureColumn();
        ensureUsernameUpdateColumns();
        ensureProfileBackgroundColumn();

        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        SysUser user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }

        String newUsername = dto.getUsername().trim();
        if (!newUsername.equals(user.getUsername())) {
            validateUsernameUpdate(user, newUsername);
            applyUsernameUpdateCounter(user);
            user.setUsername(newUsername);
        }

        user.setNickname(dto.getNickname().trim());
        if (dto.getSignature() != null) {
            user.setSignature(dto.getSignature());
        }
        if (dto.getPhone() != null) {
            String phone = normalizeOptionalText(dto.getPhone());
            if (phone != null && !phone.equals(user.getPhone())) {
                SysUser existPhone = userMapper.selectByPhone(phone);
                if (existPhone != null && !existPhone.getId().equals(user.getId())) {
                    throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "手机号已存在");
                }
            }
            user.setPhone(phone);
        }
        if (dto.getPhoneVisible() != null) {
            user.setPhoneVisible(dto.getPhoneVisible());
        }
        if (dto.getEmail() != null) {
            String email = normalizeOptionalText(dto.getEmail());
            if (email != null && !email.equals(user.getEmail())) {
                SysUser existEmail = userMapper.selectByEmail(email);
                if (existEmail != null && !existEmail.getId().equals(user.getId())) {
                    throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "邮箱已存在");
                }
            }
            user.setEmail(email);
        }
        if (dto.getEmailVisible() != null) {
            user.setEmailVisible(dto.getEmailVisible());
        }
        if (dto.getWechat() != null) {
            user.setWechat(normalizeOptionalText(dto.getWechat()));
        }
        if (dto.getWechatVisible() != null) {
            user.setWechatVisible(dto.getWechatVisible());
        }
        if (dto.getQq() != null) {
            user.setQq(normalizeOptionalText(dto.getQq()));
        }
        if (dto.getQqVisible() != null) {
            user.setQqVisible(dto.getQqVisible());
        }
        if (dto.getPrivateChatEnabled() != null) {
            user.setPrivateChatEnabled(dto.getPrivateChatEnabled());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        String oldAvatar = user.getAvatar();
        String oldProfileBackground = user.getProfileBackground();
        if (dto.getAvatar() != null) {
            user.setAvatar(normalizeOptionalText(dto.getAvatar()));
        }
        if (dto.getProfileBackground() != null) {
            user.setProfileBackground(normalizeOptionalText(dto.getProfileBackground()));
        }

        userMapper.updateUser(user);
        deleteReplacedProfileResource(oldAvatar, user.getAvatar(), user.getProfileBackground());
        deleteReplacedProfileResource(oldProfileBackground, user.getAvatar(), user.getProfileBackground());
    }

    @Override
    public List<SysUser> list(String keyword, Integer status, Long roleId) {
        AuthUtil.checkAdmin();
        ensureProfileBackgroundColumn();
        return userMapper.selectList(keyword, status, roleId);
    }

    @Override
    public PageResultVO<SysUser> page(String keyword, Integer status, Long roleId, Integer page, Integer pageSize) {
        AuthUtil.checkAdmin();
        ensureProfileBackgroundColumn();
        int safePage = PageUtil.safePage(page);
        int safePageSize = PageUtil.safePageSize(pageSize);
        Long total = userMapper.countList(keyword, status, roleId);
        return PageResultVO.of(
            userMapper.selectPage(keyword, status, roleId, PageUtil.offset(safePage, safePageSize), safePageSize),
            total == null ? 0L : total,
            safePage,
            safePageSize
        );
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        AuthUtil.checkAdmin();
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "用户状态不正确");
        }

        SysUser currentUser = AuthUtil.getLoginUser();
        if (currentUser.getId().equals(id)) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "不能修改自己的账号状态");
        }

        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }

        boolean isSuperAdmin = "admin".equals(currentUser.getUsername());
        boolean targetIsAdmin = user.getRoleId() != null && user.getRoleId().equals(1L);
        if (!isSuperAdmin && targetIsAdmin) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只有 admin 可以操作管理员账号");
        }

        userMapper.updateStatus(id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        AuthUtil.checkAdmin();
        if (id == null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "用户ID不能为空");
        }

        SysUser currentUser = AuthUtil.getLoginUser();
        if (currentUser.getId().equals(id)) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "不能删除自己的账号");
        }

        SysUser targetUser = userMapper.selectById(id);
        if (targetUser == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }
        if ("admin".equals(targetUser.getUsername())) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "不能删除内置 admin 账号");
        }

        boolean targetIsAdmin = targetUser.getRoleId() != null && targetUser.getRoleId().equals(1L);
        if (!AuthUtil.isSuperAdmin(currentUser) && targetIsAdmin) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只有 admin 可以删除管理员账号");
        }

        List<Comment> userComments = commentMapper.selectAllByUserId(id);
        Set<String> commentResourceUrls = collectUserCommentResourceUrls(userComments);
        Set<Long> affectedContentIds = collectAffectedContentIds(id, userComments);
        List<Content> contents = contentService.myList(id);
        if (contents != null) {
            for (Content content : contents) {
                if (content != null && content.getId() != null) {
                    affectedContentIds.remove(content.getId());
                    contentService.deleteById(content.getId());
                }
            }
        }

        auditRecordMapper.deleteByAuditorId(id);
        noticeMapper.deleteByPublisherId(id);

        int deleted = userMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }

        clearLoginTokens(id);
        deleteResourceUrls(commentResourceUrls);
        deleteReplacedProfileResource(targetUser.getAvatar());
        deleteReplacedProfileResource(targetUser.getProfileBackground());
        refreshAffectedContentCounts(affectedContentIds);
    }

    private SysUser maskPrivateFields(SysUser user) {
        SysUser masked = new SysUser();
        masked.setId(user.getId());
        masked.setUsername(user.getUsername());
        masked.setNickname(user.getNickname());
        masked.setSignature(user.getSignature());
        masked.setAvatar(user.getAvatar());
        masked.setProfileBackground(user.getProfileBackground());
        masked.setPhoneVisible(user.getPhoneVisible());
        masked.setEmailVisible(user.getEmailVisible());
        masked.setWechatVisible(user.getWechatVisible());
        masked.setQqVisible(user.getQqVisible());
        masked.setPrivateChatEnabled(user.getPrivateChatEnabled());
        masked.setGender(user.getGender());
        masked.setRoleId(user.getRoleId());
        masked.setStatus(user.getStatus());
        masked.setCreateTime(user.getCreateTime());
        masked.setUpdateTime(user.getUpdateTime());

        masked.setPhone(user.getPhoneVisible() != null && user.getPhoneVisible() == 1 ? user.getPhone() : null);
        masked.setEmail(user.getEmailVisible() != null && user.getEmailVisible() == 1 ? user.getEmail() : null);
        masked.setWechat(user.getWechatVisible() != null && user.getWechatVisible() == 1 ? user.getWechat() : null);
        masked.setQq(user.getQqVisible() != null && user.getQqVisible() == 1 ? user.getQq() : null);
        return masked;
    }

    private SysUser findUserByChannel(String channel, String target) {
        if ("phone".equals(channel)) {
            return userMapper.selectByPhone(target);
        }
        if ("email".equals(channel)) {
            return userMapper.selectByEmail(target);
        }
        throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "接收方式不正确");
    }

    private String resolveUserVerificationTarget(SysUser user, String channel) {
        if ("phone".equals(channel)) {
            if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
                throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "当前账号未绑定手机号");
            }
            return user.getPhone();
        }
        if ("email".equals(channel)) {
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "当前账号未绑定邮箱");
            }
            return user.getEmail();
        }
        throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "接收方式不正确");
    }

    private void assertLoginAllowed(String account) {
        if (account == null || account.isEmpty() || !redisService.enabled()) {
            return;
        }
        String value = redisService.get(loginFailKey(account));
        if (value == null) {
            return;
        }
        try {
            int failCount = Integer.parseInt(value);
            if (failCount >= cacheProperties.getLoginRateLimitMaxAttempts()) {
                throw new BusinessException(ResultCodeConstant.TOO_MANY_REQUESTS, "登录失败次数过多，请稍后再试");
            }
        } catch (NumberFormatException ignored) {
            redisService.delete(loginFailKey(account));
        }
    }

    private void recordLoginFailure(String account) {
        if (account == null || account.isEmpty()) {
            return;
        }
        // Redis 开启后记录短时间登录失败次数，减少暴力尝试对数据库和账号安全的压力。
        redisService.increment(
            loginFailKey(account),
            1,
            Duration.ofSeconds(cacheProperties.getLoginRateLimitWindowSeconds())
        );
    }

    private void clearLoginFailure(String account) {
        if (account == null || account.isEmpty()) {
            return;
        }
        redisService.delete(loginFailKey(account));
    }

    private String loginFailKey(String account) {
        return RedisKeyConstant.LOGIN_FAIL_PREFIX + account;
    }

    private String getReusableLoginToken(Long userId, String deviceId) {
        if (userId == null || deviceId == null || deviceId.isEmpty() || !redisService.enabled()) {
            return null;
        }

        String tokenKey = loginTokenKey(userId, deviceId);
        String token = redisService.get(tokenKey);
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            JwtUtil.parseToken(token);
            touchLoginDevice(userId, deviceId);
            return token;
        } catch (Exception e) {
            redisService.delete(tokenKey);
            redisService.removeFromZSet(loginDeviceKey(userId), deviceId);
            return null;
        }
    }

    private void cacheLoginToken(Long userId, String deviceId, String token) {
        if (userId == null || deviceId == null || deviceId.isEmpty() || token == null || token.isEmpty()) {
            return;
        }
        Duration ttl = Duration.ofMillis(JwtClaimsConstant.JWT_TTL);
        redisService.set(loginTokenKey(userId, deviceId), token, ttl);
        redisService.addToZSet(loginDeviceKey(userId), deviceId, System.currentTimeMillis(), ttl);
        evictOverflowLoginDevices(userId);
    }

    private void touchLoginDevice(Long userId, String deviceId) {
        redisService.addToZSet(
            loginDeviceKey(userId),
            deviceId,
            System.currentTimeMillis(),
            Duration.ofMillis(JwtClaimsConstant.JWT_TTL)
        );
    }

    private void evictOverflowLoginDevices(Long userId) {
        Long deviceCount = redisService.zCard(loginDeviceKey(userId));
        if (deviceCount == null || deviceCount <= MAX_LOGIN_DEVICES) {
            return;
        }

        Set<String> overflowDeviceIds = redisService.zRange(loginDeviceKey(userId), 0, deviceCount - MAX_LOGIN_DEVICES - 1);
        for (String overflowDeviceId : overflowDeviceIds) {
            redisService.removeFromZSet(loginDeviceKey(userId), overflowDeviceId);
            redisService.delete(loginTokenKey(userId, overflowDeviceId));
        }
    }

    private void clearLoginTokens(Long userId) {
        if (userId == null || !redisService.enabled()) {
            return;
        }
        String deviceKey = loginDeviceKey(userId);
        for (String deviceId : redisService.zRange(deviceKey, 0, -1)) {
            redisService.delete(loginTokenKey(userId, deviceId));
        }
        redisService.delete(deviceKey);
    }

    private String normalizeDeviceId(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return "legacy-" + UUID.randomUUID();
        }
        String normalized = deviceId.trim();
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }

    private String loginTokenKey(Long userId, String deviceId) {
        return RedisKeyConstant.LOGIN_TOKEN_PREFIX + userId + ":" + deviceId;
    }

    private String loginDeviceKey(Long userId) {
        return RedisKeyConstant.LOGIN_DEVICE_PREFIX + userId;
    }

    private void ensureSignatureColumn() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, "sys_user", "signature")) {
                if (rs.next()) {
                    return;
                }
            }

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                    "ALTER TABLE sys_user ADD COLUMN signature VARCHAR(200) DEFAULT NULL COMMENT '个性签名' AFTER nickname"
                );
            }
        } catch (Exception e) {
            throw new BusinessException(ResultCodeConstant.ERROR, "个性签名字段初始化失败");
        }
    }

    private void ensureUsernameUpdateColumns() {
        ensureColumn("username_update_year", "alter table sys_user add column username_update_year int null");
        ensureColumn("username_update_count", "alter table sys_user add column username_update_count int not null default 0");
    }

    private void ensureProfileBackgroundColumn() {
        ensureColumn("profile_background", "alter table sys_user add column profile_background varchar(255) default null after avatar");
    }

    private void ensureColumn(String columnName, String alterSql) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, "sys_user", columnName)) {
                if (columns.next()) {
                    return;
                }
            }

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(alterSql);
            }
        } catch (Exception ignored) {
            // 启动期兼容旧库字段，失败时保留原业务流程。
        }
    }

    private void deleteReplacedProfileResource(String oldUrl, String... retainedUrls) {
        if (oldUrl == null || oldUrl.trim().isEmpty()) {
            return;
        }
        String normalizedOldUrl = oldUrl.trim();
        if (retainedUrls != null) {
            for (String retainedUrl : retainedUrls) {
                if (retainedUrl != null && normalizedOldUrl.equals(retainedUrl.trim())) {
                    return;
                }
            }
        }

        Path profileDirectory = Paths.get(uploadPath).toAbsolutePath().normalize().resolve("profile").normalize();
        Path filePath = resolveProfileUploadPath(normalizedOldUrl, profileDirectory);
        if (filePath == null) {
            return;
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (Exception ignored) {
            // Keep profile save successful even if old file cleanup fails.
        }
    }

    private Set<String> collectUserCommentResourceUrls(List<Comment> comments) {
        Set<String> urls = new LinkedHashSet<>();
        if (comments == null || comments.isEmpty()) {
            return urls;
        }
        for (Comment comment : comments) {
            collectUploadUrlsFromText(urls, comment.getCommentText());
        }
        return urls;
    }

    private Set<Long> collectAffectedContentIds(Long userId, List<Comment> comments) {
        Set<Long> ids = new LinkedHashSet<>();
        if (comments != null) {
            for (Comment comment : comments) {
                if (comment.getContentId() != null) {
                    ids.add(comment.getContentId());
                }
            }
        }
        if (userId != null) {
            contentService.myList(userId).forEach(content -> {
                if (content.getId() != null) {
                    ids.add(content.getId());
                }
            });
            for (LikeRecord likeRecord : likeRecordMapper.selectByUserId(userId)) {
                if (likeRecord.getContentId() != null) {
                    ids.add(likeRecord.getContentId());
                }
            }
            for (FavoriteVO favorite : favoriteMapper.selectByUserId(userId)) {
                if (favorite.getContentId() != null) {
                    ids.add(favorite.getContentId());
                }
            }
        }
        return ids;
    }

    private void refreshAffectedContentCounts(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        contentMapper.refreshInteractionCounts(ids);
    }

    private void collectUploadUrlsFromText(Set<String> urls, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        Matcher matcher = UPLOAD_CONTENT_URL_PATTERN.matcher(text);
        while (matcher.find()) {
            urls.add(matcher.group());
        }
    }

    private void deleteResourceUrls(Set<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        Path contentDirectory = Paths.get(uploadPath).toAbsolutePath().normalize().resolve("content").normalize();
        for (String url : urls) {
            Path filePath = resolveContentUploadPath(url, contentDirectory);
            if (filePath == null) {
                continue;
            }
            try {
                Files.deleteIfExists(filePath);
            } catch (Exception ignored) {
                // 文件清理失败不影响账号删除，避免残留文件阻塞业务操作。
            }
        }
    }

    private Path resolveContentUploadPath(String url, Path contentDirectory) {
        try {
            String path = url == null ? "" : url.trim();
            if (path.startsWith("http://") || path.startsWith("https://")) {
                path = URI.create(path).getPath();
            }
            String fileName = null;
            int markerIndex = path.indexOf("/uploads/content/");
            if (markerIndex >= 0) {
                fileName = path.substring(markerIndex + "/uploads/content/".length());
            } else if (path.startsWith("uploads/content/")) {
                fileName = path.substring("uploads/content/".length());
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

    private Path resolveProfileUploadPath(String url, Path profileDirectory) {
        try {
            String path = url;
            if (path.startsWith("http://") || path.startsWith("https://")) {
                path = URI.create(path).getPath();
            }
            int markerIndex = path.indexOf("/uploads/profile/");
            if (markerIndex < 0) {
                return null;
            }
            String fileName = path.substring(markerIndex + "/uploads/profile/".length());
            int queryIndex = fileName.indexOf('?');
            if (queryIndex >= 0) {
                fileName = fileName.substring(0, queryIndex);
            }
            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
            if (fileName.contains("/") || fileName.contains("\\") || fileName.isBlank()) {
                return null;
            }
            Path filePath = profileDirectory.resolve(fileName).normalize();
            return filePath.startsWith(profileDirectory) ? filePath : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private void validateUsernameUpdate(SysUser user, String newUsername) {
        SysUser existUser = userMapper.selectByUsername(newUsername);
        if (existUser != null && !existUser.getId().equals(user.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "用户名已存在");
        }

        int currentYear = Year.now().getValue();
        int updateYear = user.getUsernameUpdateYear() == null ? currentYear : user.getUsernameUpdateYear();
        int updateCount = updateYear == currentYear ? (user.getUsernameUpdateCount() == null ? 0 : user.getUsernameUpdateCount()) : 0;
        if (updateCount >= 3) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "用户名每年最多只能修改3次");
        }
    }

    private void applyUsernameUpdateCounter(SysUser user) {
        int currentYear = Year.now().getValue();
        int updateYear = user.getUsernameUpdateYear() == null ? currentYear : user.getUsernameUpdateYear();
        int updateCount = updateYear == currentYear ? (user.getUsernameUpdateCount() == null ? 0 : user.getUsernameUpdateCount()) : 0;
        user.setUsernameUpdateYear(currentYear);
        user.setUsernameUpdateCount(updateCount + 1);
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
