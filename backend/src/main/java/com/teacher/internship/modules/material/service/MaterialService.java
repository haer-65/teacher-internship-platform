package com.teacher.internship.modules.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.config.FileStorageProperties;
import com.teacher.internship.modules.application.entity.BizAssignment;
import com.teacher.internship.modules.application.mapper.BizAssignmentMapper;
import com.teacher.internship.modules.material.dto.MaterialResubmitOpenRequest;
import com.teacher.internship.modules.material.entity.BizMaterial;
import com.teacher.internship.modules.material.entity.BizMaterialResubmitControl;
import com.teacher.internship.modules.material.entity.BizMaterialVersion;
import com.teacher.internship.modules.material.mapper.BizMaterialMapper;
import com.teacher.internship.modules.material.mapper.BizMaterialResubmitControlMapper;
import com.teacher.internship.modules.material.mapper.BizMaterialVersionMapper;
import com.teacher.internship.modules.material.vo.MaterialDocxPreviewVO;
import com.teacher.internship.modules.material.vo.MaterialListItemVO;
import com.teacher.internship.modules.material.vo.MaterialPageVO;
import com.teacher.internship.modules.material.vo.MaterialVersionVO;
import com.teacher.internship.modules.plan.entity.BizInternshipPlan;
import com.teacher.internship.modules.plan.entity.BizMaterialType;
import com.teacher.internship.modules.plan.mapper.BizInternshipPlanMapper;
import com.teacher.internship.modules.plan.mapper.BizMaterialTypeMapper;
import com.teacher.internship.modules.notice.service.NoticeTriggerService;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import com.teacher.internship.modules.system.service.SystemParamService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaterialService {

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_INNER_TEACHER = "INNER_TEACHER";
    private static final String ROLE_BASE_TEACHER = "BASE_TEACHER";
    private static final String ROLE_DEPT_ADMIN = "DEPT_ADMIN";

    private static final String STATUS_ENABLED = "ENABLED";
    private static final String STATUS_NOT_SUBMITTED = "NOT_SUBMITTED";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_OVERDUE = "OVERDUE";

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 200;
    private static final long MB = 1024L * 1024L;
    private static final String PARAM_FILE_MAX_SIZE_MB = "SYSTEM_FILE_MAX_SIZE_MB";

    private static final Set<String> ALLOWED_FILE_EXTENSIONS;
    private static final Set<String> PREVIEWABLE_FILE_EXTENSIONS;

    static {
        Set<String> allowed = new HashSet<>();
        allowed.add("doc");
        allowed.add("docx");
        allowed.add("pdf");
        allowed.add("jpg");
        allowed.add("jpeg");
        allowed.add("png");
        allowed.add("mp4");
        ALLOWED_FILE_EXTENSIONS = Collections.unmodifiableSet(allowed);

        Set<String> previewable = new HashSet<>();
        previewable.add("pdf");
        previewable.add("jpg");
        previewable.add("jpeg");
        previewable.add("png");
        previewable.add("mp4");
        previewable.add("docx");
        PREVIEWABLE_FILE_EXTENSIONS = Collections.unmodifiableSet(previewable);
    }

    private final BizMaterialMapper materialMapper;
    private final BizMaterialVersionMapper materialVersionMapper;
    private final BizMaterialResubmitControlMapper resubmitControlMapper;
    private final BizAssignmentMapper assignmentMapper;
    private final BizMaterialTypeMapper materialTypeMapper;
    private final BizInternshipPlanMapper planMapper;
    private final NoticeTriggerService noticeTriggerService;
    private final SysUserMapper userMapper;
    private final FileStorageProperties fileStorageProperties;
    private final SystemParamService paramService;

    public MaterialService(BizMaterialMapper materialMapper,
                           BizMaterialVersionMapper materialVersionMapper,
                           BizMaterialResubmitControlMapper resubmitControlMapper,
                           BizAssignmentMapper assignmentMapper,
                           BizMaterialTypeMapper materialTypeMapper,
                           BizInternshipPlanMapper planMapper,
                           NoticeTriggerService noticeTriggerService,
                           SysUserMapper userMapper,
                           FileStorageProperties fileStorageProperties,
                           SystemParamService paramService) {
        this.materialMapper = materialMapper;
        this.materialVersionMapper = materialVersionMapper;
        this.resubmitControlMapper = resubmitControlMapper;
        this.assignmentMapper = assignmentMapper;
        this.materialTypeMapper = materialTypeMapper;
        this.planMapper = planMapper;
        this.noticeTriggerService = noticeTriggerService;
        this.userMapper = userMapper;
        this.fileStorageProperties = fileStorageProperties;
        this.paramService = paramService;
    }

    public MaterialPageVO queryStudentPage(long page,
                                           long size,
                                           String keyword,
                                           Long planId,
                                           String materialStatus,
                                           Long userId,
                                           String roleCode) {
        SysUser operator = requireUser(userId);
        String normalizedRole = normalizeCode(roleCode);
        if (!ROLE_STUDENT.equals(normalizedRole)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色必须是师范生");
        }
        List<BizAssignment> assignments = queryCurrentAssignmentsByStudent(userId);
        return buildMaterialPage(assignments, operator, normalizedRole, page, size, keyword, planId, null, null, materialStatus);
    }

    public MaterialPageVO queryTeacherPage(long page,
                                           long size,
                                           String keyword,
                                           Long planId,
                                           String studentNo,
                                           String studentName,
                                           String materialStatus,
                                           Long userId,
                                           String roleCode) {
        SysUser operator = requireUser(userId);
        String normalizedRole = normalizeCode(roleCode);
        List<BizAssignment> assignments = queryTeacherAssignments(userId, normalizedRole);
        return buildMaterialPage(assignments, operator, normalizedRole, page, size, keyword, planId, studentNo, studentName, materialStatus);
    }

    public MaterialPageVO queryAdminPage(long page,
                                         long size,
                                         String keyword,
                                         Long planId,
                                         String studentNo,
                                         String studentName,
                                         String materialStatus,
                                         Long userId,
                                         String roleCode) {
        SysUser operator = requireUser(userId);
        String normalizedRole = normalizeCode(roleCode);
        List<BizAssignment> assignments = queryAdminAssignments(operator, normalizedRole);
        return buildMaterialPage(assignments, operator, normalizedRole, page, size, keyword, planId, studentNo, studentName, materialStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    public MaterialVersionVO uploadMaterial(Long materialId,
                                            MultipartFile file,
                                            String submitRemark,
                                            Long userId,
                                            String roleCode) {
        if (materialId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料标识不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "上传文件不能为空");
        }

        String normalizedRole = normalizeCode(roleCode);
        if (!ROLE_STUDENT.equals(normalizedRole)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "只有学生可以提交材料");
        }

        SysUser operator = requireUser(userId);
        BizMaterial material = requireMaterial(materialId);
        if (!Objects.equals(material.getStudentId(), userId)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权提交该材料");
        }

        BizAssignment assignment = requireAssignment(material.getAssignmentId());
        BizInternshipPlan plan = requirePlan(assignment.getPlanId());
        if (assignment.getIsCurrent() == null || assignment.getIsCurrent() != 1) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有当前分配版本才支持提交材料");
        }

        BizMaterialType materialType = requireMaterialType(material.getMaterialTypeId());
        if (!STATUS_ENABLED.equals(normalizeCode(materialType.getStatus()))) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料类型已禁用");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "不支持的文件类型：" + extension);
        }

        int configuredMb = paramService.getIntValue(PARAM_FILE_MAX_SIZE_MB, fileStorageProperties.getMaxFileSizeMb());
        long maxSizeMb = Math.max(1, Math.min(configuredMb, 200));
        long maxBytes = maxSizeMb * MB;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "文件大小超出限制（不得超过 " + maxSizeMb + "MB）");
        }

        LocalDateTime now = LocalDateTime.now();
        BizMaterialResubmitControl control = findResubmitControl(material.getId());
        boolean lateSubmitOpen = isLateSubmitOpen(control, now);
        if (materialType.getDeadlineTime() != null && now.isAfter(materialType.getDeadlineTime()) && !lateSubmitOpen) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "提交截止时间已过");
        }

        int maxSubmitCount = materialType.getMaxSubmitCount() == null || materialType.getMaxSubmitCount() <= 0
                ? 1
                : materialType.getMaxSubmitCount();
        int currentVersion = material.getLatestVersionNo() == null ? 0 : material.getLatestVersionNo();
        if (currentVersion >= maxSubmitCount) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "已达到最大提交次数");
        }

        int nextVersionNo = currentVersion + 1;
        String originalFileName = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename().trim()
                : "material." + extension;

        String storedName = "v" + nextVersionNo + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String typeCode = normalizeCode(materialType.getTypeCode());
        String relativePath = Paths.get("material",
                        String.valueOf(assignment.getPlanId()),
                        String.valueOf(assignment.getId()),
                        typeCode,
                        String.valueOf(material.getId()),
                        storedName)
                .toString()
                .replace("\\", "/");

        Path targetPath = resolveSafeTargetPath(relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
        } catch (IOException ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "材料文件保存失败");
        }

        LocalDateTime submitTime = LocalDateTime.now();
        BizMaterialVersion version = new BizMaterialVersion();
        try {
            materialVersionMapper.update(null, new LambdaUpdateWrapper<BizMaterialVersion>()
                    .eq(BizMaterialVersion::getMaterialId, material.getId())
                    .eq(BizMaterialVersion::getDeleted, 0L)
                    .eq(BizMaterialVersion::getIsCurrent, 1)
                    .set(BizMaterialVersion::getIsCurrent, 0)
                    .set(BizMaterialVersion::getUpdatedBy, userId)
                    .set(BizMaterialVersion::getUpdatedTime, submitTime));

            version.setMaterialId(material.getId());
            version.setVersionNo(nextVersionNo);
            version.setFileName(originalFileName);
            version.setFilePath(relativePath);
            version.setFileSize(file.getSize());
            version.setFileExt(extension);
            version.setMimeType(resolveMimeType(file.getContentType(), extension));
            version.setSubmitRemark(trimToNull(submitRemark));
            version.setSubmittedBy(userId);
            version.setSubmittedTime(submitTime);
            version.setIsCurrent(1);
            version.setCreatedBy(userId);
            version.setUpdatedBy(userId);
            version.setDeleted(0L);
            materialVersionMapper.insert(version);

            int updated = materialMapper.update(null, new LambdaUpdateWrapper<BizMaterial>()
                    .eq(BizMaterial::getId, material.getId())
                    .eq(BizMaterial::getDeleted, 0L)
                    .eq(BizMaterial::getLatestVersionNo, currentVersion)
                    .set(BizMaterial::getLatestVersionNo, nextVersionNo)
                    .set(BizMaterial::getMaterialStatus, STATUS_SUBMITTED)
                    .set(BizMaterial::getLastSubmitTime, submitTime)
                    .set(BizMaterial::getUpdatedBy, userId)
                    .set(BizMaterial::getUpdatedTime, submitTime));
            if (updated <= 0) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料数据已变化，请刷新后重试");
            }
        } catch (DuplicateKeyException ex) {
            cleanupStoredFile(targetPath);
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料版本冲突，请重新上传");
        } catch (BusinessException ex) {
            cleanupStoredFile(targetPath);
            throw ex;
        } catch (RuntimeException ex) {
            cleanupStoredFile(targetPath);
            throw ex;
        }
        Map<Long, SysUser> submitterMap = new HashMap<>();
        submitterMap.put(operator.getId(), operator);
        noticeTriggerService.notifyMaterialSubmitted(material, materialType, plan, operator, assignment, userId);
        return toMaterialVersionVO(version, submitterMap);
    }

    public List<MaterialVersionVO> queryVersionHistory(Long materialId,
                                                       Long userId,
                                                       String roleCode) {
        BizMaterial material = requireMaterial(materialId);
        BizAssignment assignment = requireAssignment(material.getAssignmentId());
        BizInternshipPlan plan = requirePlan(assignment.getPlanId());
        SysUser operator = requireUser(userId);
        ensureMaterialReadAccess(material, assignment, plan, operator, normalizeCode(roleCode));

        List<BizMaterialVersion> versions = materialVersionMapper.selectList(new LambdaQueryWrapper<BizMaterialVersion>()
                .eq(BizMaterialVersion::getMaterialId, materialId)
                .eq(BizMaterialVersion::getDeleted, 0L)
                .orderByDesc(BizMaterialVersion::getVersionNo)
                .orderByDesc(BizMaterialVersion::getId));

        Set<Long> submitterIds = versions.stream()
                .map(BizMaterialVersion::getSubmittedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUser> submitterMap = queryUserMap(submitterIds);

        return versions.stream()
                .map(item -> toMaterialVersionVO(item, submitterMap))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void openLateSubmit(MaterialResubmitOpenRequest request,
                               Long userId,
                               String roleCode) {
        BizMaterial material = requireMaterial(request.getMaterialId());
        BizAssignment assignment = requireAssignment(material.getAssignmentId());
        BizInternshipPlan plan = requirePlan(assignment.getPlanId());

        SysUser operator = requireUser(userId);
        ensureAdminManageAccess(plan, operator, normalizeCode(roleCode));

        LocalDateTime now = LocalDateTime.now();
        if (request.getOpenUntil() != null && request.getOpenUntil().isBefore(now)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "补交开放截止时间不能早于当前时间");
        }

        BizMaterialResubmitControl existing = findResubmitControl(material.getId());
        if (existing == null) {
            BizMaterialResubmitControl entity = new BizMaterialResubmitControl();
            entity.setMaterialId(material.getId());
            entity.setOpenFlag(1);
            entity.setOpenUntil(request.getOpenUntil());
            entity.setOpenReason(trimToNull(request.getOpenReason()));
            entity.setOpenedBy(userId);
            entity.setOpenedTime(now);
            entity.setClosedBy(null);
            entity.setClosedTime(null);
            entity.setCreatedBy(userId);
            entity.setUpdatedBy(userId);
            entity.setDeleted(0L);
            resubmitControlMapper.insert(entity);
            return;
        }

        existing.setOpenFlag(1);
        existing.setOpenUntil(request.getOpenUntil());
        existing.setOpenReason(trimToNull(request.getOpenReason()));
        existing.setOpenedBy(userId);
        existing.setOpenedTime(now);
        existing.setClosedBy(null);
        existing.setClosedTime(null);
        existing.setUpdatedBy(userId);
        existing.setUpdatedTime(now);
        resubmitControlMapper.updateById(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void closeLateSubmit(Long materialId,
                                Long userId,
                                String roleCode) {
        BizMaterial material = requireMaterial(materialId);
        BizAssignment assignment = requireAssignment(material.getAssignmentId());
        BizInternshipPlan plan = requirePlan(assignment.getPlanId());

        SysUser operator = requireUser(userId);
        ensureAdminManageAccess(plan, operator, normalizeCode(roleCode));

        BizMaterialResubmitControl existing = findResubmitControl(material.getId());
        if (existing == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        existing.setOpenFlag(0);
        existing.setClosedBy(userId);
        existing.setClosedTime(now);
        existing.setUpdatedBy(userId);
        existing.setUpdatedTime(now);
        resubmitControlMapper.updateById(existing);
    }

    public ResponseEntity<Resource> previewMaterialFile(Long versionId,
                                                        Long userId,
                                                        String roleCode) {
        BizMaterialVersion version = requireMaterialVersion(versionId);
        String extension = resolveExtension(version.getFileName(), version.getFileExt());
        if (!PREVIEWABLE_FILE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "当前文件类型不支持在线预览");
        }
        ensureVersionReadAccess(version, userId, roleCode);
        if ("docx".equals(extension)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "DOCX 文档请使用在线预览页面查看");
        }
        return buildFileResponse(version, true);
    }

    public MaterialDocxPreviewVO previewDocxMaterialFile(Long versionId,
                                                         Long userId,
                                                         String roleCode) {
        BizMaterialVersion version = requireMaterialVersion(versionId);
        String extension = resolveExtension(version.getFileName(), version.getFileExt());
        if (!"docx".equals(extension)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "当前版本不是 DOCX 文档");
        }
        ensureVersionReadAccess(version, userId, roleCode);

        Path filePath = resolveSafeStoredFile(version.getFilePath());
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "材料文件不存在");
        }

        try (InputStream inputStream = Files.newInputStream(filePath);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            MaterialDocxPreviewVO vo = new MaterialDocxPreviewVO();
            vo.setVersionId(version.getId());
            vo.setMaterialId(version.getMaterialId());
            vo.setVersionNo(version.getVersionNo());
            vo.setFileName(version.getFileName());
            vo.setHtmlContent(buildDocxPreviewHtml(version, document));
            return vo;
        } catch (IOException ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "DOCX 在线预览生成失败");
        }
    }

    public ResponseEntity<Resource> downloadMaterialFile(Long versionId,
                                                         Long userId,
                                                         String roleCode) {
        BizMaterialVersion version = requireMaterialVersion(versionId);
        ensureVersionReadAccess(version, userId, roleCode);
        return buildFileResponse(version, false);
    }

    private MaterialPageVO buildMaterialPage(List<BizAssignment> assignments,
                                             SysUser operator,
                                             String roleCode,
                                             long page,
                                             long size,
                                             String keyword,
                                             Long planId,
                                             String studentNo,
                                             String studentName,
                                             String materialStatus) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);

        if (CollectionUtils.isEmpty(assignments)) {
            return emptyPage(safePage, safeSize);
        }

        Set<Long> planIds = assignments.stream()
                .map(BizAssignment::getPlanId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);

        if (ROLE_DEPT_ADMIN.equals(roleCode)) {
            assignments = assignments.stream()
                    .filter(item -> {
                        BizInternshipPlan plan = planMap.get(item.getPlanId());
                        return plan != null && Objects.equals(plan.getDeptId(), operator.getDeptId());
                    })
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(assignments)) {
                return emptyPage(safePage, safeSize);
            }
        }

        Set<Long> assignmentIds = assignments.stream()
                .map(BizAssignment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, BizAssignment> assignmentMap = assignments.stream()
                .collect(Collectors.toMap(BizAssignment::getId, item -> item, (left, right) -> left));

        List<BizMaterialType> materialTypeEntities = materialTypeMapper.selectList(new LambdaQueryWrapper<BizMaterialType>()
                .in(BizMaterialType::getPlanId, planIds)
                .eq(BizMaterialType::getStatus, STATUS_ENABLED)
                .eq(BizMaterialType::getDeleted, 0L)
                .orderByAsc(BizMaterialType::getDeadlineTime)
                .orderByAsc(BizMaterialType::getId));

        if (CollectionUtils.isEmpty(materialTypeEntities)) {
            return emptyPage(safePage, safeSize);
        }

        Map<Long, List<BizMaterialType>> materialTypesByPlan = materialTypeEntities.stream()
                .collect(Collectors.groupingBy(BizMaterialType::getPlanId));

        ensureMaterialMasterRecords(assignments, materialTypesByPlan, operator.getId());

        List<BizMaterial> materials = materialMapper.selectList(new LambdaQueryWrapper<BizMaterial>()
                .in(BizMaterial::getAssignmentId, assignmentIds)
                .eq(BizMaterial::getDeleted, 0L));

        if (CollectionUtils.isEmpty(materials)) {
            return emptyPage(safePage, safeSize);
        }

        Map<Long, BizMaterialType> materialTypeMap = materialTypeEntities.stream()
                .collect(Collectors.toMap(BizMaterialType::getId, item -> item, (left, right) -> left));

        Set<Long> materialIds = materials.stream().map(BizMaterial::getId).collect(Collectors.toSet());

        Map<Long, BizMaterialVersion> currentVersionMap = materialVersionMapper.selectList(new LambdaQueryWrapper<BizMaterialVersion>()
                        .in(BizMaterialVersion::getMaterialId, materialIds)
                        .eq(BizMaterialVersion::getIsCurrent, 1)
                        .eq(BizMaterialVersion::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BizMaterialVersion::getMaterialId, item -> item, (left, right) -> left));

        Map<Long, BizMaterialResubmitControl> resubmitControlMap = resubmitControlMapper.selectList(new LambdaQueryWrapper<BizMaterialResubmitControl>()
                        .in(BizMaterialResubmitControl::getMaterialId, materialIds)
                        .eq(BizMaterialResubmitControl::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BizMaterialResubmitControl::getMaterialId, item -> item, (left, right) -> left));

        Set<Long> userIds = new HashSet<>();
        userIds.add(operator.getId());
        for (BizAssignment assignment : assignments) {
            if (assignment.getStudentId() != null) {
                userIds.add(assignment.getStudentId());
            }
            if (assignment.getInnerTeacherId() != null) {
                userIds.add(assignment.getInnerTeacherId());
            }
            if (assignment.getBaseTeacherId() != null) {
                userIds.add(assignment.getBaseTeacherId());
            }
        }
        for (BizMaterialVersion version : currentVersionMap.values()) {
            if (version.getSubmittedBy() != null) {
                userIds.add(version.getSubmittedBy());
            }
        }

        Map<Long, SysUser> userMap = queryUserMap(userIds);

        LocalDateTime now = LocalDateTime.now();
        List<MaterialListItemVO> viewItems = new ArrayList<>();

        for (BizMaterial material : materials) {
            BizAssignment assignment = assignmentMap.get(material.getAssignmentId());
            if (assignment == null) {
                continue;
            }
            BizMaterialType materialType = materialTypeMap.get(material.getMaterialTypeId());
            if (materialType == null) {
                continue;
            }
            BizInternshipPlan plan = planMap.get(assignment.getPlanId());
            if (plan == null) {
                continue;
            }

            MaterialListItemVO item = new MaterialListItemVO();
            item.setMaterialId(material.getId());
            item.setAssignmentId(assignment.getId());
            item.setPlanId(assignment.getPlanId());
            item.setPlanCode(plan.getPlanCode());
            item.setPlanName(plan.getPlanName());
            item.setStudentId(material.getStudentId());

            SysUser student = userMap.get(material.getStudentId());
            if (student != null) {
                item.setStudentNo(student.getStudentNo());
                item.setStudentName(student.getRealName());
            }

            item.setMaterialTypeId(materialType.getId());
            item.setMaterialTypeCode(materialType.getTypeCode());
            item.setMaterialTypeName(materialType.getTypeName());
            item.setRequiredFlag(materialType.getRequiredFlag());
            item.setMaxSubmitCount(materialType.getMaxSubmitCount());
            item.setDeadlineTime(materialType.getDeadlineTime());
            item.setLatestVersionNo(material.getLatestVersionNo());
            item.setLastSubmitTime(material.getLastSubmitTime());

            BizMaterialResubmitControl control = resubmitControlMap.get(material.getId());
            boolean lateOpen = isLateSubmitOpen(control, now);
            item.setLateSubmitOpen(lateOpen);
            item.setLateSubmitUntil(control == null ? null : control.getOpenUntil());
            item.setLateSubmitReason(control == null ? null : control.getOpenReason());

            boolean overdue = materialType.getDeadlineTime() != null
                    && now.isAfter(materialType.getDeadlineTime())
                    && (material.getLatestVersionNo() == null || material.getLatestVersionNo() <= 0)
                    && !lateOpen;
            item.setOverdue(overdue);
            item.setMaterialStatus(overdue ? STATUS_OVERDUE : normalizeCode(material.getMaterialStatus()));

            boolean canSubmit = ROLE_STUDENT.equals(roleCode)
                    && Objects.equals(material.getStudentId(), operator.getId())
                    && canSubmitByRule(material, materialType, lateOpen, now);
            item.setCanSubmit(canSubmit);

            BizMaterialVersion latestVersion = currentVersionMap.get(material.getId());
            if (latestVersion != null) {
                item.setLatestVersion(toMaterialVersionVO(latestVersion, userMap));
            }
            viewItems.add(item);
        }

        List<MaterialListItemVO> filtered = applyFilter(viewItems, keyword, planId, studentNo, studentName, materialStatus);

        filtered.sort(Comparator
                .comparing(MaterialListItemVO::getDeadlineTime, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparing(MaterialListItemVO::getPlanId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(MaterialListItemVO::getMaterialTypeCode, Comparator.nullsLast(String::compareTo))
                .thenComparing(MaterialListItemVO::getMaterialId, Comparator.nullsLast(Long::compareTo)));

        List<MaterialListItemVO> paged = paginate(filtered, safePage, safeSize);

        MaterialPageVO result = new MaterialPageVO();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal((long) filtered.size());
        result.setRecords(paged);
        return result;
    }

    private List<MaterialListItemVO> applyFilter(List<MaterialListItemVO> source,
                                                  String keyword,
                                                  Long planId,
                                                  String studentNo,
                                                  String studentName,
                                                  String materialStatus) {
        List<MaterialListItemVO> result = new ArrayList<>(source);

        if (planId != null) {
            result = result.stream()
                    .filter(item -> Objects.equals(item.getPlanId(), planId))
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(studentNo)) {
            String value = studentNo.trim();
            result = result.stream()
                    .filter(item -> StringUtils.hasText(item.getStudentNo()) && item.getStudentNo().contains(value))
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(studentName)) {
            String value = studentName.trim();
            result = result.stream()
                    .filter(item -> StringUtils.hasText(item.getStudentName()) && item.getStudentName().contains(value))
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(materialStatus)) {
            String value = normalizeCode(materialStatus);
            result = result.stream()
                    .filter(item -> value.equals(normalizeCode(item.getMaterialStatus())))
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim().toLowerCase(Locale.ROOT);
            result = result.stream()
                    .filter(item -> containsKeyword(item, value))
                    .collect(Collectors.toList());
        }

        return result;
    }

    private boolean containsKeyword(MaterialListItemVO item, String keyword) {
        return containsIgnoreCase(item.getPlanCode(), keyword)
                || containsIgnoreCase(item.getPlanName(), keyword)
                || containsIgnoreCase(item.getStudentNo(), keyword)
                || containsIgnoreCase(item.getStudentName(), keyword)
                || containsIgnoreCase(item.getMaterialTypeCode(), keyword)
                || containsIgnoreCase(item.getMaterialTypeName(), keyword)
                || (item.getLatestVersion() != null && containsIgnoreCase(item.getLatestVersion().getFileName(), keyword));
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        return StringUtils.hasText(text) && text.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private List<MaterialListItemVO> paginate(List<MaterialListItemVO> source, long page, long size) {
        if (CollectionUtils.isEmpty(source)) {
            return new ArrayList<>();
        }
        int fromIndex = (int) ((page - 1) * size);
        if (fromIndex >= source.size()) {
            return new ArrayList<>();
        }
        int toIndex = Math.min(source.size(), fromIndex + (int) size);
        return new ArrayList<>(source.subList(fromIndex, toIndex));
    }

    private void ensureMaterialMasterRecords(List<BizAssignment> assignments,
                                             Map<Long, List<BizMaterialType>> materialTypesByPlan,
                                             Long operatorId) {
        if (CollectionUtils.isEmpty(assignments)) {
            return;
        }

        Set<Long> assignmentIds = assignments.stream()
                .map(BizAssignment::getId)
                .collect(Collectors.toSet());

        List<BizMaterial> existingMaterials = materialMapper.selectList(new LambdaQueryWrapper<BizMaterial>()
                .in(BizMaterial::getAssignmentId, assignmentIds)
                .eq(BizMaterial::getDeleted, 0L));

        Set<String> existingKeys = existingMaterials.stream()
                .map(item -> buildMaterialUniqueKey(item.getAssignmentId(), item.getMaterialTypeId()))
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        for (BizAssignment assignment : assignments) {
            List<BizMaterialType> types = materialTypesByPlan.getOrDefault(assignment.getPlanId(), Collections.emptyList());
            for (BizMaterialType type : types) {
                String uniqueKey = buildMaterialUniqueKey(assignment.getId(), type.getId());
                if (existingKeys.contains(uniqueKey)) {
                    continue;
                }
                BizMaterial entity = new BizMaterial();
                entity.setAssignmentId(assignment.getId());
                entity.setMaterialTypeId(type.getId());
                entity.setStudentId(assignment.getStudentId());
                entity.setLatestVersionNo(0);
                entity.setMaterialStatus(STATUS_NOT_SUBMITTED);
                entity.setLastSubmitTime(null);
                entity.setCreatedBy(operatorId);
                entity.setUpdatedBy(operatorId);
                entity.setCreatedTime(now);
                entity.setUpdatedTime(now);
                entity.setDeleted(0L);

                try {
                    materialMapper.insert(entity);
                    existingKeys.add(uniqueKey);
                } catch (Exception ignored) {
                    // concurrent creation can hit unique constraint; follow-up query can read existing row.
                }
            }
        }
    }

    private String buildMaterialUniqueKey(Long assignmentId, Long materialTypeId) {
        return String.valueOf(assignmentId) + "_" + materialTypeId;
    }

    private boolean canSubmitByRule(BizMaterial material,
                                    BizMaterialType materialType,
                                    boolean lateSubmitOpen,
                                    LocalDateTime now) {
        int maxCount = materialType.getMaxSubmitCount() == null || materialType.getMaxSubmitCount() <= 0
                ? 1
                : materialType.getMaxSubmitCount();
        int currentCount = material.getLatestVersionNo() == null ? 0 : material.getLatestVersionNo();
        if (currentCount >= maxCount) {
            return false;
        }

        LocalDateTime deadline = materialType.getDeadlineTime();
        if (deadline == null) {
            return true;
        }
        return !now.isAfter(deadline) || lateSubmitOpen;
    }

    private ResponseEntity<Resource> buildFileResponse(BizMaterialVersion version, boolean inline) {
        Path filePath = resolveSafeStoredFile(version.getFilePath());
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "文件不存在");
        }

        String contentType = resolveResponseContentType(version, filePath);
        ContentDisposition disposition = ContentDisposition
                .builder(inline ? "inline" : "attachment")
                .filename(version.getFileName(), StandardCharsets.UTF_8)
                .build();

        try {
            InputStream inputStream = Files.newInputStream(filePath);
            Resource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(version.getFileSize() == null ? Files.size(filePath) : version.getFileSize())
                    .body(resource);
        } catch (IOException ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "打开文件失败");
        }
    }

    private String resolveResponseContentType(BizMaterialVersion version, Path filePath) {
        if (StringUtils.hasText(version.getMimeType())) {
            return version.getMimeType();
        }
        try {
            String detected = Files.probeContentType(filePath);
            if (StringUtils.hasText(detected)) {
                return detected;
            }
        } catch (IOException ignored) {
            // fallback below
        }
        return resolveMimeType(null, resolveExtension(version.getFileName(), version.getFileExt()));
    }

    private void ensureVersionReadAccess(BizMaterialVersion version,
                                         Long userId,
                                         String roleCode) {
        BizMaterial material = requireMaterial(version.getMaterialId());
        BizAssignment assignment = requireAssignment(material.getAssignmentId());
        BizInternshipPlan plan = requirePlan(assignment.getPlanId());
        SysUser operator = requireUser(userId);
        ensureMaterialReadAccess(material, assignment, plan, operator, normalizeCode(roleCode));
    }

    private void ensureMaterialReadAccess(BizMaterial material,
                                          BizAssignment assignment,
                                          BizInternshipPlan plan,
                                          SysUser operator,
                                          String roleCode) {
        if (ROLE_DEPT_ADMIN.equals(roleCode)) {
            if (!Objects.equals(plan.getDeptId(), operator.getDeptId())) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权访问该材料");
            }
            return;
        }
        if (ROLE_STUDENT.equals(roleCode)) {
            if (!Objects.equals(material.getStudentId(), operator.getId())) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权访问该材料");
            }
            return;
        }
        if (ROLE_INNER_TEACHER.equals(roleCode)) {
            if (!Objects.equals(assignment.getInnerTeacherId(), operator.getId())) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权访问该材料");
            }
            return;
        }
        if (ROLE_BASE_TEACHER.equals(roleCode)) {
            if (!Objects.equals(assignment.getBaseTeacherId(), operator.getId())) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权访问该材料");
            }
            return;
        }

        throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权访问该材料");
    }

    private void ensureAdminManageAccess(BizInternshipPlan plan,
                                         SysUser operator,
                                         String roleCode) {
        if (ROLE_DEPT_ADMIN.equals(roleCode) && Objects.equals(plan.getDeptId(), operator.getDeptId())) {
            return;
        }
        throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权管理该材料");
    }

    private List<BizAssignment> queryCurrentAssignmentsByStudent(Long userId) {
        return assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getStudentId, userId)
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L));
    }

    private List<BizAssignment> queryTeacherAssignments(Long userId, String roleCode) {
        if (!ROLE_INNER_TEACHER.equals(roleCode) && !ROLE_BASE_TEACHER.equals(roleCode)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色必须是教师");
        }

        LambdaQueryWrapper<BizAssignment> wrapper = new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L);

        if (ROLE_INNER_TEACHER.equals(roleCode)) {
            wrapper.eq(BizAssignment::getInnerTeacherId, userId);
        } else {
            wrapper.eq(BizAssignment::getBaseTeacherId, userId);
        }
        return assignmentMapper.selectList(wrapper);
    }

    private List<BizAssignment> queryAdminAssignments(SysUser operator, String roleCode) {
        if (!ROLE_DEPT_ADMIN.equals(roleCode)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色必须是管理员");
        }

        if (operator.getDeptId() == null) {
            return new ArrayList<>();
        }

        Set<Long> planIds = planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                        .eq(BizInternshipPlan::getDeptId, operator.getDeptId())
                        .eq(BizInternshipPlan::getDeleted, 0L))
                .stream()
                .map(BizInternshipPlan::getId)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(planIds)) {
            return new ArrayList<>();
        }

        return assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                .in(BizAssignment::getPlanId, planIds)
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L));
    }

    private Map<Long, BizInternshipPlan> queryPlanMap(Set<Long> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return new HashMap<>();
        }
        return planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                        .in(BizInternshipPlan::getId, planIds)
                        .eq(BizInternshipPlan::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BizInternshipPlan::getId, item -> item, (left, right) -> left));
    }

    private Map<Long, SysUser> queryUserMap(Set<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<>();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getId, userIds)
                        .eq(SysUser::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, item -> item, (left, right) -> left));
    }

    private MaterialVersionVO toMaterialVersionVO(BizMaterialVersion entity,
                                                  Map<Long, SysUser> userMap) {
        MaterialVersionVO vo = new MaterialVersionVO();
        vo.setId(entity.getId());
        vo.setMaterialId(entity.getMaterialId());
        vo.setVersionNo(entity.getVersionNo());
        vo.setFileName(entity.getFileName());
        vo.setFileSize(entity.getFileSize());
        vo.setFileExt(resolveExtension(entity.getFileName(), entity.getFileExt()));
        vo.setMimeType(entity.getMimeType());
        vo.setSubmitRemark(entity.getSubmitRemark());
        vo.setSubmittedBy(entity.getSubmittedBy());
        vo.setSubmittedTime(entity.getSubmittedTime());
        vo.setIsCurrent(entity.getIsCurrent());

        SysUser submitter = userMap.get(entity.getSubmittedBy());
        vo.setSubmittedByName(submitter == null ? null : submitter.getRealName());

        String extension = resolveExtension(entity.getFileName(), entity.getFileExt());
        boolean previewable = PREVIEWABLE_FILE_EXTENSIONS.contains(extension);
        vo.setPreviewable(previewable);
        if ("docx".equals(extension)) {
            vo.setPreviewUrl("/api/v1/material/file/docx-preview/" + entity.getId());
        } else {
            vo.setPreviewUrl("/api/v1/material/file/preview/" + entity.getId());
        }
        vo.setDownloadUrl("/api/v1/material/file/download/" + entity.getId());
        return vo;
    }

    private String buildDocxPreviewHtml(BizMaterialVersion version, XWPFDocument document) {
        StringBuilder html = new StringBuilder(8192);
        html.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\"/>")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>")
                .append("<title>").append(escapeHtml(version.getFileName())).append("</title>")
                .append("<style>")
                .append("body{margin:0;padding:0;background:#f6f8fc;color:#1f2937;font-family:Arial,'PingFang SC','Microsoft YaHei',sans-serif;}")
                .append(".page{max-width:960px;margin:0 auto;padding:24px 20px 40px;}")
                .append(".header{background:#fff;border:1px solid #e5e7eb;border-radius:16px;padding:20px 24px;margin-bottom:18px;box-shadow:0 10px 30px rgba(15,23,42,.06);}")
                .append(".header h1{margin:0 0 8px;font-size:22px;line-height:1.4;color:#111827;}")
                .append(".meta{display:flex;flex-wrap:wrap;gap:12px;color:#6b7280;font-size:13px;}")
                .append(".doc{background:#fff;border:1px solid #e5e7eb;border-radius:16px;padding:28px 32px;box-shadow:0 10px 30px rgba(15,23,42,.06);}")
                .append(".doc p{margin:0 0 14px;line-height:1.85;white-space:pre-wrap;word-break:break-word;}")
                .append(".doc .empty-line{min-height:1.4em;}")
                .append(".doc table{width:100%;border-collapse:collapse;margin:14px 0 18px;}")
                .append(".doc td,.doc th{border:1px solid #d9e2ef;padding:8px 10px;vertical-align:top;word-break:break-word;}")
                .append(".doc th{background:#f3f6fb;font-weight:600;}")
                .append("</style></head><body>")
                .append("<div class=\"page\">")
                .append("<div class=\"header\">")
                .append("<h1>").append(escapeHtml(version.getFileName())).append("</h1>")
                .append("<div class=\"meta\">")
                .append("<span>版本：").append(version.getVersionNo() == null ? "-" : version.getVersionNo()).append("</span>")
                .append("<span>类型：DOCX 在线预览</span>")
                .append("</div></div>")
                .append("<div class=\"doc\">");

        boolean hasContent = false;
        for (IBodyElement element : document.getBodyElements()) {
            hasContent = true;
            if (element instanceof XWPFParagraph) {
                appendParagraphHtml(html, (XWPFParagraph) element);
            } else if (element instanceof XWPFTable) {
                appendTableHtml(html, (XWPFTable) element);
            }
        }

        if (!hasContent) {
            html.append("<p class=\"empty-line\">暂无可展示内容</p>");
        }

        html.append("</div></div></body></html>");
        return html.toString();
    }

    private void appendParagraphHtml(StringBuilder html, XWPFParagraph paragraph) {
        String text = paragraph.getText();
        if (!StringUtils.hasText(text)) {
            html.append("<p class=\"empty-line\">&nbsp;</p>");
            return;
        }
        html.append("<p>")
                .append(escapeHtml(text).replace("\n", "<br/>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"))
                .append("</p>");
    }

    private void appendTableHtml(StringBuilder html, XWPFTable table) {
        html.append("<table><tbody>");
        for (XWPFTableRow row : table.getRows()) {
            html.append("<tr>");
            for (XWPFTableCell cell : row.getTableCells()) {
                String text = cell.getText();
                html.append("<td>");
                if (StringUtils.hasText(text)) {
                    html.append(escapeHtml(text).replace("\n", "<br/>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"));
                } else {
                    html.append("&nbsp;");
                }
                html.append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table>");
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(text.length() + 32);
        for (char ch : text.toCharArray()) {
            switch (ch) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                default:
                    escaped.append(ch);
            }
        }
        return escaped.toString();
    }

    private boolean isLateSubmitOpen(BizMaterialResubmitControl control, LocalDateTime now) {
        if (control == null) {
            return false;
        }
        if (control.getOpenFlag() == null || control.getOpenFlag() != 1) {
            return false;
        }
        return control.getOpenUntil() == null || !now.isAfter(control.getOpenUntil());
    }

    private BizMaterialResubmitControl findResubmitControl(Long materialId) {
        return resubmitControlMapper.selectOne(new LambdaQueryWrapper<BizMaterialResubmitControl>()
                .eq(BizMaterialResubmitControl::getMaterialId, materialId)
                .eq(BizMaterialResubmitControl::getDeleted, 0L)
                .last("LIMIT 1"));
    }

    private BizMaterial requireMaterial(Long materialId) {
        BizMaterial material = materialMapper.selectOne(new LambdaQueryWrapper<BizMaterial>()
                .eq(BizMaterial::getId, materialId)
                .eq(BizMaterial::getDeleted, 0L)
                .last("LIMIT 1"));
        if (material == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "材料不存在");
        }
        return material;
    }

    private BizMaterialVersion requireMaterialVersion(Long versionId) {
        BizMaterialVersion entity = materialVersionMapper.selectOne(new LambdaQueryWrapper<BizMaterialVersion>()
                .eq(BizMaterialVersion::getId, versionId)
                .eq(BizMaterialVersion::getDeleted, 0L)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "材料版本不存在");
        }
        return entity;
    }

    private BizAssignment requireAssignment(Long assignmentId) {
        BizAssignment assignment = assignmentMapper.selectOne(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getId, assignmentId)
                .eq(BizAssignment::getDeleted, 0L)
                .last("LIMIT 1"));
        if (assignment == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "分配记录不存在");
        }
        return assignment;
    }

    private BizMaterialType requireMaterialType(Long materialTypeId) {
        BizMaterialType materialType = materialTypeMapper.selectOne(new LambdaQueryWrapper<BizMaterialType>()
                .eq(BizMaterialType::getId, materialTypeId)
                .eq(BizMaterialType::getDeleted, 0L)
                .last("LIMIT 1"));
        if (materialType == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "材料类型不存在");
        }
        return materialType;
    }

    private BizInternshipPlan requirePlan(Long planId) {
        BizInternshipPlan plan = planMapper.selectOne(new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getId, planId)
                .eq(BizInternshipPlan::getDeleted, 0L)
                .last("LIMIT 1"));
        if (plan == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "计划不存在");
        }
        return plan;
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

    private MaterialPageVO emptyPage(long page, long size) {
        MaterialPageVO result = new MaterialPageVO();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(0L);
        result.setRecords(new ArrayList<>());
        return result;
    }

    private long normalizePage(long page) {
        return page <= 0 ? 1 : page;
    }

    private long normalizeSize(long size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
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

    private String resolveExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String resolveExtension(String fileName, String fallbackExt) {
        String ext = resolveExtension(fileName);
        if (StringUtils.hasText(ext)) {
            return ext;
        }
        return normalizeCode(fallbackExt).toLowerCase(Locale.ROOT);
    }

    private String resolveMimeType(String mimeType, String extension) {
        if (StringUtils.hasText(mimeType)) {
            return mimeType;
        }
        String ext = normalizeCode(extension).toLowerCase(Locale.ROOT);
        if ("pdf".equals(ext)) {
            return "application/pdf";
        }
        if ("jpg".equals(ext) || "jpeg".equals(ext)) {
            return "image/jpeg";
        }
        if ("png".equals(ext)) {
            return "image/png";
        }
        if ("mp4".equals(ext)) {
            return "video/mp4";
        }
        if ("doc".equals(ext)) {
            return "application/msword";
        }
        if ("docx".equals(ext)) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";
    }

    private Path storageRootPath() {
        return Paths.get(fileStorageProperties.getRootPath()).toAbsolutePath().normalize();
    }

    private Path resolveSafeTargetPath(String relativePath) {
        Path root = storageRootPath();
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "文件路径无效");
        }
        return target;
    }

    private Path resolveSafeStoredFile(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "存储文件路径无效");
        }
        return resolveSafeTargetPath(relativePath);
    }

    private void cleanupStoredFile(Path targetPath) {
        if (targetPath == null) {
            return;
        }
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ignored) {
            // keep original business exception when db transaction fails
        }
    }
}
