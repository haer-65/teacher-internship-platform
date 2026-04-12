package com.teacher.internship.modules.stats.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.stats.dto.StatsQueryRequest;
import com.teacher.internship.modules.stats.service.StatsService;
import com.teacher.internship.modules.stats.vo.StatsDashboardVO;
import com.teacher.internship.modules.stats.vo.StatsDimensionItemVO;
import com.teacher.internship.modules.stats.vo.StatsDimensionPageVO;
import com.teacher.internship.modules.stats.vo.StatsFilterOptionsVO;
import com.teacher.internship.security.auth.SecurityUtils;
import com.teacher.internship.security.jwt.JwtUserPrincipal;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {

    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    @PreAuthorize("@permissionService.hasPermission('stats:view')")
    public ApiResponse<StatsDashboardVO> overview(StatsQueryRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(statsService.queryDashboard(
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/dimension/list")
    @PreAuthorize("@permissionService.hasPermission('stats:view')")
    public ApiResponse<List<StatsDimensionItemVO>> dimensionList(StatsQueryRequest request,
                                                                 @RequestParam(required = false) Integer top) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(statsService.queryDimensionList(
                request,
                top,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/dimension/page")
    @PreAuthorize("@permissionService.hasPermission('stats:view')")
    public ApiResponse<StatsDimensionPageVO> dimensionPage(StatsQueryRequest request,
                                                           @RequestParam(defaultValue = "1") long page,
                                                           @RequestParam(defaultValue = "10") long size) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(statsService.queryDimensionPage(
                request,
                page,
                size,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/options")
    @PreAuthorize("@permissionService.hasPermission('stats:view')")
    public ApiResponse<StatsFilterOptionsVO> options(StatsQueryRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(statsService.queryFilterOptions(
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/export")
    @PreAuthorize("@permissionService.hasPermission('stats:export')")
    public ResponseEntity<byte[]> export(StatsQueryRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        byte[] content = statsService.exportDimensionStats(
                request,
                principal.getUserId(),
                principal.getRoleCode()
        );

        String fileName = "统计分析导出-" + FILE_TIME.format(LocalDateTime.now()) + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
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
