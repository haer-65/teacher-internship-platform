package com.teacher.internship.modules.system.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.system.dto.BaseDepartmentSaveRequest;
import com.teacher.internship.modules.system.dto.BaseGradeSaveRequest;
import com.teacher.internship.modules.system.dto.BaseInternshipBaseSaveRequest;
import com.teacher.internship.modules.system.dto.BaseMajorSaveRequest;
import com.teacher.internship.modules.system.dto.SystemStatusUpdateRequest;
import com.teacher.internship.modules.system.service.SystemAuditService;
import com.teacher.internship.modules.system.service.SystemBaseDataService;
import com.teacher.internship.modules.system.vo.BaseDepartmentItemVO;
import com.teacher.internship.modules.system.vo.BaseGradeItemVO;
import com.teacher.internship.modules.system.vo.BaseInternshipBaseItemVO;
import com.teacher.internship.modules.system.vo.BaseMajorItemVO;
import com.teacher.internship.modules.system.vo.IdNameOptionVO;
import com.teacher.internship.modules.system.vo.PageResultVO;
import com.teacher.internship.security.auth.SecurityUtils;
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
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/admin/system/base")
public class SystemBaseDataController {

    private final SystemBaseDataService baseDataService;
    private final SystemAuditService auditService;

    public SystemBaseDataController(SystemBaseDataService baseDataService,
                                    SystemAuditService auditService) {
        this.baseDataService = baseDataService;
        this.auditService = auditService;
    }

    @GetMapping("/department/page")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<PageResultVO<BaseDepartmentItemVO>> departmentPage(@RequestParam(defaultValue = "1") long page,
                                                                          @RequestParam(defaultValue = "10") long size,
                                                                          @RequestParam(required = false) String keyword,
                                                                          @RequestParam(required = false) String status) {
        return ApiResponse.success(baseDataService.queryDepartmentPage(page, size, keyword, status));
    }

