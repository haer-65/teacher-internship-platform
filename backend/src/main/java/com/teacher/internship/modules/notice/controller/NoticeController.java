package com.teacher.internship.modules.notice.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.notice.dto.NoticeSendRequest;
import com.teacher.internship.modules.notice.service.NoticeService;
import com.teacher.internship.modules.notice.service.NoticeTriggerService;
import com.teacher.internship.modules.notice.vo.NoticeDetailVO;
import com.teacher.internship.modules.notice.vo.NoticeListItemVO;
import com.teacher.internship.modules.notice.vo.NoticePageVO;
import com.teacher.internship.modules.notice.vo.NoticeTriggerResultVO;
import com.teacher.internship.modules.notice.vo.NoticeUnreadVO;
import com.teacher.internship.security.auth.SecurityUtils;
import com.teacher.internship.security.jwt.JwtUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/notice")
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeTriggerService noticeTriggerService;

    public NoticeController(NoticeService noticeService, NoticeTriggerService noticeTriggerService) {
        this.noticeService = noticeService;
        this.noticeTriggerService = noticeTriggerService;
    }

    @GetMapping("/page")
    @PreAuthorize("@permissionService.hasPermission('notice:view')")
    public ApiResponse<NoticePageVO> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer readFlag) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(noticeService.queryMyNoticePage(
                page,
                size,
                keyword,
                readFlag,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("@permissionService.hasPermission('notice:view')")
    public ApiResponse<NoticeDetailVO> detail(@PathVariable("id") Long noticeId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(noticeService.getMyNoticeDetail(
                noticeId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/read/{id}")
    @PreAuthorize("@permissionService.hasPermission('notice:view')")
    public ApiResponse<NoticeUnreadVO> markRead(@PathVariable("id") Long noticeId) {
        JwtUserPrincipal principal = requirePrincipal();
        noticeService.markRead(noticeId, principal.getUserId(), principal.getRoleCode());
        NoticeUnreadVO vo = new NoticeUnreadVO();
        vo.setUnreadCount(noticeService.queryUnreadCount(principal.getUserId(), principal.getRoleCode()));
        return ApiResponse.success(vo);
    }

    @PostMapping("/read-all")
    @PreAuthorize("@permissionService.hasPermission('notice:view')")
    public ApiResponse<NoticeUnreadVO> markAllRead() {
        JwtUserPrincipal principal = requirePrincipal();
        noticeService.markAllRead(principal.getUserId(), principal.getRoleCode());
        NoticeUnreadVO vo = new NoticeUnreadVO();
        vo.setUnreadCount(noticeService.queryUnreadCount(principal.getUserId(), principal.getRoleCode()));
        return ApiResponse.success(vo);
    }

    @PostMapping("/delete")
    @PreAuthorize("@permissionService.hasPermission('notice:view')")
    public ApiResponse<NoticeUnreadVO> delete(@Valid @RequestBody List<Long> noticeIds) {
        JwtUserPrincipal principal = requirePrincipal();
        long unreadCount = noticeService.deleteMyNotices(noticeIds, principal.getUserId(), principal.getRoleCode());
        NoticeUnreadVO vo = new NoticeUnreadVO();
        vo.setUnreadCount(unreadCount);
        return ApiResponse.success(vo);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("@permissionService.hasPermission('notice:view')")
    public ApiResponse<NoticeUnreadVO> unreadCount() {
        JwtUserPrincipal principal = requirePrincipal();
        NoticeUnreadVO vo = new NoticeUnreadVO();
        vo.setUnreadCount(noticeService.queryUnreadCount(principal.getUserId(), principal.getRoleCode()));
        return ApiResponse.success(vo);
    }

    @GetMapping("/recent")
    @PreAuthorize("@permissionService.hasPermission('notice:view')")
    public ApiResponse<List<NoticeListItemVO>> recent(@RequestParam(defaultValue = "5") int limit,
                                                      @RequestParam(required = false, defaultValue = "1") Integer unreadOnly) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(noticeService.queryRecentMyNotices(
                limit,
                unreadOnly,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/send")
    @PreAuthorize("@permissionService.hasPermission('notice:send')")
    public ApiResponse<Long> send(@Valid @RequestBody NoticeSendRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(noticeService.sendManualNotice(request, principal.getUserId()));
    }

    @PostMapping("/trigger/material-deadline")
    @PreAuthorize("@permissionService.hasPermission('notice:send')")
    public ApiResponse<NoticeTriggerResultVO> triggerMaterialDeadline() {
        long generatedCount = noticeTriggerService.triggerMaterialDeadlineReminder();
        NoticeTriggerResultVO result = new NoticeTriggerResultVO();
        result.setTriggerType("MATERIAL_DEADLINE");
        result.setGeneratedCount(generatedCount);
        return ApiResponse.success(result);
    }

    private JwtUserPrincipal requirePrincipal() {
        JwtUserPrincipal principal = SecurityUtils.currentPrincipal();
        if (principal == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        return principal;
    }
}
