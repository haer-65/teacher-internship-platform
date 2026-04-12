package com.teacher.internship.modules.plan.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.plan.dto.PlanSaveRequest;
import com.teacher.internship.modules.plan.service.PlanService;
import com.teacher.internship.modules.plan.vo.PlanAttachmentVO;
import com.teacher.internship.modules.plan.vo.PlanDetailVO;
import com.teacher.internship.modules.plan.vo.PlanMaterialTypeVO;
import com.teacher.internship.modules.plan.vo.PlanPageVO;
import com.teacher.internship.modules.system.vo.IdNameOptionVO;
import com.teacher.internship.security.auth.SecurityUtils;
import com.teacher.internship.security.jwt.JwtUserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/plan")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/page")
    @PreAuthorize("@permissionService.hasPermission('plan:view')")
    public ApiResponse<PlanPageVO> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String status) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(planService.queryPlanPage(
                page,
                size,
                keyword,
                status,
                principal.getUserId(),
                principal.getRoleCode(),
                false
        ));
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("@permissionService.hasPermission('plan:view')")
    public ApiResponse<PlanDetailVO> detail(@PathVariable("id") Long id) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(planService.getPlanDetail(
                id,
                principal.getUserId(),
                principal.getRoleCode(),
                false
        ));
    }

    @PostMapping("/create")
    @PreAuthorize("@permissionService.hasPermission('plan:publish')")
    public ApiResponse<PlanDetailVO> create(@Valid @RequestBody PlanSaveRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(planService.createDraft(
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("@permissionService.hasPermission('plan:publish')")
    public ApiResponse<PlanDetailVO> update(@PathVariable("id") Long id,
                                            @Valid @RequestBody PlanSaveRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(planService.updateDraft(
                id,
                request,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/publish/{id}")
    @PreAuthorize("@permissionService.hasPermission('plan:publish')")
    public ApiResponse<PlanDetailVO> publish(@PathVariable("id") Long id) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(planService.publishPlan(
                id,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/finish/{id}")
    @PreAuthorize("@permissionService.hasPermission('plan:publish')")
    public ApiResponse<PlanDetailVO> finish(@PathVariable("id") Long id) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(planService.finishPlan(
                id,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/archive/{id}")
    @PreAuthorize("@permissionService.hasPermission('plan:publish')")
    public ApiResponse<PlanDetailVO> archive(@PathVariable("id") Long id) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(planService.archivePlan(
                id,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@permissionService.hasPermission('plan:publish')")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        JwtUserPrincipal principal = requirePrincipal();
        planService.deletePlan(id, principal.getUserId(), principal.getRoleCode());
        return ApiResponse.success("Plan deleted", null);
    }

    @PostMapping(value = "/attachment/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permissionService.hasPermission('plan:publish')")
    public ApiResponse<PlanAttachmentVO> uploadAttachment(@RequestParam("planId") Long planId,
                                                          @RequestPart("file") MultipartFile file) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(planService.uploadAttachment(
                planId,
                file,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @DeleteMapping("/attachment/delete/{id}")
    @PreAuthorize("@permissionService.hasPermission('plan:publish')")
    public ApiResponse<Void> deleteAttachment(@PathVariable("id") Long id) {
        JwtUserPrincipal principal = requirePrincipal();
        planService.deleteAttachment(id, principal.getUserId(), principal.getRoleCode());
        return ApiResponse.success("Attachment deleted", null);
    }

    @GetMapping("/attachment/download/{id}")
    @PreAuthorize("@permissionService.hasPermission('plan:view')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable("id") Long id) {
        JwtUserPrincipal principal = requirePrincipal();
        return planService.downloadAttachment(id, principal.getUserId(), principal.getRoleCode());
    }

    @GetMapping("/material-type/list/{planId}")
    @PreAuthorize("@permissionService.hasPermission('plan:view')")
    public ApiResponse<List<PlanMaterialTypeVO>> materialTypes(@PathVariable("planId") Long planId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(planService.queryMaterialTypeConfig(
                planId,
                principal.getUserId(),
                principal.getRoleCode(),
                false
        ));
    }

    @GetMapping("/base-options")
    @PreAuthorize("@permissionService.hasPermission('plan:publish')")
    public ApiResponse<List<IdNameOptionVO>> baseOptions() {
        requirePrincipal();
        return ApiResponse.success(planService.queryEnabledBaseOptions());
    }

    @GetMapping("/departments")
    @PreAuthorize("@permissionService.hasPermission('plan:publish')")
    public ApiResponse<List<IdNameOptionVO>> departments() {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(planService.queryManageDepartmentOptions(
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/student/page")
    @PreAuthorize("@permissionService.hasPermission('plan:view')")
    public ApiResponse<PlanPageVO> studentPage(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String status) {
        JwtUserPrincipal principal = requireStudentPrincipal();
        return ApiResponse.success(planService.queryPlanPage(
                page,
                size,
                keyword,
                status,
                principal.getUserId(),
                principal.getRoleCode(),
                true
        ));
    }

    @GetMapping("/student/detail/{id}")
    @PreAuthorize("@permissionService.hasPermission('plan:view')")
    public ApiResponse<PlanDetailVO> studentDetail(@PathVariable("id") Long id) {
        JwtUserPrincipal principal = requireStudentPrincipal();
        return ApiResponse.success(planService.getPlanDetail(
                id,
                principal.getUserId(),
                principal.getRoleCode(),
                true
        ));
    }

    private JwtUserPrincipal requirePrincipal() {
        JwtUserPrincipal principal = SecurityUtils.currentPrincipal();
        if (principal == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        return principal;
    }

    private JwtUserPrincipal requireStudentPrincipal() {
        JwtUserPrincipal principal = requirePrincipal();
        if (!"STUDENT".equalsIgnoreCase(principal.getRoleCode())) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色必须是师范生");
        }
        return principal;
    }
}
