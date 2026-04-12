package com.teacher.internship.modules.notice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.notice.dto.NoticeSendRequest;
import com.teacher.internship.modules.notice.entity.BizNotice;
import com.teacher.internship.modules.notice.entity.BizNoticeReceiver;
import com.teacher.internship.modules.notice.mapper.BizNoticeMapper;
import com.teacher.internship.modules.notice.mapper.BizNoticeReceiverMapper;
import com.teacher.internship.modules.notice.service.mail.NoticeMailGateway;
import com.teacher.internship.modules.notice.vo.NoticeDetailVO;
import com.teacher.internship.modules.notice.vo.NoticeListItemVO;
import com.teacher.internship.modules.notice.vo.NoticePageVO;
import com.teacher.internship.modules.system.entity.SysRole;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.entity.SysUserRole;
import com.teacher.internship.modules.system.mapper.SysRoleMapper;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import com.teacher.internship.modules.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoticeService {

    private static final String NOTICE_STATUS_SENT = "SENT";
    private static final String NOTICE_TYPE_SYSTEM = "SYSTEM";
    private static final String NOTICE_TYPE_BUSINESS = "BUSINESS";
    private static final String NOTICE_LEVEL_NORMAL = "NORMAL";
    private static final String NOTICE_LEVEL_URGENT = "URGENT";
    private static final String USER_STATUS_ENABLED = "ENABLED";
    private static final String ROLE_SYS_ADMIN = "SYS_ADMIN";
    private static final String BIZ_TYPE_USER_REGISTER_SUBMITTED = "USER_REGISTER_SUBMITTED";

    private final BizNoticeMapper noticeMapper;
    private final BizNoticeReceiverMapper noticeReceiverMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final NoticeMailGateway noticeMailGateway;

    public NoticeService(BizNoticeMapper noticeMapper,
                         BizNoticeReceiverMapper noticeReceiverMapper,
                         SysUserMapper userMapper,
                         SysRoleMapper roleMapper,
                         SysUserRoleMapper userRoleMapper,
                         NoticeMailGateway noticeMailGateway) {
        this.noticeMapper = noticeMapper;
        this.noticeReceiverMapper = noticeReceiverMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.noticeMailGateway = noticeMailGateway;
    }

    public NoticePageVO queryMyNoticePage(long page,
                                          long size,
                                          String keyword,
                                          Integer readFlag,
                                          Long userId,
                                          String currentRoleCode) {
        requireUser(userId);
        validateReadFlag(readFlag);

        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        Set<Long> keywordNoticeIds = queryNoticeIdsByKeyword(keyword);
        if (StringUtils.hasText(keyword) && CollectionUtils.isEmpty(keywordNoticeIds)) {
            return emptyNoticePage(safePage, safeSize);
        }

        LambdaQueryWrapper<BizNoticeReceiver> wrapper = new LambdaQueryWrapper<BizNoticeReceiver>()
                .eq(BizNoticeReceiver::getReceiverId, userId)
                .eq(BizNoticeReceiver::getDeleted, 0L)
                .orderByDesc(BizNoticeReceiver::getId);

        if (readFlag != null) {
            wrapper.eq(BizNoticeReceiver::getReadFlag, readFlag);
        }
        if (!CollectionUtils.isEmpty(keywordNoticeIds)) {
            wrapper.in(BizNoticeReceiver::getNoticeId, keywordNoticeIds);
        }

        List<BizNoticeReceiver> receiverRecords = noticeReceiverMapper.selectList(wrapper);
        Set<Long> noticeIds = receiverRecords.stream().map(BizNoticeReceiver::getNoticeId).collect(Collectors.toSet());
        Map<Long, BizNotice> noticeMap = queryNoticeMap(noticeIds);
        Map<Long, String> senderNameMap = querySenderNameMap(noticeMap.values());
        List<BizNoticeReceiver> accessibleReceivers = receiverRecords.stream()
                .filter(item -> canRoleAccessNotice(noticeMap.get(item.getNoticeId()), currentRoleCode))
                .collect(Collectors.toList());

        int fromIndex = (int) Math.min((safePage - 1) * safeSize, accessibleReceivers.size());
        int toIndex = (int) Math.min(fromIndex + safeSize, accessibleReceivers.size());
        List<BizNoticeReceiver> pageReceivers = accessibleReceivers.subList(fromIndex, toIndex);

        NoticePageVO result = new NoticePageVO();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal(accessibleReceivers.size());
        result.setRecords(pageReceivers.stream()
                .map(item -> toNoticeListItemVO(item, noticeMap.get(item.getNoticeId()), senderNameMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return result;
    }

    public NoticeDetailVO getMyNoticeDetail(Long noticeId, Long userId, String currentRoleCode) {
        requireUser(userId);
        BizNoticeReceiver receiver = requireReceiverByNoticeAndUser(noticeId, userId);
        BizNotice notice = requireNotice(noticeId);
        ensureRoleCanAccessNotice(notice, currentRoleCode);
        String senderName = querySenderName(notice.getSenderId());

        NoticeDetailVO detail = new NoticeDetailVO();
        detail.setNoticeId(notice.getId());
        detail.setNoticeType(notice.getNoticeType());
        detail.setNoticeLevel(notice.getNoticeLevel());
        detail.setNoticeTitle(notice.getNoticeTitle());
        detail.setNoticeContent(notice.getNoticeContent());
        detail.setSenderId(notice.getSenderId());
        detail.setSenderName(senderName);
        detail.setTargetRoleCode(notice.getTargetRoleCode());
        detail.setRelatedBusinessType(notice.getRelatedBusinessType());
        detail.setRelatedBusinessId(notice.getRelatedBusinessId());
        detail.setStatus(notice.getStatus());
        detail.setSendTime(notice.getSendTime());
        detail.setReadFlag(receiver.getReadFlag());
        detail.setReadTime(receiver.getReadTime());
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long noticeId, Long userId, String currentRoleCode) {
        requireUser(userId);
        BizNoticeReceiver receiver = requireReceiverByNoticeAndUser(noticeId, userId);
        BizNotice notice = requireNotice(noticeId);
        ensureRoleCanAccessNotice(notice, currentRoleCode);
        if (Objects.equals(receiver.getReadFlag(), 1)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        receiver.setReadFlag(1);
        receiver.setReadTime(now);
        receiver.setUpdatedBy(userId);
        receiver.setUpdatedTime(now);
        noticeReceiverMapper.updateById(receiver);
    }

    @Transactional(rollbackFor = Exception.class)
    public long deleteMyNotices(List<Long> noticeIds, Long userId, String currentRoleCode) {
        requireUser(userId);
        if (CollectionUtils.isEmpty(noticeIds)) {
            return queryUnreadCount(userId, currentRoleCode);
        }

        Set<Long> normalizedNoticeIds = noticeIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (CollectionUtils.isEmpty(normalizedNoticeIds)) {
            return queryUnreadCount(userId, currentRoleCode);
        }

        List<BizNoticeReceiver> receivers = noticeReceiverMapper.selectList(new LambdaQueryWrapper<BizNoticeReceiver>()
                .eq(BizNoticeReceiver::getReceiverId, userId)
                .eq(BizNoticeReceiver::getDeleted, 0L)
                .in(BizNoticeReceiver::getNoticeId, normalizedNoticeIds));
        if (CollectionUtils.isEmpty(receivers)) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "消息不存在");
        }

        Map<Long, BizNotice> noticeMap = queryNoticeMap(receivers.stream()
                .map(BizNoticeReceiver::getNoticeId)
                .collect(Collectors.toSet()));
        List<Long> deletableNoticeIds = receivers.stream()
                .map(BizNoticeReceiver::getNoticeId)
                .filter(noticeId -> canRoleAccessNotice(noticeMap.get(noticeId), currentRoleCode))
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deletableNoticeIds)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色无权删除该消息");
        }

        LocalDateTime now = LocalDateTime.now();
        noticeReceiverMapper.update(null, new LambdaUpdateWrapper<BizNoticeReceiver>()
                .eq(BizNoticeReceiver::getReceiverId, userId)
                .eq(BizNoticeReceiver::getDeleted, 0L)
                .in(BizNoticeReceiver::getNoticeId, deletableNoticeIds)
                .set(BizNoticeReceiver::getDeleted, 1L)
                .set(BizNoticeReceiver::getUpdatedBy, userId)
                .set(BizNoticeReceiver::getUpdatedTime, now));
        return queryUnreadCount(userId, currentRoleCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public long markAllRead(Long userId, String currentRoleCode) {
        requireUser(userId);
        List<BizNoticeReceiver> receivers = noticeReceiverMapper.selectList(new LambdaQueryWrapper<BizNoticeReceiver>()
                .eq(BizNoticeReceiver::getReceiverId, userId)
                .eq(BizNoticeReceiver::getDeleted, 0L)
                .eq(BizNoticeReceiver::getReadFlag, 0));
        if (CollectionUtils.isEmpty(receivers)) {
            return 0L;
        }

        Map<Long, BizNotice> noticeMap = queryNoticeMap(receivers.stream()
                .map(BizNoticeReceiver::getNoticeId)
                .collect(Collectors.toSet()));
        List<Long> accessibleNoticeIds = receivers.stream()
                .map(BizNoticeReceiver::getNoticeId)
                .filter(noticeId -> canRoleAccessNotice(noticeMap.get(noticeId), currentRoleCode))
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(accessibleNoticeIds)) {
            return 0L;
        }

        LocalDateTime now = LocalDateTime.now();
        int updatedRows = noticeReceiverMapper.update(null, new LambdaUpdateWrapper<BizNoticeReceiver>()
                .eq(BizNoticeReceiver::getReceiverId, userId)
                .eq(BizNoticeReceiver::getDeleted, 0L)
                .eq(BizNoticeReceiver::getReadFlag, 0)
                .in(BizNoticeReceiver::getNoticeId, accessibleNoticeIds)
                .set(BizNoticeReceiver::getReadFlag, 1)
                .set(BizNoticeReceiver::getReadTime, now)
                .set(BizNoticeReceiver::getUpdatedBy, userId)
                .set(BizNoticeReceiver::getUpdatedTime, now));
        return (long) updatedRows;
    }

    public long queryUnreadCount(Long userId, String currentRoleCode) {
        requireUser(userId);
        List<BizNoticeReceiver> receivers = noticeReceiverMapper.selectList(new LambdaQueryWrapper<BizNoticeReceiver>()
                .eq(BizNoticeReceiver::getReceiverId, userId)
                .eq(BizNoticeReceiver::getDeleted, 0L)
                .eq(BizNoticeReceiver::getReadFlag, 0));
        if (CollectionUtils.isEmpty(receivers)) {
            return 0L;
        }
        Map<Long, BizNotice> noticeMap = queryNoticeMap(receivers.stream()
                .map(BizNoticeReceiver::getNoticeId)
                .collect(Collectors.toSet()));
        return receivers.stream()
                .filter(item -> canRoleAccessNotice(noticeMap.get(item.getNoticeId()), currentRoleCode))
                .count();
    }

    public List<NoticeListItemVO> queryRecentMyNotices(int limit,
                                                       Integer unreadOnly,
                                                       Long userId,
                                                       String currentRoleCode) {
        requireUser(userId);
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        LambdaQueryWrapper<BizNoticeReceiver> wrapper = new LambdaQueryWrapper<BizNoticeReceiver>()
                .eq(BizNoticeReceiver::getReceiverId, userId)
                .eq(BizNoticeReceiver::getDeleted, 0L)
                .orderByDesc(BizNoticeReceiver::getId);
        if (unreadOnly != null && unreadOnly == 1) {
            wrapper.eq(BizNoticeReceiver::getReadFlag, 0);
        }

        List<BizNoticeReceiver> receivers = noticeReceiverMapper.selectList(wrapper);
        Set<Long> noticeIds = receivers.stream().map(BizNoticeReceiver::getNoticeId).collect(Collectors.toSet());
        Map<Long, BizNotice> noticeMap = queryNoticeMap(noticeIds);
        Map<Long, String> senderNameMap = querySenderNameMap(noticeMap.values());
        return receivers.stream()
                .filter(item -> canRoleAccessNotice(noticeMap.get(item.getNoticeId()), currentRoleCode))
                .limit(safeLimit)
                .map(item -> toNoticeListItemVO(item, noticeMap.get(item.getNoticeId()), senderNameMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long sendManualNotice(NoticeSendRequest request, Long senderId) {
        requireUser(senderId);
        if (request == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "请求参数不能为空");
        }

        Set<Long> receiverIds = new LinkedHashSet<>();
        if (!CollectionUtils.isEmpty(request.getTargetUserIds())) {
            receiverIds.addAll(request.getTargetUserIds());
        }
        if (StringUtils.hasText(request.getTargetRoleCode())) {
            receiverIds.addAll(queryEnabledUserIdsByRole(request.getTargetRoleCode(), null));
        }
        if (CollectionUtils.isEmpty(receiverIds)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "至少选择一个接收对象");
        }

        Long noticeId = publishNotice(
                request.getNoticeType(),
                request.getNoticeLevel(),
                request.getNoticeTitle(),
                request.getNoticeContent(),
                senderId,
                request.getTargetRoleCode(),
                request.getRelatedBusinessType(),
                request.getRelatedBusinessId(),
                receiverIds,
                Boolean.TRUE.equals(request.getSendEmail())
        );
        if (noticeId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "未找到有效的接收对象");
        }
        return noticeId;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long publishNotice(String noticeType,
                              String noticeLevel,
                              String noticeTitle,
                              String noticeContent,
                              Long senderId,
                              String targetRoleCode,
                              String relatedBusinessType,
                              Long relatedBusinessId,
                              Set<Long> receiverIds,
                              boolean sendEmail) {
        String title = trimToNull(noticeTitle);
        String content = trimToNull(noticeContent);
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "通知标题和通知内容不能为空");
        }
        if (CollectionUtils.isEmpty(receiverIds)) {
            return null;
        }

        Map<Long, SysUser> receiverMap = queryEnabledUserMap(receiverIds);
        if (CollectionUtils.isEmpty(receiverMap)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        Long operatorId = senderId == null ? 0L : senderId;
        BizNotice notice = new BizNotice();
        notice.setNoticeType(normalizeNoticeType(noticeType));
        notice.setNoticeLevel(normalizeNoticeLevel(noticeLevel));
        notice.setNoticeTitle(title);
        notice.setNoticeContent(content);
        notice.setSenderId(senderId);
        notice.setTargetRoleCode(trimToNull(targetRoleCode));
        notice.setRelatedBusinessType(normalizeBusinessType(relatedBusinessType));
        notice.setRelatedBusinessId(relatedBusinessId);
        notice.setSendTime(now);
        notice.setStatus(NOTICE_STATUS_SENT);
        notice.setCreatedBy(operatorId);
        notice.setUpdatedBy(operatorId);
        notice.setDeleted(0L);
        noticeMapper.insert(notice);

        for (Long receiverId : receiverMap.keySet()) {
            BizNoticeReceiver receiver = new BizNoticeReceiver();
            receiver.setNoticeId(notice.getId());
            receiver.setReceiverId(receiverId);
            receiver.setReadFlag(0);
            receiver.setReadTime(null);
            receiver.setCreatedBy(operatorId);
            receiver.setUpdatedBy(operatorId);
            receiver.setDeleted(0L);
            noticeReceiverMapper.insert(receiver);
        }

        if (sendEmail) {
            noticeMailGateway.sendNoticeMail(notice, new ArrayList<>(receiverMap.values()));
        }
        return notice.getId();
    }

    public boolean existsSentNotice(String relatedBusinessType, Long relatedBusinessId) {
        String normalizedType = normalizeBusinessType(relatedBusinessType);
        if (!StringUtils.hasText(normalizedType) || relatedBusinessId == null) {
            return false;
        }
        return noticeMapper.selectCount(new LambdaQueryWrapper<BizNotice>()
                .eq(BizNotice::getRelatedBusinessType, normalizedType)
                .eq(BizNotice::getRelatedBusinessId, relatedBusinessId)
                .eq(BizNotice::getStatus, NOTICE_STATUS_SENT)
                .eq(BizNotice::getDeleted, 0L)) > 0;
    }

    public Set<Long> queryEnabledUserIdsByRole(String roleCode, Long deptId) {
        String normalizedRoleCode = normalizeCode(roleCode);
        if (!StringUtils.hasText(normalizedRoleCode)) {
            return new HashSet<>();
        }
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, normalizedRoleCode)
                .eq(SysRole::getStatus, USER_STATUS_ENABLED)
                .eq(SysRole::getDeleted, 0L)
                .last("LIMIT 1"));
        if (role == null) {
            return new HashSet<>();
        }

        List<SysUserRole> roleRelations = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, role.getId())
                .eq(SysUserRole::getDeleted, 0L));
        if (CollectionUtils.isEmpty(roleRelations)) {
            return new HashSet<>();
        }
        Set<Long> userIds = roleRelations.stream()
                .map(SysUserRole::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashSet<>();
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, userIds)
                .eq(SysUser::getStatus, USER_STATUS_ENABLED)
                .eq(SysUser::getDeleted, 0L);
        if (deptId != null) {
            wrapper.eq(SysUser::getDeptId, deptId);
        }

        return userMapper.selectList(wrapper).stream()
                .map(SysUser::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private BizNoticeReceiver requireReceiverByNoticeAndUser(Long noticeId, Long userId) {
        if (noticeId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "通知标识不能为空");
        }
        BizNoticeReceiver receiver = noticeReceiverMapper.selectOne(new LambdaQueryWrapper<BizNoticeReceiver>()
                .eq(BizNoticeReceiver::getNoticeId, noticeId)
                .eq(BizNoticeReceiver::getReceiverId, userId)
                .eq(BizNoticeReceiver::getDeleted, 0L)
                .last("LIMIT 1"));
        if (receiver == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "通知不存在");
        }
        return receiver;
    }

    private void ensureRoleCanAccessNotice(BizNotice notice, String currentRoleCode) {
        if (!canRoleAccessNotice(notice, currentRoleCode)) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "閫氱煡涓嶅瓨鍦?");
        }
    }

    private BizNotice requireNotice(Long noticeId) {
        if (noticeId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "通知标识不能为空");
        }
        BizNotice notice = noticeMapper.selectOne(new LambdaQueryWrapper<BizNotice>()
                .eq(BizNotice::getId, noticeId)
                .eq(BizNotice::getStatus, NOTICE_STATUS_SENT)
                .eq(BizNotice::getDeleted, 0L)
                .last("LIMIT 1"));
        if (notice == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "通知不存在");
        }
        return notice;
    }

    private Set<Long> queryNoticeIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String value = keyword.trim();
        List<BizNotice> notices = noticeMapper.selectList(new LambdaQueryWrapper<BizNotice>()
                .eq(BizNotice::getStatus, NOTICE_STATUS_SENT)
                .eq(BizNotice::getDeleted, 0L)
                .and(w -> w.like(BizNotice::getNoticeTitle, value)
                        .or()
                        .like(BizNotice::getNoticeContent, value)));
        return notices.stream().map(BizNotice::getId).collect(Collectors.toSet());
    }

    private Map<Long, BizNotice> queryNoticeMap(Set<Long> noticeIds) {
        if (CollectionUtils.isEmpty(noticeIds)) {
            return new HashMap<>();
        }
        return noticeMapper.selectList(new LambdaQueryWrapper<BizNotice>()
                        .in(BizNotice::getId, noticeIds)
                        .eq(BizNotice::getStatus, NOTICE_STATUS_SENT)
                        .eq(BizNotice::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BizNotice::getId, item -> item, (left, right) -> left));
    }

    private Map<Long, String> querySenderNameMap(Iterable<BizNotice> notices) {
        Set<Long> senderIds = new HashSet<>();
        for (BizNotice notice : notices) {
            if (notice != null && notice.getSenderId() != null) {
                senderIds.add(notice.getSenderId());
            }
        }
        if (CollectionUtils.isEmpty(senderIds)) {
            return new HashMap<>();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getId, senderIds)
                        .eq(SysUser::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (left, right) -> left));
    }

    private String querySenderName(Long senderId) {
        if (senderId == null) {
            return null;
        }
        SysUser sender = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, senderId)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        return sender == null ? null : sender.getRealName();
    }

    private Map<Long, SysUser> queryEnabledUserMap(Set<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<>();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getId, userIds)
                        .eq(SysUser::getStatus, USER_STATUS_ENABLED)
                        .eq(SysUser::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private NoticeListItemVO toNoticeListItemVO(BizNoticeReceiver receiver,
                                                BizNotice notice,
                                                Map<Long, String> senderNameMap) {
        if (receiver == null || notice == null) {
            return null;
        }
        NoticeListItemVO item = new NoticeListItemVO();
        item.setNoticeId(notice.getId());
        item.setNoticeType(notice.getNoticeType());
        item.setNoticeLevel(notice.getNoticeLevel());
        item.setNoticeTitle(notice.getNoticeTitle());
        item.setNoticeContentPreview(toPreview(notice.getNoticeContent()));
        item.setSenderId(notice.getSenderId());
        item.setSenderName(senderNameMap.get(notice.getSenderId()));
        item.setTargetRoleCode(notice.getTargetRoleCode());
        item.setRelatedBusinessType(notice.getRelatedBusinessType());
        item.setRelatedBusinessId(notice.getRelatedBusinessId());
        item.setSendTime(notice.getSendTime());
        item.setReadFlag(receiver.getReadFlag());
        item.setReadTime(receiver.getReadTime());
        return item;
    }

    private String toPreview(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String text = content.trim();
        if (text.length() <= 100) {
            return text;
        }
        return text.substring(0, 100) + "...";
    }

    private void validateReadFlag(Integer readFlag) {
        if (readFlag == null) {
            return;
        }
        if (readFlag != 0 && readFlag != 1) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "已读标记只能为 0 或 1");
        }
    }

    private String normalizeNoticeType(String noticeType) {
        String normalized = normalizeCode(noticeType);
        if (!NOTICE_TYPE_SYSTEM.equals(normalized) && !NOTICE_TYPE_BUSINESS.equals(normalized)) {
            return NOTICE_TYPE_BUSINESS;
        }
        return normalized;
    }

    private String normalizeNoticeLevel(String noticeLevel) {
        String normalized = normalizeCode(noticeLevel);
        if (!NOTICE_LEVEL_NORMAL.equals(normalized) && !NOTICE_LEVEL_URGENT.equals(normalized)) {
            return NOTICE_LEVEL_NORMAL;
        }
        return normalized;
    }

    private String normalizeBusinessType(String businessType) {
        if (!StringUtils.hasText(businessType)) {
            return null;
        }
        return businessType.trim().toUpperCase(Locale.ROOT);
    }

    private boolean canRoleAccessNotice(BizNotice notice, String currentRoleCode) {
        if (notice == null) {
            return false;
        }
        String normalizedRoleCode = normalizeCode(currentRoleCode);
        String targetRoleCode = normalizeCode(notice.getTargetRoleCode());
        if (StringUtils.hasText(targetRoleCode) && !Objects.equals(targetRoleCode, normalizedRoleCode)) {
            return false;
        }
        String businessType = normalizeBusinessType(notice.getRelatedBusinessType());
        if (BIZ_TYPE_USER_REGISTER_SUBMITTED.equals(businessType)) {
            return ROLE_SYS_ADMIN.equals(normalizedRoleCode);
        }
        return true;
    }

    private long normalizePage(long page) {
        return page <= 0 ? 1 : page;
    }

    private long normalizeSize(long size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 200);
    }

    private NoticePageVO emptyNoticePage(long page, long size) {
        NoticePageVO result = new NoticePageVO();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(0L);
        result.setRecords(new ArrayList<>());
        return result;
    }

    private SysUser requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), "当前用户不存在");
        }
        return user;
    }

    private String normalizeCode(String text) {
        return text == null ? "" : text.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }
}
