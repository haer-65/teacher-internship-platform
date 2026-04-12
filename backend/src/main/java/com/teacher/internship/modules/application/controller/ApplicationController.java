package com.teacher.internship.modules.application.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.application.dto.ApplicationReviewRequest;
import com.teacher.internship.modules.application.dto.AssignmentAdjustRequest;
import com.teacher.internship.modules.application.dto.AssignmentManualRequest;
import com.teacher.internship.modules.application.dto.StudentApplicationSaveRequest;
import com.teacher.internship.modules.application.service.ApplicationService;
import com.teacher.internship.modules.application.vo.AdminApplicationItemVO;
import com.teacher.internship.modules.application.vo.AdminApplicationPageVO;
import com.teacher.internship.modules.application.vo.AssignmentCandidatePageVO;
import com.teacher.internship.modules.application.vo.AssignmentImportResultVO;
import com.teacher.internship.modules.application.vo.AssignmentItemVO;
import com.teacher.internship.modules.application.vo.AssignmentOptionVO;
import com.teacher.internship.modules.application.vo.AssignmentPageVO;
import com.teacher.internship.modules.application.vo.StudentApplicationItemVO;
import com.teacher.internship.modules.application.vo.StudentApplicationPageVO;
import com.teacher.internship.modules.application.vo.StudentAvailablePlanVO;
import com.teacher.internship.security.auth.SecurityUtils;
import com.teacher.internship.security.jwt.JwtUserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/application")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/student/available-plans")
    @PreAuthorize("@permissionService.hasPermission('application:view')")
    public ApiResponse<List<StudentAvailablePlanVO>> availablePlans() {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.queryStudentAvailablePlans(
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/student/page")
    @PreAuthorize("@permissionService.hasPermission('application:view')")
    public ApiResponse<StudentApplicationPageVO> studentPage(@RequestParam(defaultValue = "1") long page,
                                                             @RequestParam(defaultValue = "10") long size,
                                                             @RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) String applicationStatus) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.queryStudentApplicationPage(
                page,
                size,
                keyword,
                applicationStatus,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/student/detail/{id}")
    @PreAuthorize("@permissionService.hasPermission('application:view')")
    public ApiResponse<StudentApplicationItemVO> studentDetail(@PathVariable("id") Long applicationId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.getStudentApplicationDetail(
                applicationId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/student/submit")
    @PreAuthorize("@permissionService.hasPermission('application:view')")
    public ApiResponse<StudentApplicationItemVO> studentSubmit(@Valid @RequestBody StudentApplicationSaveRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.submitStudentApplication(
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PutMapping("/student/update/{id}")
    @PreAuthorize("@permissionService.hasPermission('application:view')")
    public ApiResponse<StudentApplicationItemVO> studentUpdate(@PathVariable("id") Long applicationId,
                                                               @Valid @RequestBody StudentApplicationSaveRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.updateStudentApplication(
                applicationId,
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/student/withdraw/{id}")
    @PreAuthorize("@permissionService.hasPermission('application:view')")
    public ApiResponse<Void> studentWithdraw(@PathVariable("id") Long applicationId) {
        JwtUserPrincipal principal = requirePrincipal();
        applicationService.withdrawStudentApplication(
                applicationId,
                principal.getUserId(),
                principal.getRoleCode()
        );
        return ApiResponse.success("申请已撤回", null);
    }

    @GetMapping("/admin/page")
    @PreAuthorize("@permissionService.hasPermission('application:view')")
    public ApiResponse<AdminApplicationPageVO> adminPage(@RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "10") long size,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) Long planId,
                                                         @RequestParam(required = false) String applicationStatus,
                                                         @RequestParam(required = false) String studentNo,
                                                         @RequestParam(required = false) String studentName) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.queryAdminApplicationPage(
                page,
                size,
                keyword,
                planId,
                applicationStatus,
                studentNo,
                studentName,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/admin/review/{id}")
    @PreAuthorize("@permissionService.hasPermission('application:assign')")
    public ApiResponse<AdminApplicationItemVO> review(@PathVariable("id") Long applicationId,
                                                      @Valid @RequestBody ApplicationReviewRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.reviewApplication(
                applicationId,
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/assignment/candidate/page")
    @PreAuthorize("@permissionService.hasPermission('application:assign')")
    public ApiResponse<AssignmentCandidatePageVO> assignmentCandidatePage(@RequestParam(defaultValue = "1") long page,
                                                                          @RequestParam(defaultValue = "10") long size,
                                                                          @RequestParam(required = false) String keyword,
                                                                          @RequestParam(required = false) Long planId,
                                                                          @RequestParam(required = false) String studentNo) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.queryAssignmentCandidatePage(
                page,
                size,
                keyword,
                planId,
                studentNo,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/assignment/options")
    @PreAuthorize("@permissionService.hasPermission('application:assign')")
    public ApiResponse<AssignmentOptionVO> assignmentOptions(@RequestParam(required = false) Long planId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.queryAssignmentOptions(
                principal.getUserId(),
                principal.getRoleCode(),
                planId
        ));
    }

    @GetMapping("/assignment/page")
    @PreAuthorize("@permissionService.hasPermission('application:view')")
    public ApiResponse<AssignmentPageVO> assignmentPage(@RequestParam(defaultValue = "1") long page,
                                                        @RequestParam(defaultValue = "10") long size,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Long planId,
                                                        @RequestParam(required = false) String studentNo,
                                                        @RequestParam(required = false) Integer isCurrent) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.queryAssignmentPage(
                page,
                size,
                keyword,
                planId,
                studentNo,
                isCurrent,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/assignment/detail/{id}")
    @PreAuthorize("@permissionService.hasPermission('application:assign')")
    public ApiResponse<AssignmentItemVO> assignmentDetail(@PathVariable("id") Long assignmentId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.getAssignmentDetail(
                assignmentId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/assignment/my/page")
    @PreAuthorize("@permissionService.hasPermission('application:view')")
    public ApiResponse<AssignmentPageVO> myAssignmentPage(@RequestParam(defaultValue = "1") long page,
                                                          @RequestParam(defaultValue = "10") long size,
                                                          @RequestParam(required = false) String keyword) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.queryMyAssignmentPage(
                page,
                size,
                keyword,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/assignment/manual")
    @PreAuthorize("@permissionService.hasPermission('application:assign')")
    public ApiResponse<AssignmentItemVO> manualAssign(@Valid @RequestBody AssignmentManualRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.manualAssign(
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/assignment/adjust/{id}")
    @PreAuthorize("@permissionService.hasPermission('application:assign')")
    public ApiResponse<AssignmentItemVO> adjustAssign(@PathVariable("id") Long assignmentId,
                                                      @Valid @RequestBody AssignmentAdjustRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.adjustAssignment(
                assignmentId,
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping(value = "/assignment/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permissionService.hasPermission('application:assign')")
    public ApiResponse<AssignmentImportResultVO> importAssignment(@RequestPart("file") MultipartFile file) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(applicationService.importAssignments(
                file,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    private JwtUserPrincipal requirePrincipal() {
        JwtUserPrincipal principal = SecurityUtils.currentPrincipal();
        if (principal == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        return principal;
    }
}