    @PostMapping("/department/create")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseDepartmentItemVO> createDepartment(@Valid @RequestBody BaseDepartmentSaveRequest request,
                                                              HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseDepartmentItemVO data = baseDataService.createDepartment(request, operatorId);
        auditService.logSuccess(operatorId, "BASE", "DEPARTMENT_CREATE", "BASE_DEPARTMENT", data.getId(),
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("院系创建成功", data);
    }

    @PutMapping("/department/update/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseDepartmentItemVO> updateDepartment(@PathVariable("id") Long id,
                                                              @Valid @RequestBody BaseDepartmentSaveRequest request,
                                                              HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseDepartmentItemVO data = baseDataService.updateDepartment(id, request, operatorId);
        auditService.logSuccess(operatorId, "BASE", "DEPARTMENT_UPDATE", "BASE_DEPARTMENT", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("院系更新成功", data);
    }

    @PutMapping("/department/status/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseDepartmentItemVO> updateDepartmentStatus(@PathVariable("id") Long id,
                                                                    @Valid @RequestBody SystemStatusUpdateRequest request,
                                                                    HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseDepartmentItemVO data = baseDataService.updateDepartmentStatus(id, request.getStatus(), operatorId);
        auditService.logSuccess(operatorId, "BASE", "DEPARTMENT_STATUS", "BASE_DEPARTMENT", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("院系状态更新成功", data);
    }

    @DeleteMapping("/department/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<Void> deleteDepartment(@PathVariable("id") Long id,
                                              HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        baseDataService.deleteDepartment(id, operatorId);
        auditService.logSuccess(operatorId, "BASE", "DEPARTMENT_DELETE", "BASE_DEPARTMENT", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), null, null);
        return ApiResponse.success("院系删除成功", null);
    }

    @GetMapping("/major/page")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<PageResultVO<BaseMajorItemVO>> majorPage(@RequestParam(defaultValue = "1") long page,
                                                                @RequestParam(defaultValue = "10") long size,
                                                                @RequestParam(required = false) String keyword,
                                                                @RequestParam(required = false) String status,
                                                                @RequestParam(required = false) Long deptId) {
        return ApiResponse.success(baseDataService.queryMajorPage(page, size, keyword, status, deptId));
    }

    @PostMapping("/major/create")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseMajorItemVO> createMajor(@Valid @RequestBody BaseMajorSaveRequest request,
                                                    HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseMajorItemVO data = baseDataService.createMajor(request, operatorId);
        auditService.logSuccess(operatorId, "BASE", "MAJOR_CREATE", "BASE_MAJOR", data.getId(),
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("专业创建成功", data);
    }

    @PutMapping("/major/update/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseMajorItemVO> updateMajor(@PathVariable("id") Long id,
                                                    @Valid @RequestBody BaseMajorSaveRequest request,
                                                    HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseMajorItemVO data = baseDataService.updateMajor(id, request, operatorId);
        auditService.logSuccess(operatorId, "BASE", "MAJOR_UPDATE", "BASE_MAJOR", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("专业更新成功", data);
    }

    @PutMapping("/major/status/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseMajorItemVO> updateMajorStatus(@PathVariable("id") Long id,
                                                          @Valid @RequestBody SystemStatusUpdateRequest request,
                                                          HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseMajorItemVO data = baseDataService.updateMajorStatus(id, request.getStatus(), operatorId);
        auditService.logSuccess(operatorId, "BASE", "MAJOR_STATUS", "BASE_MAJOR", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("专业状态更新成功", data);
    }

    @DeleteMapping("/major/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<Void> deleteMajor(@PathVariable("id") Long id,
                                         HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        baseDataService.deleteMajor(id, operatorId);
        auditService.logSuccess(operatorId, "BASE", "MAJOR_DELETE", "BASE_MAJOR", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), null, null);
        return ApiResponse.success("专业删除成功", null);
    }

    @GetMapping("/grade/page")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<PageResultVO<BaseGradeItemVO>> gradePage(@RequestParam(defaultValue = "1") long page,
                                                                @RequestParam(defaultValue = "10") long size,
                                                                @RequestParam(required = false) String keyword,
                                                                @RequestParam(required = false) String status) {
        return ApiResponse.success(baseDataService.queryGradePage(page, size, keyword, status));
    }

    @PostMapping("/grade/create")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseGradeItemVO> createGrade(@Valid @RequestBody BaseGradeSaveRequest request,
                                                    HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseGradeItemVO data = baseDataService.createGrade(request, operatorId);
        auditService.logSuccess(operatorId, "BASE", "GRADE_CREATE", "BASE_GRADE", data.getId(),
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("年级创建成功", data);
    }

    @PutMapping("/grade/update/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseGradeItemVO> updateGrade(@PathVariable("id") Long id,
                                                    @Valid @RequestBody BaseGradeSaveRequest request,
                                                    HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseGradeItemVO data = baseDataService.updateGrade(id, request, operatorId);
        auditService.logSuccess(operatorId, "BASE", "GRADE_UPDATE", "BASE_GRADE", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("年级更新成功", data);
    }

    @PutMapping("/grade/status/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseGradeItemVO> updateGradeStatus(@PathVariable("id") Long id,
                                                          @Valid @RequestBody SystemStatusUpdateRequest request,
                                                          HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseGradeItemVO data = baseDataService.updateGradeStatus(id, request.getStatus(), operatorId);
        auditService.logSuccess(operatorId, "BASE", "GRADE_STATUS", "BASE_GRADE", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("年级状态更新成功", data);
    }

    @DeleteMapping("/grade/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<Void> deleteGrade(@PathVariable("id") Long id,
                                         HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        baseDataService.deleteGrade(id, operatorId);
        auditService.logSuccess(operatorId, "BASE", "GRADE_DELETE", "BASE_GRADE", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), null, null);
        return ApiResponse.success("年级删除成功", null);
    }

    @GetMapping("/internship-base/page")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<PageResultVO<BaseInternshipBaseItemVO>> internshipBasePage(@RequestParam(defaultValue = "1") long page,
                                                                                   @RequestParam(defaultValue = "10") long size,
                                                                                   @RequestParam(required = false) String keyword,
                                                                                   @RequestParam(required = false) String status) {
        return ApiResponse.success(baseDataService.queryInternshipBasePage(page, size, keyword, status));
    }

    @PostMapping("/internship-base/create")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseInternshipBaseItemVO> createInternshipBase(@Valid @RequestBody BaseInternshipBaseSaveRequest request,
                                                                      HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseInternshipBaseItemVO data = baseDataService.createInternshipBase(request, operatorId);
        auditService.logSuccess(operatorId, "BASE", "INTERNSHIP_BASE_CREATE", "BASE_INTERNSHIP_BASE", data.getId(),
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("实习基地创建成功", data);
    }

    @PutMapping("/internship-base/update/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseInternshipBaseItemVO> updateInternshipBase(@PathVariable("id") Long id,
                                                                      @Valid @RequestBody BaseInternshipBaseSaveRequest request,
                                                                      HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseInternshipBaseItemVO data = baseDataService.updateInternshipBase(id, request, operatorId);
        auditService.logSuccess(operatorId, "BASE", "INTERNSHIP_BASE_UPDATE", "BASE_INTERNSHIP_BASE", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("实习基地更新成功", data);
    }

    @PutMapping("/internship-base/status/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<BaseInternshipBaseItemVO> updateInternshipBaseStatus(@PathVariable("id") Long id,
                                                                            @Valid @RequestBody SystemStatusUpdateRequest request,
                                                                            HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        BaseInternshipBaseItemVO data = baseDataService.updateInternshipBaseStatus(id, request.getStatus(), operatorId);
        auditService.logSuccess(operatorId, "BASE", "INTERNSHIP_BASE_STATUS", "BASE_INTERNSHIP_BASE", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("实习基地状态更新成功", data);
    }

    @DeleteMapping("/internship-base/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<Void> deleteInternshipBase(@PathVariable("id") Long id,
                                                  HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        baseDataService.deleteInternshipBase(id, operatorId);
        auditService.logSuccess(operatorId, "BASE", "INTERNSHIP_BASE_DELETE", "BASE_INTERNSHIP_BASE", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), null, null);
        return ApiResponse.success("实习基地删除成功", null);
    }

    @GetMapping("/department/options")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<List<IdNameOptionVO>> departmentOptions() {
        return ApiResponse.success(baseDataService.queryDepartmentOptions());
    }

    private Long requireOperatorId() {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        return userId;
    }

    private String extractIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
