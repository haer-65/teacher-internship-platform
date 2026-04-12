package com.teacher.internship.modules.evaluation.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.evaluation.dto.FinalEvaluationSubmitRequest;
import com.teacher.internship.modules.evaluation.dto.ProcessEvaluationSubmitRequest;
import com.teacher.internship.modules.evaluation.service.EvaluationService;
import com.teacher.internship.modules.evaluation.vo.EvaluationRecordVO;
import com.teacher.internship.modules.evaluation.vo.EvaluationScoreSummaryVO;
import com.teacher.internship.modules.evaluation.vo.FinalEvaluationDetailVO;
import com.teacher.internship.modules.evaluation.vo.ProcessEvaluationDetailVO;
import com.teacher.internship.modules.evaluation.vo.StudentEvaluationPageVO;
import com.teacher.internship.modules.evaluation.vo.TeacherFinalPendingPageVO;
import com.teacher.internship.modules.evaluation.vo.TeacherProcessPendingPageVO;
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

@Validated
@RestController
@RequestMapping("/api/v1/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/teacher/process/page")
    @PreAuthorize("@permissionService.hasPermission('evaluation:view') or @permissionService.hasPermission('score:view')")
    public ApiResponse<TeacherProcessPendingPageVO> teacherProcessPage(@RequestParam(defaultValue = "1") long page,
                                                                       @RequestParam(defaultValue = "10") long size,
                                                                       @RequestParam(required = false) String keyword,
                                                                       @RequestParam(required = false) Long planId,
                                                                       @RequestParam(required = false) String studentNo,
                                                                       @RequestParam(required = false) Integer pendingOnly) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(evaluationService.queryTeacherProcessPage(
                page,
                size,
                keyword,
                planId,
                studentNo,
                pendingOnly,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/teacher/process/detail/{materialVersionId}")
    @PreAuthorize("@permissionService.hasPermission('evaluation:view') or @permissionService.hasPermission('score:view')")
    public ApiResponse<ProcessEvaluationDetailVO> teacherProcessDetail(@PathVariable("materialVersionId") Long materialVersionId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(evaluationService.getTeacherProcessDetail(
                materialVersionId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/teacher/process/submit")
    @PreAuthorize("@permissionService.hasPermission('evaluation:submit')")
    public ApiResponse<EvaluationRecordVO> submitProcess(@Valid @RequestBody ProcessEvaluationSubmitRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(evaluationService.submitProcessEvaluation(
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/teacher/final/page")
    @PreAuthorize("@permissionService.hasPermission('evaluation:view')")
    public ApiResponse<TeacherFinalPendingPageVO> teacherFinalPage(@RequestParam(defaultValue = "1") long page,
                                                                   @RequestParam(defaultValue = "10") long size,
                                                                   @RequestParam(required = false) String keyword,
                                                                   @RequestParam(required = false) Long planId,
                                                                   @RequestParam(required = false) String studentNo,
                                                                   @RequestParam(required = false) Integer pendingOnly) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(evaluationService.queryTeacherFinalPage(
                page,
                size,
                keyword,
                planId,
                studentNo,
                pendingOnly,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/teacher/final/detail/{assignmentId}")
    @PreAuthorize("@permissionService.hasPermission('evaluation:view')")
    public ApiResponse<FinalEvaluationDetailVO> teacherFinalDetail(@PathVariable("assignmentId") Long assignmentId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(evaluationService.getTeacherFinalDetail(
                assignmentId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/teacher/final/submit")
    @PreAuthorize("@permissionService.hasPermission('evaluation:submit')")
    public ApiResponse<EvaluationRecordVO> submitFinal(@Valid @RequestBody FinalEvaluationSubmitRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(evaluationService.submitFinalEvaluation(
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/student/page")
    @PreAuthorize("@permissionService.hasPermission('evaluation:view')")
    public ApiResponse<StudentEvaluationPageVO> studentPage(@RequestParam(defaultValue = "1") long page,
                                                            @RequestParam(defaultValue = "10") long size,
                                                            @RequestParam(required = false) String keyword,
                                                            @RequestParam(required = false) String evaluationType) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(evaluationService.queryStudentEvaluationPage(
                page,
                size,
                keyword,
                evaluationType,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/score/summary/{assignmentId}")
    @PreAuthorize("@permissionService.hasPermission('evaluation:view') or @permissionService.hasPermission('score:view')")
    public ApiResponse<EvaluationScoreSummaryVO> scoreSummary(@PathVariable("assignmentId") Long assignmentId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(evaluationService.queryScoreSummary(
                assignmentId,
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
