package com.teacher.internship.modules.material.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.material.dto.MaterialResubmitOpenRequest;
import com.teacher.internship.modules.material.service.MaterialService;
import com.teacher.internship.modules.material.vo.MaterialDocxPreviewVO;
import com.teacher.internship.modules.material.vo.MaterialPageVO;
import com.teacher.internship.modules.material.vo.MaterialVersionVO;
import com.teacher.internship.security.auth.SecurityUtils;
import com.teacher.internship.security.jwt.JwtUserPrincipal;
import org.springframework.core.io.Resource;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/material")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping("/student/page")
    @PreAuthorize("@permissionService.hasPermission('material:view')")
    public ApiResponse<MaterialPageVO> studentPage(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Long planId,
                                                   @RequestParam(required = false) String materialStatus) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(materialService.queryStudentPage(
                page,
                size,
                keyword,
                planId,
                materialStatus,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping(value = "/student/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permissionService.hasPermission('material:upload')")
    public ApiResponse<MaterialVersionVO> studentUpload(@RequestParam("materialId") Long materialId,
                                                        @RequestParam(value = "submitRemark", required = false) String submitRemark,
                                                        @RequestPart("file") MultipartFile file) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(materialService.uploadMaterial(
                materialId,
                file,
                submitRemark,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/teacher/page")
    @PreAuthorize("@permissionService.hasPermission('material:view')")
    public ApiResponse<MaterialPageVO> teacherPage(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Long planId,
                                                   @RequestParam(required = false) String studentNo,
                                                   @RequestParam(required = false) String studentName,
                                                   @RequestParam(required = false) String materialStatus) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(materialService.queryTeacherPage(
                page,
                size,
                keyword,
                planId,
                studentNo,
                studentName,
                materialStatus,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/admin/page")
    @PreAuthorize("@permissionService.hasPermission('material:view')")
    public ApiResponse<MaterialPageVO> adminPage(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Long planId,
                                                 @RequestParam(required = false) String studentNo,
                                                 @RequestParam(required = false) String studentName,
                                                 @RequestParam(required = false) String materialStatus) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(materialService.queryAdminPage(
                page,
                size,
                keyword,
                planId,
                studentNo,
                studentName,
                materialStatus,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @PostMapping("/admin/resubmit/open")
    @PreAuthorize("@permissionService.hasPermission('material:manage')")
    public ApiResponse<Void> openResubmit(@Valid @RequestBody MaterialResubmitOpenRequest request) {
        JwtUserPrincipal principal = requirePrincipal();
        materialService.openLateSubmit(request, principal.getUserId(), principal.getRoleCode());
        return ApiResponse.success("Late submit opened", null);
    }

    @PostMapping("/admin/resubmit/close/{materialId}")
    @PreAuthorize("@permissionService.hasPermission('material:manage')")
    public ApiResponse<Void> closeResubmit(@PathVariable("materialId") Long materialId) {
        JwtUserPrincipal principal = requirePrincipal();
        materialService.closeLateSubmit(materialId, principal.getUserId(), principal.getRoleCode());
        return ApiResponse.success("Late submit closed", null);
    }

    @GetMapping("/version/list/{materialId}")
    @PreAuthorize("@permissionService.hasPermission('material:view')")
    public ApiResponse<List<MaterialVersionVO>> versionList(@PathVariable("materialId") Long materialId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(materialService.queryVersionHistory(
                materialId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/file/preview/{versionId}")
    @PreAuthorize("@permissionService.hasPermission('material:view')")
    public ResponseEntity<Resource> preview(@PathVariable("versionId") Long versionId) {
        JwtUserPrincipal principal = requirePrincipal();
        return materialService.previewMaterialFile(versionId, principal.getUserId(), principal.getRoleCode());
    }

    @GetMapping("/file/docx-preview/{versionId}")
    @PreAuthorize("@permissionService.hasPermission('material:view')")
    public ApiResponse<MaterialDocxPreviewVO> docxPreview(@PathVariable("versionId") Long versionId) {
        JwtUserPrincipal principal = requirePrincipal();
        return ApiResponse.success(materialService.previewDocxMaterialFile(
                versionId,
                principal.getUserId(),
                principal.getRoleCode()
        ));
    }

    @GetMapping("/file/download/{versionId}")
    @PreAuthorize("@permissionService.hasPermission('material:view')")
    public ResponseEntity<Resource> download(@PathVariable("versionId") Long versionId) {
        JwtUserPrincipal principal = requirePrincipal();
        return materialService.downloadMaterialFile(versionId, principal.getUserId(), principal.getRoleCode());
    }

    private JwtUserPrincipal requirePrincipal() {
        JwtUserPrincipal principal = SecurityUtils.currentPrincipal();
        if (principal == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        return principal;
    }
}
