package com.teacher.internship.modules.score.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.score.dto.ScoreAdjustRequest;
import com.teacher.internship.modules.score.service.ScoreService;
import com.teacher.internship.modules.score.vo.ScoreDetailVO;
import com.teacher.internship.modules.score.vo.ScorePageVO;
import com.teacher.internship.modules.score.vo.ScorePublishPreviewVO;
import com.teacher.internship.modules.score.vo.ScorePublishResultVO;
import com.teacher.internship.modules.score.vo.ScoreRecalculateResultVO;
import com.teacher.internship.modules.system.vo.IdNameOptionVO;
import com.teacher.internship.security.auth.SecurityUtils;
import com.teacher.internship.security.jwt.JwtUserPrincipal;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Validated
@RestController
@RequestMapping("/api/v1/score")
public class ScoreController {

    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @GetMapping("/page")
    @PreAuthorize("@permissionService.hasPermission('score:view')")
    public ApiResponse<ScorePageVO> page(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long planId,
                                         @RequestParam(required = false) String studentNo,
                                         @RequestParam(required = false) String studentName,
                                         @RequestParam(required = false) String status) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(scoreService.queryScorePage(
                page,
                size,
                keyword,
                planId,
                studentNo,
                studentName,
                status,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("@permissionService.hasPermission('score:view')")
    public ApiResponse<ScoreDetailVO> detail(@PathVariable("id") Long scoreSheetId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(scoreService.getScoreDetail(
                scoreSheetId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/recalculate/plan/{planId}")
    @PreAuthorize("@permissionService.hasPermission('score:publish')")
    public ApiResponse<ScoreRecalculateResultVO> recalculate(@PathVariable("planId") Long planId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(scoreService.recalculateByPlan(
                planId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/publish/preview/{planId}")
    @PreAuthorize("@permissionService.hasPermission('score:publish')")
    public ApiResponse<ScorePublishPreviewVO> publishPreview(@PathVariable("planId") Long planId,
                                                             @RequestParam(required = false, defaultValue = "1") Integer recalculate) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(scoreService.previewPublish(
                planId,
                recalculate,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/publish/plan/{planId}")
    @PreAuthorize("@permissionService.hasPermission('score:publish')")
    public ApiResponse<ScorePublishResultVO> publish(@PathVariable("planId") Long planId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(scoreService.publishByPlan(
                planId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/adjust/{id}")
    @PreAuthorize("@permissionService.hasPermission('score:adjust')")
    public ApiResponse<ScoreDetailVO> adjust(@PathVariable("id") Long scoreSheetId,
                                             @Valid @RequestBody ScoreAdjustRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(scoreService.adjustScore(
                scoreSheetId,
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/student/page")
    @PreAuthorize("@permissionService.hasPermission('score:view')")
    public ApiResponse<ScorePageVO> studentPage(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Long planId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(scoreService.queryStudentPublishedPage(
                page,
                size,
                keyword,
                planId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/plan-options")
    @PreAuthorize("@permissionService.hasPermission('score:view')")
    public ApiResponse<java.util.List<IdNameOptionVO>> visiblePlanOptions() {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(scoreService.queryVisiblePlanOptions(
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/export/excel")
    @PreAuthorize("@permissionService.hasPermission('score:view')")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) Long planId,
                                              @RequestParam(required = false) String status) {
        JwtUserPrincipal principal = requirePrincipal();
        byte[] content = scoreService.exportExcel(
                planId,
                status,
                principal.getUserId(),
                principal.getRoleCode()
        );

        String fileName = "成绩单导出-" + FILE_TIME.format(LocalDateTime.now()) + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(content);
    }

    @GetMapping("/export/pdf/{id}")
    @PreAuthorize("@permissionService.hasPermission('score:view')")
    public ResponseEntity<byte[]> exportPdf(@PathVariable("id") Long scoreSheetId) {
        JwtUserPrincipal principal = requirePrincipal();
        byte[] content = scoreService.exportPdf(
                scoreSheetId,
                principal.getUserId(),
                principal.getRoleCode()
        );

        String fileName = "成绩单-" + scoreSheetId + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(content);
    }

    private JwtUserPrincipal requirePrincipal() {
        JwtUserPrincipal principal = SecurityUtils.currentPrincipal();
        if (principal == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        return principal;
    }
}
