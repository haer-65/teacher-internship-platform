package com.teacher.internship.modules.plan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.config.FileStorageProperties;
import com.teacher.internship.modules.application.entity.BizAssignment;
import com.teacher.internship.modules.application.entity.BizStudentApplication;
import com.teacher.internship.modules.application.mapper.BizAssignmentMapper;
import com.teacher.internship.modules.application.mapper.BizStudentApplicationMapper;
import com.teacher.internship.modules.base.entity.BaseInternshipBase;
import com.teacher.internship.modules.base.entity.BaseDepartment;
import com.teacher.internship.modules.base.mapper.BaseInternshipBaseMapper;
import com.teacher.internship.modules.base.mapper.BaseDepartmentMapper;
import com.teacher.internship.modules.evaluation.entity.BizEvaluation;
import com.teacher.internship.modules.evaluation.mapper.BizEvaluationMapper;
import com.teacher.internship.modules.notice.service.NoticeTriggerService;
import com.teacher.internship.modules.plan.dto.PlanBaseRequest;
import com.teacher.internship.modules.plan.dto.PlanMaterialTypeRequest;
import com.teacher.internship.modules.plan.dto.PlanSaveRequest;
import com.teacher.internship.modules.plan.entity.BizInternshipPlan;
import com.teacher.internship.modules.plan.entity.BizMaterialType;
import com.teacher.internship.modules.plan.entity.BizPlanBase;
import com.teacher.internship.modules.plan.entity.BizPlanAttachment;
import com.teacher.internship.modules.plan.mapper.BizInternshipPlanMapper;
import com.teacher.internship.modules.plan.mapper.BizMaterialTypeMapper;
import com.teacher.internship.modules.plan.mapper.BizPlanBaseMapper;
import com.teacher.internship.modules.plan.mapper.BizPlanAttachmentMapper;
import com.teacher.internship.modules.plan.vo.PlanAttachmentVO;
import com.teacher.internship.modules.plan.vo.PlanBaseVO;
import com.teacher.internship.modules.plan.vo.PlanDetailVO;
import com.teacher.internship.modules.plan.vo.PlanListItemVO;
import com.teacher.internship.modules.plan.vo.PlanMaterialTypeVO;
import com.teacher.internship.modules.plan.vo.PlanPageVO;
import com.teacher.internship.modules.material.entity.BizMaterial;
import com.teacher.internship.modules.material.entity.BizMaterialVersion;
import com.teacher.internship.modules.material.mapper.BizMaterialMapper;
import com.teacher.internship.modules.material.mapper.BizMaterialVersionMapper;
import com.teacher.internship.modules.score.entity.BizScoreSheet;
import com.teacher.internship.modules.score.mapper.BizScoreSheetMapper;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import com.teacher.internship.modules.system.service.SystemParamService;
import com.teacher.internship.modules.system.vo.IdNameOptionVO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlanService {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final String ROLE_SYS_ADMIN = "SYS_ADMIN";
    private static final String ROLE_DEPT_ADMIN = "DEPT_ADMIN";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_INNER_TEACHER = "INNER_TEACHER";
    private static final String ROLE_BASE_TEACHER = "BASE_TEACHER";

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_FINISHED = "FINISHED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";
    private static final String STATUS_ENABLED = "ENABLED";
    private static final String TYPE_PROCESS = "PROCESS";
    private static final String TYPE_FINAL = "FINAL";
    private static final String PARAM_FILE_MAX_SIZE_MB = "SYSTEM_FILE_MAX_SIZE_MB";
    private static final String PARAM_PLAN_DEFAULT_QUOTA = "PLAN_DEFAULT_QUOTA";
    private static final int DEFAULT_PLAN_DEFAULT_QUOTA = 50;

    private final BizInternshipPlanMapper planMapper;
    private final BizMaterialTypeMapper materialTypeMapper;
    private final BizPlanBaseMapper planBaseMapper;
    private final BizPlanAttachmentMapper attachmentMapper;
    private final BizStudentApplicationMapper studentApplicationMapper;
    private final BizAssignmentMapper assignmentMapper;
    private final BizEvaluationMapper evaluationMapper;
    private final BizMaterialMapper materialMapper;
    private final BizMaterialVersionMapper materialVersionMapper;
    private final BizScoreSheetMapper scoreSheetMapper;
    private final BaseDepartmentMapper departmentMapper;
    private final BaseInternshipBaseMapper internshipBaseMapper;
    private final SysUserMapper sysUserMapper;
    private final FileStorageProperties fileStorageProperties;
    private final SystemParamService paramService;
    private final NoticeTriggerService noticeTriggerService;

    public PlanService(BizInternshipPlanMapper planMapper,
                       BizMaterialTypeMapper materialTypeMapper,
                       BizPlanBaseMapper planBaseMapper,
                       BizPlanAttachmentMapper attachmentMapper,
                       BizStudentApplicationMapper studentApplicationMapper,
                       BizAssignmentMapper assignmentMapper,
                       BizEvaluationMapper evaluationMapper,
                       BizMaterialMapper materialMapper,
                       BizMaterialVersionMapper materialVersionMapper,
                       BizScoreSheetMapper scoreSheetMapper,
                       BaseDepartmentMapper departmentMapper,
                       BaseInternshipBaseMapper internshipBaseMapper,
                       SysUserMapper sysUserMapper,
                       FileStorageProperties fileStorageProperties,
                       SystemParamService paramService,
                       NoticeTriggerService noticeTriggerService) {
        this.planMapper = planMapper;
        this.materialTypeMapper = materialTypeMapper;
        this.planBaseMapper = planBaseMapper;
        this.attachmentMapper = attachmentMapper;
        this.studentApplicationMapper = studentApplicationMapper;
        this.assignmentMapper = assignmentMapper;
        this.evaluationMapper = evaluationMapper;
        this.materialMapper = materialMapper;
        this.materialVersionMapper = materialVersionMapper;
        this.scoreSheetMapper = scoreSheetMapper;
        this.departmentMapper = departmentMapper;
        this.internshipBaseMapper = internshipBaseMapper;
        this.sysUserMapper = sysUserMapper;
        this.fileStorageProperties = fileStorageProperties;
        this.paramService = paramService;
        this.noticeTriggerService = noticeTriggerService;
    }

    public PlanPageVO queryPlanPage(long page,
                                    long size,
                                    String keyword,
                                    String status,
                                    Long userId,
                                    String roleCode,
                                    boolean studentView) {
        long safePage = page <= 0 ? 1 : page;
        long safeSize = size <= 0 ? 10 : size;
        String normalizedRoleCode = normalizeCode(roleCode);

        SysUser currentUser = requireUser(userId);

        LambdaQueryWrapper<BizInternshipPlan> wrapper = new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getDeleted, 0L)
                .orderByDesc(BizInternshipPlan::getCreatedTime);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BizInternshipPlan::getPlanCode, keyword)
                    .or().like(BizInternshipPlan::getPlanName, keyword)
                    .or().like(BizInternshipPlan::getAcademicYear, keyword)
                    .or().like(BizInternshipPlan::getTerm, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(BizInternshipPlan::getPlanStatus, normalizeCode(status));
        }

        if (studentView || ROLE_STUDENT.equals(normalizedRoleCode)) {
            wrapper.eq(BizInternshipPlan::getDeptId, currentUser.getDeptId())
                    .in(BizInternshipPlan::getPlanStatus, visibleStatusesForStudent());
        } else if (ROLE_DEPT_ADMIN.equals(normalizedRoleCode)) {
            wrapper.eq(BizInternshipPlan::getDeptId, currentUser.getDeptId());
        }

        Page<BizInternshipPlan> pageResult = planMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        Map<Long, Integer> remainingQuotaMap = queryRemainingQuotaMapByPlanIds(pageResult.getRecords().stream()
                .map(BizInternshipPlan::getId)
                .collect(Collectors.toSet()));

        PlanPageVO vo = new PlanPageVO();
        vo.setPage(safePage);
        vo.setSize(safeSize);
        vo.setTotal(pageResult.getTotal());
        vo.setRecords(pageResult.getRecords().stream()
                .map(entity -> toPlanListItemVO(entity, remainingQuotaMap.get(entity.getId())))
                .collect(Collectors.toList()));
        return vo;
    }

    public PlanDetailVO getPlanDetail(Long planId, Long userId, String roleCode, boolean studentView) {
        BizInternshipPlan plan = requirePlan(planId);
        SysUser user = requireUser(userId);
        ensureReadAccess(plan, user, normalizeCode(roleCode), studentView);
        return buildPlanDetail(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public PlanDetailVO createDraft(PlanSaveRequest request, Long userId, String roleCode) {
        String normalizedRoleCode = normalizeCode(roleCode);
        SysUser currentUser = requireUser(userId);
        ensureManagePermission(currentUser, normalizedRoleCode, request.getDeptId());
        normalizePlanDefaultQuota(request);
        validateSaveRequest(request);

        BizInternshipPlan plan = new BizInternshipPlan();
        plan.setPlanCode(generatePlanCode());
        applySaveRequest(plan, request);
        plan.setPlanStatus(STATUS_DRAFT);
        plan.setScorePublishStatus("UNPUBLISHED");
        plan.setPublishedTime(null);
        plan.setArchivedTime(null);
        plan.setCreatedBy(userId);
        plan.setUpdatedBy(userId);
        plan.setDeleted(0L);
        planMapper.insert(plan);

        replacePlanBases(plan.getId(), request.getPlanBases(), userId);
        replaceMaterialTypes(plan.getId(), request.getMaterialTypes(), userId);
        return buildPlanDetail(requirePlan(plan.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public PlanDetailVO updateDraft(Long planId, PlanSaveRequest request, Long userId, String roleCode) {
        BizInternshipPlan plan = requirePlan(planId);
        String normalizedRoleCode = normalizeCode(roleCode);
        SysUser currentUser = requireUser(userId);
        ensureManageAccess(plan, currentUser, normalizedRoleCode);

        if (STATUS_ARCHIVED.equals(plan.getPlanStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "已归档计划只读，不允许修改");
        }
        if (!STATUS_DRAFT.equals(plan.getPlanStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有草稿计划才可以编辑");
        }

        validateSaveRequest(request);
        ensureManagePermission(currentUser, normalizedRoleCode, request.getDeptId());

        applySaveRequest(plan, request);
        plan.setUpdatedBy(userId);
        plan.setUpdatedTime(LocalDateTime.now());
        planMapper.updateById(plan);

        replacePlanBases(plan.getId(), request.getPlanBases(), userId);
        replaceMaterialTypes(plan.getId(), request.getMaterialTypes(), userId);
        return buildPlanDetail(requirePlan(plan.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public PlanDetailVO publishPlan(Long planId, Long userId, String roleCode) {
        BizInternshipPlan plan = requirePlan(planId);
        String normalizedRoleCode = normalizeCode(roleCode);
        SysUser currentUser = requireUser(userId);
        ensureManageAccess(plan, currentUser, normalizedRoleCode);

        if (!STATUS_DRAFT.equals(plan.getPlanStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有草稿计划才可以发布");
        }

        validateTeacherWeights(plan.getInnerTeacherWeight(), plan.getBaseTeacherWeight());
        validatePlanBaseConfigurationForPublish(plan.getId(), plan.getStudentQuota());
        validateMaterialConfigurationForPublish(plan.getId(), plan.getStartTime(), plan.getEndTime());

        plan.setPlanStatus(STATUS_PUBLISHED);
        plan.setPublishedTime(LocalDateTime.now());
        plan.setUpdatedBy(userId);
        plan.setUpdatedTime(LocalDateTime.now());
        planMapper.updateById(plan);
        noticeTriggerService.notifyPlanPublished(plan, userId);
        return buildPlanDetail(requirePlan(plan.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public PlanDetailVO finishPlan(Long planId, Long userId, String roleCode) {
        BizInternshipPlan plan = requirePlan(planId);
        String normalizedRoleCode = normalizeCode(roleCode);
        SysUser currentUser = requireUser(userId);
        ensureManageAccess(plan, currentUser, normalizedRoleCode);

        if (!STATUS_PUBLISHED.equals(plan.getPlanStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有已发布计划才可以结束");
        }

        if (plan.getEndTime() != null && LocalDateTime.now().isBefore(plan.getEndTime())) {
            ensurePlanReadyForManualFinish(plan);
        }

        plan.setPlanStatus(STATUS_FINISHED);
        plan.setUpdatedBy(userId);
        plan.setUpdatedTime(LocalDateTime.now());
        planMapper.updateById(plan);
        noticeTriggerService.notifyPlanFinished(plan, userId);
        return buildPlanDetail(requirePlan(plan.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public PlanDetailVO archivePlan(Long planId, Long userId, String roleCode) {
        BizInternshipPlan plan = requirePlan(planId);
        String normalizedRoleCode = normalizeCode(roleCode);
        SysUser currentUser = requireUser(userId);
        ensureManageAccess(plan, currentUser, normalizedRoleCode);

        if (!STATUS_FINISHED.equals(plan.getPlanStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有已结束计划才可以归档");
        }

        ensurePlanReadyForArchive(plan);

        plan.setPlanStatus(STATUS_ARCHIVED);
        plan.setArchivedTime(LocalDateTime.now());
        plan.setUpdatedBy(userId);
        plan.setUpdatedTime(LocalDateTime.now());
        planMapper.updateById(plan);
        return buildPlanDetail(requirePlan(plan.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long planId, Long userId, String roleCode) {
        BizInternshipPlan plan = requirePlan(planId);
        String normalizedRoleCode = normalizeCode(roleCode);
        SysUser currentUser = requireUser(userId);
        ensureManageAccess(plan, currentUser, normalizedRoleCode);

        if (!STATUS_DRAFT.equals(plan.getPlanStatus()) && !STATUS_ARCHIVED.equals(plan.getPlanStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有草稿或已归档计划才可以删除");
        }

        if (STATUS_ARCHIVED.equals(plan.getPlanStatus())) {
            ensureArchivedPlanHasNoBusinessData(planId);
        }

        long deletedFlag = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();

        List<BizPlanAttachment> attachments = attachmentMapper.selectList(new LambdaQueryWrapper<BizPlanAttachment>()
                .eq(BizPlanAttachment::getPlanId, planId)
                .eq(BizPlanAttachment::getDeleted, 0L));

        planBaseMapper.update(null, new LambdaUpdateWrapper<BizPlanBase>()
                .eq(BizPlanBase::getPlanId, planId)
                .eq(BizPlanBase::getDeleted, 0L)
                .set(BizPlanBase::getDeleted, deletedFlag)
                .set(BizPlanBase::getUpdatedBy, userId)
                .set(BizPlanBase::getUpdatedTime, now));

        materialTypeMapper.update(null, new LambdaUpdateWrapper<BizMaterialType>()
                .eq(BizMaterialType::getPlanId, planId)
                .eq(BizMaterialType::getDeleted, 0L)
                .set(BizMaterialType::getDeleted, deletedFlag)
                .set(BizMaterialType::getUpdatedBy, userId)
                .set(BizMaterialType::getUpdatedTime, now));

        attachmentMapper.update(null, new LambdaUpdateWrapper<BizPlanAttachment>()
                .eq(BizPlanAttachment::getPlanId, planId)
                .eq(BizPlanAttachment::getDeleted, 0L)
                .set(BizPlanAttachment::getDeleted, deletedFlag)
                .set(BizPlanAttachment::getUpdatedBy, userId)
                .set(BizPlanAttachment::getUpdatedTime, now));

        plan.setDeleted(deletedFlag);
        plan.setUpdatedBy(userId);
        plan.setUpdatedTime(now);
        planMapper.updateById(plan);

        for (BizPlanAttachment attachment : attachments) {
            if (!StringUtils.hasText(attachment.getFilePath())) {
                continue;
            }
            Path fullPath = Paths.get(fileStorageProperties.getRootPath()).resolve(attachment.getFilePath());
            try {
                Files.deleteIfExists(fullPath);
            } catch (IOException ignored) {
                // keep DB consistency even if file cleanup fails
            }
        }
    }

    private void ensureArchivedPlanHasNoBusinessData(Long planId) {
        long applicationCount = studentApplicationMapper.selectCount(new LambdaQueryWrapper<BizStudentApplication>()
                .eq(BizStudentApplication::getPlanId, planId)
                .eq(BizStudentApplication::getDeleted, 0L));
        if (applicationCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该已归档计划下仍存在学生申请记录，不能删除");
        }

        long assignmentCount = assignmentMapper.selectCount(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getPlanId, planId)
                .eq(BizAssignment::getDeleted, 0L));
        if (assignmentCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该已归档计划下仍存在分配记录，不能删除");
        }

        long evaluationCount = evaluationMapper.selectCount(new LambdaQueryWrapper<BizEvaluation>()
                .eq(BizEvaluation::getPlanId, planId)
                .eq(BizEvaluation::getDeleted, 0L));
        if (evaluationCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该已归档计划下仍存在评价记录，不能删除");
        }

        long scoreCount = scoreSheetMapper.selectCount(new LambdaQueryWrapper<BizScoreSheet>()
                .eq(BizScoreSheet::getPlanId, planId)
                .eq(BizScoreSheet::getDeleted, 0L));
        if (scoreCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该已归档计划下仍存在成绩记录，不能删除");
        }
    }

    private void ensurePlanReadyForManualFinish(BizInternshipPlan plan) {
        ensureRequiredMaterialsSubmitted(plan);
        ensureProcessEvaluationsCompleted(plan);
    }

    private void ensurePlanReadyForArchive(BizInternshipPlan plan) {
        ensureFinalEvaluationsCompleted(plan);
    }

    private void ensureRequiredMaterialsSubmitted(BizInternshipPlan plan) {
        List<BizAssignment> assignments = queryCurrentAssignmentsByPlan(plan.getId());
        if (CollectionUtils.isEmpty(assignments)) {
            return;
        }

        List<BizMaterialType> requiredTypes = materialTypeMapper.selectList(new LambdaQueryWrapper<BizMaterialType>()
                .eq(BizMaterialType::getPlanId, plan.getId())
                .eq(BizMaterialType::getDeleted, 0L)
                .eq(BizMaterialType::getStatus, STATUS_ENABLED)
                .eq(BizMaterialType::getRequiredFlag, 1));
        if (CollectionUtils.isEmpty(requiredTypes)) {
            return;
        }

        Set<Long> assignmentIds = assignments.stream().map(BizAssignment::getId).collect(Collectors.toSet());
        Map<Long, Map<Long, BizMaterial>> materialByAssignmentMap = materialMapper.selectList(new LambdaQueryWrapper<BizMaterial>()
                        .in(BizMaterial::getAssignmentId, assignmentIds)
                        .eq(BizMaterial::getDeleted, 0L))
                .stream()
                .filter(item -> item.getAssignmentId() != null && item.getMaterialTypeId() != null)
                .collect(Collectors.groupingBy(BizMaterial::getAssignmentId,
                        Collectors.toMap(BizMaterial::getMaterialTypeId, item -> item, (left, right) -> left)));

        Set<Long> materialIds = materialByAssignmentMap.values().stream()
                .flatMap(map -> map.values().stream())
                .map(BizMaterial::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(materialIds)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "学生必交材料尚未全部提交，不能提前结束计划");
        }
        Map<Long, BizMaterialVersion> currentVersionMap = materialVersionMapper.selectList(new LambdaQueryWrapper<BizMaterialVersion>()
                        .in(BizMaterialVersion::getMaterialId, materialIds)
                        .eq(BizMaterialVersion::getDeleted, 0L)
                        .eq(BizMaterialVersion::getIsCurrent, 1))
                .stream()
                .filter(item -> item.getMaterialId() != null)
                .collect(Collectors.toMap(BizMaterialVersion::getMaterialId, item -> item, (left, right) -> left));

        for (BizAssignment assignment : assignments) {
            Map<Long, BizMaterial> byType = materialByAssignmentMap.get(assignment.getId());
            for (BizMaterialType requiredType : requiredTypes) {
                BizMaterial material = byType == null ? null : byType.get(requiredType.getId());
                if (material == null) {
                    throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "学生必交材料尚未全部提交，不能提前结束计划");
                }
                BizMaterialVersion version = currentVersionMap.get(material.getId());
                if (version == null) {
                    throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "学生必交材料尚未全部提交，不能提前结束计划");
                }
            }
        }
    }

    private void ensureProcessEvaluationsCompleted(BizInternshipPlan plan) {
        List<BizAssignment> assignments = queryCurrentAssignmentsByPlan(plan.getId());
        if (CollectionUtils.isEmpty(assignments)) {
            return;
        }

        List<BizMaterialType> requiredTypes = materialTypeMapper.selectList(new LambdaQueryWrapper<BizMaterialType>()
                .eq(BizMaterialType::getPlanId, plan.getId())
                .eq(BizMaterialType::getDeleted, 0L)
                .eq(BizMaterialType::getStatus, STATUS_ENABLED)
                .eq(BizMaterialType::getRequiredFlag, 1));
        if (CollectionUtils.isEmpty(requiredTypes)) {
            return;
        }

        Set<Long> assignmentIds = assignments.stream().map(BizAssignment::getId).collect(Collectors.toSet());
        Map<Long, Map<Long, BizMaterial>> materialByAssignmentMap = materialMapper.selectList(new LambdaQueryWrapper<BizMaterial>()
                        .in(BizMaterial::getAssignmentId, assignmentIds)
                        .eq(BizMaterial::getDeleted, 0L))
                .stream()
                .filter(item -> item.getAssignmentId() != null && item.getMaterialTypeId() != null)
                .collect(Collectors.groupingBy(BizMaterial::getAssignmentId,
                        Collectors.toMap(BizMaterial::getMaterialTypeId, item -> item, (left, right) -> left)));

        Set<Long> materialIds = materialByAssignmentMap.values().stream()
                .flatMap(map -> map.values().stream())
                .map(BizMaterial::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(materialIds)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "学生必交材料尚未全部提交，不能提前结束计划");
        }
        Map<Long, BizMaterialVersion> currentVersionMap = materialVersionMapper.selectList(new LambdaQueryWrapper<BizMaterialVersion>()
                        .in(BizMaterialVersion::getMaterialId, materialIds)
                        .eq(BizMaterialVersion::getDeleted, 0L)
                        .eq(BizMaterialVersion::getIsCurrent, 1))
                .stream()
                .filter(item -> item.getMaterialId() != null)
                .collect(Collectors.toMap(BizMaterialVersion::getMaterialId, item -> item, (left, right) -> left));

        for (BizAssignment assignment : assignments) {
            if (assignment.getInnerTeacherId() == null || assignment.getBaseTeacherId() == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "指导老师信息不完整，不能提前结束计划");
            }
            Map<Long, BizMaterial> byType = materialByAssignmentMap.get(assignment.getId());
            for (BizMaterialType requiredType : requiredTypes) {
                BizMaterial material = byType == null ? null : byType.get(requiredType.getId());
                if (material == null) {
                    throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "学生必交材料尚未全部提交，不能提前结束计划");
                }
                BizMaterialVersion version = currentVersionMap.get(material.getId());
                if (version == null) {
                    throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "学生必交材料尚未全部提交，不能提前结束计划");
                }
                int innerCount = countTeacherEvaluations(assignment.getId(), assignment.getInnerTeacherId(), ROLE_INNER_TEACHER, TYPE_PROCESS, version.getId());
                int baseCount = countTeacherEvaluations(assignment.getId(), assignment.getBaseTeacherId(), ROLE_BASE_TEACHER, TYPE_PROCESS, version.getId());
                if (innerCount <= 0 || baseCount <= 0) {
                    throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "两个指导老师过程评价未完成，不能提前结束计划");
                }
            }
        }
    }

    private void ensureFinalEvaluationsCompleted(BizInternshipPlan plan) {
        List<BizAssignment> assignments = queryCurrentAssignmentsByPlan(plan.getId());
        if (CollectionUtils.isEmpty(assignments)) {
            return;
        }

        for (BizAssignment assignment : assignments) {
            if (assignment.getInnerTeacherId() == null || assignment.getBaseTeacherId() == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "指导老师信息不完整，不能归档计划");
            }
            int innerCount = countTeacherEvaluations(assignment.getId(), assignment.getInnerTeacherId(), ROLE_INNER_TEACHER, TYPE_FINAL, null);
            int baseCount = countTeacherEvaluations(assignment.getId(), assignment.getBaseTeacherId(), ROLE_BASE_TEACHER, TYPE_FINAL, null);
            if (innerCount <= 0 || baseCount <= 0) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "两个指导老师综合评价未完成，不能归档计划");
            }
        }
    }

    private List<BizAssignment> queryCurrentAssignmentsByPlan(Long planId) {
        return assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getPlanId, planId)
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L));
    }

    private Map<Long, Integer> queryRemainingQuotaMapByPlanIds(Set<Long> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return new HashMap<>();
        }
        List<BizInternshipPlan> plans = planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                .in(BizInternshipPlan::getId, planIds)
                .eq(BizInternshipPlan::getDeleted, 0L));
        if (CollectionUtils.isEmpty(plans)) {
            return new HashMap<>();
        }
        Map<Long, Integer> currentCountMap = queryCurrentAssignmentCountMapByPlanIds(planIds);
        Map<Long, Integer> result = new HashMap<>();
        for (BizInternshipPlan plan : plans) {
            result.put(plan.getId(), calculateRemainingQuota(plan.getStudentQuota(), currentCountMap.get(plan.getId())));
        }
        return result;
    }

    private Map<Long, Integer> queryRemainingQuotaMapByPlanIdAndBaseIds(Long planId, Set<Long> baseIds) {
        if (planId == null || CollectionUtils.isEmpty(baseIds)) {
            return new HashMap<>();
        }
        List<BizPlanBase> planBases = planBaseMapper.selectList(new LambdaQueryWrapper<BizPlanBase>()
                .eq(BizPlanBase::getPlanId, planId)
                .eq(BizPlanBase::getDeleted, 0L)
                .in(BizPlanBase::getBaseId, baseIds));
        if (CollectionUtils.isEmpty(planBases)) {
            return new HashMap<>();
        }
        Map<Long, Integer> currentCountMap = queryCurrentAssignmentCountMapByPlanAndBaseId(planId);
        Map<Long, Integer> result = new HashMap<>();
        for (BizPlanBase planBase : planBases) {
            result.put(planBase.getBaseId(), calculateRemainingQuota(planBase.getBaseQuota(), currentCountMap.get(planBase.getBaseId())));
        }
        return result;
    }

    private Map<Long, Integer> queryCurrentAssignmentCountMapByPlanIds(Set<Long> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return new HashMap<>();
        }
        List<BizAssignment> assignments = assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                .in(BizAssignment::getPlanId, planIds)
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L));
        if (CollectionUtils.isEmpty(assignments)) {
            return new HashMap<>();
        }
        return assignments.stream()
                .filter(item -> item.getPlanId() != null)
                .collect(Collectors.groupingBy(BizAssignment::getPlanId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    private Map<Long, Integer> queryCurrentAssignmentCountMapByPlanAndBaseId(Long planId) {
        if (planId == null) {
            return new HashMap<>();
        }
        List<BizAssignment> assignments = queryCurrentAssignmentsByPlan(planId);
        if (CollectionUtils.isEmpty(assignments)) {
            return new HashMap<>();
        }
        return assignments.stream()
                .filter(item -> item.getBaseId() != null)
                .collect(Collectors.groupingBy(BizAssignment::getBaseId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    private Integer calculateRemainingQuota(Integer totalQuota, Integer currentCount) {
        int quota = totalQuota == null ? 0 : totalQuota;
        int used = currentCount == null ? 0 : currentCount;
        return Math.max(quota - used, 0);
    }

    private int countTeacherEvaluations(Long assignmentId,
                                        Long evaluatorId,
                                        String roleCode,
                                        String evaluationType,
                                        Long materialVersionId) {
        LambdaQueryWrapper<BizEvaluation> wrapper = new LambdaQueryWrapper<BizEvaluation>()
                .eq(BizEvaluation::getAssignmentId, assignmentId)
                .eq(BizEvaluation::getEvaluatorId, evaluatorId)
                .eq(BizEvaluation::getEvaluatorRole, normalizeCode(roleCode))
                .eq(BizEvaluation::getEvaluationType, evaluationType)
                .eq(BizEvaluation::getDeleted, 0L);
        if (materialVersionId == null) {
            wrapper.isNull(BizEvaluation::getMaterialVersionId);
        } else {
            wrapper.eq(BizEvaluation::getMaterialVersionId, materialVersionId);
        }
        return Math.toIntExact(evaluationMapper.selectCount(wrapper));
    }

    @Transactional(rollbackFor = Exception.class)
    public PlanAttachmentVO uploadAttachment(Long planId,
                                             MultipartFile file,
                                             Long userId,
                                             String roleCode) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "上传文件不能为空");
        }

        BizInternshipPlan plan = requirePlan(planId);
        String normalizedRoleCode = normalizeCode(roleCode);
        SysUser currentUser = requireUser(userId);
        ensureManageAccess(plan, currentUser, normalizedRoleCode);
        if (STATUS_ARCHIVED.equals(plan.getPlanStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "已归档计划只读，不允许修改");
        }

        int configuredMb = paramService.getIntValue(PARAM_FILE_MAX_SIZE_MB, fileStorageProperties.getMaxFileSizeMb());
        long maxBytes = (long) Math.max(1, Math.min(configuredMb, 200)) * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "文件大小超出限制");
        }

        String originalFileName = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename()
                : "unknown";
        String fileExt = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.hasText(fileExt)) {
            storedFileName = storedFileName + "." + fileExt;
        }

        Path rootPath = Paths.get(fileStorageProperties.getRootPath());
        Path planDir = rootPath.resolve(Paths.get("plan", String.valueOf(planId)));
        String relativePath = Paths.get("plan", String.valueOf(planId), storedFileName).toString().replace("\\", "/");
        Path fullPath = planDir.resolve(storedFileName);

        try {
            Files.createDirectories(planDir);
            file.transferTo(fullPath.toFile());
        } catch (IOException ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "附件保存失败");
        }

        BizPlanAttachment attachment = new BizPlanAttachment();
        attachment.setPlanId(planId);
        attachment.setFileName(originalFileName);
        attachment.setFilePath(relativePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileExt(fileExt);
        attachment.setMimeType(file.getContentType());
        attachment.setUploadedBy(userId);
        attachment.setUploadedTime(LocalDateTime.now());
        attachment.setCreatedBy(userId);
        attachment.setUpdatedBy(userId);
        attachment.setDeleted(0L);
        attachmentMapper.insert(attachment);

        return toPlanAttachmentVO(attachment);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long attachmentId, Long userId, String roleCode) {
        BizPlanAttachment attachment = requireAttachment(attachmentId);
        BizInternshipPlan plan = requirePlan(attachment.getPlanId());

        String normalizedRoleCode = normalizeCode(roleCode);
        SysUser currentUser = requireUser(userId);
        ensureManageAccess(plan, currentUser, normalizedRoleCode);
        if (STATUS_ARCHIVED.equals(plan.getPlanStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "已归档计划只读，不允许修改");
        }

        attachment.setDeleted(System.currentTimeMillis());
        attachment.setUpdatedBy(userId);
        attachment.setUpdatedTime(LocalDateTime.now());
        attachmentMapper.updateById(attachment);

        Path fullPath = Paths.get(fileStorageProperties.getRootPath()).resolve(attachment.getFilePath());
        try {
            Files.deleteIfExists(fullPath);
        } catch (IOException ignored) {
            // keep DB consistency even if file cleanup fails
        }
    }

    public ResponseEntity<Resource> downloadAttachment(Long attachmentId, Long userId, String roleCode) {
        BizPlanAttachment attachment = requireAttachment(attachmentId);
        BizInternshipPlan plan = requirePlan(attachment.getPlanId());

        String normalizedRoleCode = normalizeCode(roleCode);
        SysUser currentUser = requireUser(userId);
        ensureReadAccess(plan, currentUser, normalizedRoleCode, ROLE_STUDENT.equals(normalizedRoleCode));

        Path filePath = resolveSafeStoredFile(attachment.getFilePath());
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "附件文件不存在");
        }

        String contentType = resolveAttachmentContentType(attachment, filePath);
        ContentDisposition disposition = ContentDisposition
                .attachment()
                .filename(attachment.getFileName(), StandardCharsets.UTF_8)
                .build();

        try {
            InputStream inputStream = Files.newInputStream(filePath);
            Resource resource = new InputStreamResource(inputStream);
            long contentLength = attachment.getFileSize() == null ? Files.size(filePath) : attachment.getFileSize();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(contentLength)
                    .body(resource);
        } catch (IOException ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "附件下载失败");
        }
    }

    public List<PlanMaterialTypeVO> queryMaterialTypeConfig(Long planId, Long userId, String roleCode, boolean studentView) {
        BizInternshipPlan plan = requirePlan(planId);
        SysUser user = requireUser(userId);
        ensureReadAccess(plan, user, normalizeCode(roleCode), studentView);
        return queryMaterialTypeVOList(planId);
    }

    public List<IdNameOptionVO> queryEnabledBaseOptions() {
        return internshipBaseMapper.selectList(new LambdaQueryWrapper<BaseInternshipBase>()
                        .eq(BaseInternshipBase::getDeleted, 0L)
                        .eq(BaseInternshipBase::getStatus, STATUS_ENABLED)
                        .orderByAsc(BaseInternshipBase::getBaseName)
                        .orderByAsc(BaseInternshipBase::getId))
                .stream()
                .map(this::toBaseOption)
                .collect(Collectors.toList());
    }

    public List<IdNameOptionVO> queryManageDepartmentOptions(Long userId, String roleCode) {
        String normalizedRoleCode = normalizeCode(roleCode);
        if (ROLE_SYS_ADMIN.equals(normalizedRoleCode)) {
            return queryEnabledDepartmentOptions();
        }

        SysUser currentUser = requireUser(userId);
        if (currentUser.getDeptId() == null) {
            return new ArrayList<>();
        }

        return departmentMapper.selectList(new LambdaQueryWrapper<BaseDepartment>()
                        .eq(BaseDepartment::getDeleted, 0L)
                        .eq(BaseDepartment::getStatus, STATUS_ENABLED)
                        .eq(BaseDepartment::getId, currentUser.getDeptId())
                        .last("LIMIT 1"))
                .stream()
                .map(this::toDepartmentOption)
                .collect(Collectors.toList());
    }

    private List<IdNameOptionVO> queryEnabledDepartmentOptions() {
        return departmentMapper.selectList(new LambdaQueryWrapper<BaseDepartment>()
                        .eq(BaseDepartment::getDeleted, 0L)
                        .eq(BaseDepartment::getStatus, STATUS_ENABLED)
                        .orderByAsc(BaseDepartment::getDeptName))
                .stream()
                .map(this::toDepartmentOption)
                .collect(Collectors.toList());
    }

    private BizInternshipPlan requirePlan(Long planId) {
        if (planId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "计划标识不能为空");
        }
        BizInternshipPlan plan = planMapper.selectOne(new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getId, planId)
                .eq(BizInternshipPlan::getDeleted, 0L)
                .last("LIMIT 1"));
        if (plan == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "计划不存在");
        }
        return plan;
    }

    private BizPlanAttachment requireAttachment(Long attachmentId) {
        if (attachmentId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "附件标识不能为空");
        }
        BizPlanAttachment attachment = attachmentMapper.selectOne(new LambdaQueryWrapper<BizPlanAttachment>()
                .eq(BizPlanAttachment::getId, attachmentId)
                .eq(BizPlanAttachment::getDeleted, 0L)
                .last("LIMIT 1"));
        if (attachment == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "附件不存在");
        }
        return attachment;
    }

    private SysUser requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), "当前用户不存在");
        }
        return user;
    }

    private void ensureManagePermission(SysUser currentUser, String roleCode, Long targetDeptId) {
        if (!ROLE_SYS_ADMIN.equals(roleCode) && !ROLE_DEPT_ADMIN.equals(roleCode)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权管理该计划");
        }
        if (ROLE_DEPT_ADMIN.equals(roleCode) && !Objects.equals(currentUser.getDeptId(), targetDeptId)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权管理其他院系的计划");
        }
    }

    private void ensureManageAccess(BizInternshipPlan plan, SysUser currentUser, String roleCode) {
        if (!ROLE_SYS_ADMIN.equals(roleCode) && !ROLE_DEPT_ADMIN.equals(roleCode)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权管理该计划");
        }
        if (ROLE_DEPT_ADMIN.equals(roleCode) && !Objects.equals(plan.getDeptId(), currentUser.getDeptId())) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权管理其他院系的计划");
        }
    }

    private void ensureReadAccess(BizInternshipPlan plan, SysUser currentUser, String roleCode, boolean studentView) {
        if (ROLE_SYS_ADMIN.equals(roleCode)) {
            return;
        }
        if (ROLE_DEPT_ADMIN.equals(roleCode)) {
            if (!Objects.equals(plan.getDeptId(), currentUser.getDeptId())) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看其他院系的计划");
            }
            return;
        }
        if (studentView || ROLE_STUDENT.equals(roleCode)) {
            if (!Objects.equals(plan.getDeptId(), currentUser.getDeptId())) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "师范生无权查看其他院系的计划");
            }
            if (!visibleStatusesForStudent().contains(plan.getPlanStatus())) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "师范生只能查看已发布的计划");
            }
            return;
        }
        if (STATUS_DRAFT.equals(plan.getPlanStatus())) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看草稿计划");
        }
    }

    private List<String> visibleStatusesForStudent() {
        List<String> statuses = new ArrayList<>();
        statuses.add(STATUS_PUBLISHED);
        statuses.add(STATUS_FINISHED);
        statuses.add(STATUS_ARCHIVED);
        return statuses;
    }

    private void validateSaveRequest(PlanSaveRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "开始时间和结束时间不能为空");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "开始时间必须早于结束时间");
        }
        if (request.getApplyDeadline() != null && request.getApplyDeadline().isAfter(request.getEndTime())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "报名截止时间不能晚于结束时间");
        }

        validateTeacherWeights(request.getInnerTeacherWeight(), request.getBaseTeacherWeight());
        validatePlanBaseRequestList(request.getPlanBases(), request.getStudentQuota());
        validateMaterialRequestList(request.getMaterialTypes(), request.getStartTime(), request.getEndTime());
    }

    private void validatePlanBaseRequestList(List<PlanBaseRequest> planBases, Integer studentQuota) {
        if (CollectionUtils.isEmpty(planBases)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "至少配置一个实习基地");
        }
        if (studentQuota == null || studentQuota <= 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "学生名额必须大于 0");
        }

        Set<Long> baseIdSet = new HashSet<>();
        int totalQuota = 0;
        for (PlanBaseRequest item : planBases) {
            if (item.getBaseId() == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "实习基地标识不能为空");
            }
            if (!baseIdSet.add(item.getBaseId())) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "实习基地不能重复配置：" + item.getBaseId());
            }
            BaseInternshipBase base = requireEnabledBase(item.getBaseId());
            if (base == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "无效的实习基地：" + item.getBaseId());
            }
            if (item.getBaseQuota() == null || item.getBaseQuota() <= 0) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "基地名额必须大于 0");
            }
            if (item.getSortNo() == null || item.getSortNo() <= 0) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "排序号必须大于 0");
            }
            totalQuota += item.getBaseQuota();
        }

        if (totalQuota != studentQuota) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "基地名额总和必须等于学生名额");
        }
    }

    private void validateTeacherWeights(BigDecimal innerWeight, BigDecimal baseWeight) {
        BigDecimal inner = normalizeWeight(innerWeight);
        BigDecimal base = normalizeWeight(baseWeight);
        BigDecimal sum = inner.add(base).setScale(2, RoundingMode.HALF_UP);
        if (sum.compareTo(HUNDRED) != 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "校内导师权重与基地导师权重之和必须等于 100");
        }
    }

    private void validateMaterialRequestList(List<PlanMaterialTypeRequest> materialTypes,
                                             LocalDateTime planStartTime,
                                             LocalDateTime planEndTime) {
        if (CollectionUtils.isEmpty(materialTypes)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "至少配置一种材料类型");
        }

        Set<String> typeCodeSet = new HashSet<>();
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (PlanMaterialTypeRequest item : materialTypes) {
            String typeCode = normalizeCode(item.getTypeCode());
            if (!typeCodeSet.add(typeCode)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料类型编码重复：" + item.getTypeCode());
            }
            if (item.getDeadlineTime() == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "每种材料都必须设置截止时间");
            }
            if (item.getDeadlineTime().isAfter(planEndTime)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料截止时间不能晚于计划结束时间");
            }
            if (item.getDeadlineTime().isBefore(planStartTime)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料截止时间不能早于计划开始时间");
            }
            totalWeight = totalWeight.add(normalizeWeight(item.getWeight()));
        }

        if (totalWeight.setScale(2, RoundingMode.HALF_UP).compareTo(HUNDRED) != 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料权重总和必须等于 100");
        }
    }

    private void validateMaterialConfigurationForPublish(Long planId,
                                                         LocalDateTime planStartTime,
                                                         LocalDateTime planEndTime) {
        List<PlanMaterialTypeVO> materials = queryMaterialTypeVOList(planId);
        if (CollectionUtils.isEmpty(materials)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "发布前必须先配置材料类型");
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        Set<String> typeCodeSet = new HashSet<>();
        for (PlanMaterialTypeVO item : materials) {
            String typeCode = normalizeCode(item.getTypeCode());
            if (!typeCodeSet.add(typeCode)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料类型编码重复：" + item.getTypeCode());
            }
            if (item.getDeadlineTime().isAfter(planEndTime) || item.getDeadlineTime().isBefore(planStartTime)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料截止时间超出了计划时间范围");
            }
            totalWeight = totalWeight.add(normalizeWeight(item.getWeight()));
        }
        if (totalWeight.setScale(2, RoundingMode.HALF_UP).compareTo(HUNDRED) != 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "材料权重总和必须等于 100");
        }
    }

    private void validatePlanBaseConfigurationForPublish(Long planId, Integer studentQuota) {
        List<BizPlanBase> planBases = planBaseMapper.selectList(new LambdaQueryWrapper<BizPlanBase>()
                .eq(BizPlanBase::getPlanId, planId)
                .eq(BizPlanBase::getDeleted, 0L)
                .orderByAsc(BizPlanBase::getSortNo)
                .orderByAsc(BizPlanBase::getId));
        if (CollectionUtils.isEmpty(planBases)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "发布前必须先配置实习基地");
        }

        Set<Long> baseIdSet = new HashSet<>();
        int enabledQuota = 0;
        boolean hasEnabledBase = false;
        for (BizPlanBase item : planBases) {
            if (item.getBaseId() == null || !baseIdSet.add(item.getBaseId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "存在重复的实习基地配置");
            }
            BaseInternshipBase base = requireEnabledBase(item.getBaseId());
            if (StringUtils.hasText(item.getStatus()) && STATUS_ENABLED.equals(normalizeCode(item.getStatus()))) {
                hasEnabledBase = true;
                enabledQuota += item.getBaseQuota() == null ? 0 : item.getBaseQuota();
                if (base == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "已配置的实习基地已禁用");
                }
            }
        }

        if (!hasEnabledBase) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "发布前至少需要一个启用中的实习基地");
        }
        if (studentQuota == null || enabledQuota != studentQuota) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "发布前启用基地的名额总和必须等于学生名额");
        }
    }

    private void normalizePlanDefaultQuota(PlanSaveRequest request) {
        if (request == null) {
            return;
        }
        if (request.getStudentQuota() != null && request.getStudentQuota() > 0) {
            return;
        }
        int configuredQuota = paramService.getIntValue(PARAM_PLAN_DEFAULT_QUOTA, DEFAULT_PLAN_DEFAULT_QUOTA);
        request.setStudentQuota(Math.max(configuredQuota, 1));
    }

    private void ensurePlanCodeUnique(String planCode, Long excludePlanId) {
        BizInternshipPlan existing = planMapper.selectOne(new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getPlanCode, planCode)
                .eq(BizInternshipPlan::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing == null) {
            return;
        }
        if (excludePlanId == null || !excludePlanId.equals(existing.getId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "计划编码已存在");
        }
    }

    private void applySaveRequest(BizInternshipPlan plan, PlanSaveRequest request) {
        if (!StringUtils.hasText(plan.getPlanCode())) {
            plan.setPlanCode(generatePlanCode());
        }
        plan.setPlanName(request.getPlanName().trim());
        plan.setAcademicYear(request.getAcademicYear().trim());
        plan.setTerm(request.getTerm().trim());
        plan.setDeptId(request.getDeptId());
        plan.setStartTime(request.getStartTime());
        plan.setEndTime(request.getEndTime());
        plan.setApplyDeadline(request.getApplyDeadline());
        plan.setStudentQuota(request.getStudentQuota());
        plan.setDescription(request.getDescription());
        plan.setInnerTeacherWeight(normalizeWeight(request.getInnerTeacherWeight()));
        plan.setBaseTeacherWeight(normalizeWeight(request.getBaseTeacherWeight()));
    }

    private void replacePlanBases(Long planId, List<PlanBaseRequest> planBases, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        planBaseMapper.update(null, new LambdaUpdateWrapper<BizPlanBase>()
                .eq(BizPlanBase::getPlanId, planId)
                .eq(BizPlanBase::getDeleted, 0L)
                .set(BizPlanBase::getDeleted, System.currentTimeMillis())
                .set(BizPlanBase::getUpdatedBy, userId)
                .set(BizPlanBase::getUpdatedTime, now));

        for (PlanBaseRequest item : planBases) {
            BizPlanBase entity = new BizPlanBase();
            entity.setPlanId(planId);
            entity.setBaseId(item.getBaseId());
            entity.setBaseQuota(item.getBaseQuota());
            entity.setSortNo(item.getSortNo());
            entity.setStatus(StringUtils.hasText(item.getStatus()) ? normalizeCode(item.getStatus()) : STATUS_ENABLED);
            entity.setRemark(StringUtils.hasText(item.getRemark()) ? item.getRemark().trim() : null);
            entity.setCreatedBy(userId);
            entity.setUpdatedBy(userId);
            entity.setDeleted(0L);
            planBaseMapper.insert(entity);
        }
    }

    private void replaceMaterialTypes(Long planId, List<PlanMaterialTypeRequest> materialTypes, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        materialTypeMapper.update(null, new LambdaUpdateWrapper<BizMaterialType>()
                .eq(BizMaterialType::getPlanId, planId)
                .eq(BizMaterialType::getDeleted, 0L)
                .set(BizMaterialType::getDeleted, System.currentTimeMillis())
                .set(BizMaterialType::getUpdatedBy, userId)
                .set(BizMaterialType::getUpdatedTime, now));

        for (PlanMaterialTypeRequest item : materialTypes) {
            BizMaterialType entity = new BizMaterialType();
            entity.setPlanId(planId);
            entity.setTypeCode(normalizeCode(item.getTypeCode()));
            entity.setTypeName(item.getTypeName().trim());
            entity.setRequiredFlag(item.getRequiredFlag() != null && item.getRequiredFlag() == 1 ? 1 : 0);
            entity.setDeadlineTime(item.getDeadlineTime());
            entity.setWeight(normalizeWeight(item.getWeight()));

            boolean allowResubmit = Boolean.TRUE.equals(item.getAllowResubmit());
            int maxSubmitCount;
            if (allowResubmit) {
                maxSubmitCount = item.getMaxSubmitCount() != null && item.getMaxSubmitCount() > 1
                        ? item.getMaxSubmitCount()
                        : 10;
            } else {
                maxSubmitCount = 1;
            }
            entity.setMaxSubmitCount(maxSubmitCount);
            entity.setStatus(StringUtils.hasText(item.getStatus()) ? normalizeCode(item.getStatus()) : "ENABLED");
            entity.setCreatedBy(userId);
            entity.setUpdatedBy(userId);
            entity.setDeleted(0L);
            materialTypeMapper.insert(entity);
        }
    }

    private PlanDetailVO buildPlanDetail(BizInternshipPlan plan) {
        PlanDetailVO detail = new PlanDetailVO();
        int currentAssignmentCount = queryCurrentAssignmentsByPlan(plan.getId()).size();
        detail.setId(plan.getId());
        detail.setPlanCode(plan.getPlanCode());
        detail.setPlanName(plan.getPlanName());
        detail.setAcademicYear(plan.getAcademicYear());
        detail.setTerm(plan.getTerm());
        detail.setDeptId(plan.getDeptId());
        detail.setDeptName(resolveDeptName(plan.getDeptId()));
        detail.setStartTime(plan.getStartTime());
        detail.setEndTime(plan.getEndTime());
        detail.setApplyDeadline(plan.getApplyDeadline());
        detail.setStudentQuota(plan.getStudentQuota());
        detail.setRemainingQuota(calculateRemainingQuota(plan.getStudentQuota(), currentAssignmentCount));
        detail.setDescription(plan.getDescription());
        detail.setInnerTeacherWeight(plan.getInnerTeacherWeight());
        detail.setBaseTeacherWeight(plan.getBaseTeacherWeight());
        detail.setPlanStatus(plan.getPlanStatus());
        detail.setScorePublishStatus(plan.getScorePublishStatus());
        detail.setPublishedTime(plan.getPublishedTime());
        detail.setArchivedTime(plan.getArchivedTime());
        detail.setPlanBases(queryPlanBaseVOList(plan.getId()));
        detail.setMaterialTypes(queryMaterialTypeVOList(plan.getId()));
        detail.setAttachments(queryAttachmentVOList(plan.getId()));
        return detail;
    }

    private List<PlanBaseVO> queryPlanBaseVOList(Long planId) {
        List<BizPlanBase> entities = planBaseMapper.selectList(new LambdaQueryWrapper<BizPlanBase>()
                .eq(BizPlanBase::getPlanId, planId)
                .eq(BizPlanBase::getDeleted, 0L)
                .orderByAsc(BizPlanBase::getSortNo)
                .orderByAsc(BizPlanBase::getId));
        Set<Long> baseIds = entities.stream().map(BizPlanBase::getBaseId).collect(Collectors.toSet());
        List<BaseInternshipBase> bases = CollectionUtils.isEmpty(baseIds)
                ? new ArrayList<>()
                : internshipBaseMapper.selectList(new LambdaQueryWrapper<BaseInternshipBase>()
                .in(BaseInternshipBase::getId, baseIds)
                .eq(BaseInternshipBase::getDeleted, 0L));
        java.util.Map<Long, BaseInternshipBase> baseMap = bases.stream()
                .collect(Collectors.toMap(BaseInternshipBase::getId, item -> item, (left, right) -> left));
        Map<Long, Integer> remainingQuotaMap = queryRemainingQuotaMapByPlanIdAndBaseIds(planId, baseIds);
        return entities.stream()
                .map(item -> toPlanBaseVO(item, baseMap.get(item.getBaseId()), remainingQuotaMap.get(item.getBaseId())))
                .collect(Collectors.toList());
    }

    private List<PlanMaterialTypeVO> queryMaterialTypeVOList(Long planId) {
        List<BizMaterialType> entities = materialTypeMapper.selectList(new LambdaQueryWrapper<BizMaterialType>()
                .eq(BizMaterialType::getPlanId, planId)
                .eq(BizMaterialType::getDeleted, 0L)
                .orderByAsc(BizMaterialType::getDeadlineTime)
                .orderByAsc(BizMaterialType::getId));
        return entities.stream().map(this::toPlanMaterialTypeVO).collect(Collectors.toList());
    }

    private List<PlanAttachmentVO> queryAttachmentVOList(Long planId) {
        List<BizPlanAttachment> entities = attachmentMapper.selectList(new LambdaQueryWrapper<BizPlanAttachment>()
                .eq(BizPlanAttachment::getPlanId, planId)
                .eq(BizPlanAttachment::getDeleted, 0L)
                .orderByDesc(BizPlanAttachment::getUploadedTime)
                .orderByDesc(BizPlanAttachment::getId));
        return entities.stream().map(this::toPlanAttachmentVO).collect(Collectors.toList());
    }

    private PlanListItemVO toPlanListItemVO(BizInternshipPlan entity, Integer remainingQuota) {
        PlanListItemVO vo = new PlanListItemVO();
        vo.setId(entity.getId());
        vo.setPlanCode(entity.getPlanCode());
        vo.setPlanName(entity.getPlanName());
        vo.setAcademicYear(entity.getAcademicYear());
        vo.setTerm(entity.getTerm());
        vo.setDeptId(entity.getDeptId());
        vo.setDeptName(resolveDeptName(entity.getDeptId()));
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setApplyDeadline(entity.getApplyDeadline());
        vo.setStudentQuota(entity.getStudentQuota());
        vo.setRemainingQuota(remainingQuota);
        vo.setInnerTeacherWeight(entity.getInnerTeacherWeight());
        vo.setBaseTeacherWeight(entity.getBaseTeacherWeight());
        vo.setPlanStatus(entity.getPlanStatus());
        vo.setPublishedTime(entity.getPublishedTime());
        vo.setArchivedTime(entity.getArchivedTime());
        return vo;
    }

    private PlanMaterialTypeVO toPlanMaterialTypeVO(BizMaterialType entity) {
        PlanMaterialTypeVO vo = new PlanMaterialTypeVO();
        vo.setId(entity.getId());
        vo.setPlanId(entity.getPlanId());
        vo.setTypeCode(entity.getTypeCode());
        vo.setTypeName(entity.getTypeName());
        vo.setRequiredFlag(entity.getRequiredFlag());
        vo.setAllowResubmit(entity.getMaxSubmitCount() != null && entity.getMaxSubmitCount() > 1);
        vo.setMaxSubmitCount(entity.getMaxSubmitCount());
        vo.setDeadlineTime(entity.getDeadlineTime());
        vo.setWeight(entity.getWeight());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    private PlanAttachmentVO toPlanAttachmentVO(BizPlanAttachment entity) {
        PlanAttachmentVO vo = new PlanAttachmentVO();
        vo.setId(entity.getId());
        vo.setPlanId(entity.getPlanId());
        vo.setFileName(entity.getFileName());
        vo.setFilePath(entity.getFilePath());
        vo.setDownloadUrl("/api/v1/plan/attachment/download/" + entity.getId());
        vo.setFileSize(entity.getFileSize());
        vo.setFileExt(entity.getFileExt());
        vo.setMimeType(entity.getMimeType());
        vo.setUploadedBy(entity.getUploadedBy());
        vo.setUploadedTime(entity.getUploadedTime());
        return vo;
    }

    private Path resolveSafeStoredFile(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "附件文件不存在");
        }
        Path root = Paths.get(fileStorageProperties.getRootPath());
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "附件路径非法");
        }
        return target;
    }

    private String resolveAttachmentContentType(BizPlanAttachment attachment, Path filePath) {
        if (StringUtils.hasText(attachment.getMimeType())) {
            return attachment.getMimeType();
        }
        try {
            String detected = Files.probeContentType(filePath);
            if (StringUtils.hasText(detected)) {
                return detected;
            }
        } catch (IOException ignored) {
            // fall through to default
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private PlanBaseVO toPlanBaseVO(BizPlanBase entity, BaseInternshipBase base, Integer remainingQuota) {
        PlanBaseVO vo = new PlanBaseVO();
        vo.setId(entity.getId());
        vo.setPlanId(entity.getPlanId());
        vo.setBaseId(entity.getBaseId());
        vo.setBaseCode(base == null ? null : base.getBaseCode());
        vo.setBaseName(base == null ? null : base.getBaseName());
        vo.setBaseQuota(entity.getBaseQuota());
        vo.setRemainingQuota(remainingQuota);
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private IdNameOptionVO toBaseOption(BaseInternshipBase entity) {
        IdNameOptionVO vo = new IdNameOptionVO();
        vo.setId(entity.getId());
        vo.setName(StringUtils.hasText(entity.getBaseCode())
                ? entity.getBaseName() + " (" + entity.getBaseCode() + ")"
                : entity.getBaseName());
        return vo;
    }

    private BigDecimal normalizeWeight(BigDecimal weight) {
        if (weight == null) {
            return BigDecimal.ZERO;
        }
        return weight.setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveDeptName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        BaseDepartment department = departmentMapper.selectOne(new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getId, deptId)
                .eq(BaseDepartment::getDeleted, 0L)
                .last("LIMIT 1"));
        return department == null ? null : department.getDeptName();
    }

    private BaseInternshipBase requireEnabledBase(Long baseId) {
        return internshipBaseMapper.selectOne(new LambdaQueryWrapper<BaseInternshipBase>()
                .eq(BaseInternshipBase::getId, baseId)
                .eq(BaseInternshipBase::getDeleted, 0L)
                .eq(BaseInternshipBase::getStatus, STATUS_ENABLED)
                .last("LIMIT 1"));
    }

    private IdNameOptionVO toDepartmentOption(BaseDepartment department) {
        IdNameOptionVO option = new IdNameOptionVO();
        option.setId(department.getId());
        option.setName(department.getDeptName());
        return option;
    }

    private String normalizeCode(String text) {
        return text == null ? "" : text.trim().toUpperCase(Locale.ROOT);
    }

    private String generatePlanCode() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String timestamp = LocalDateTime.now().format(formatter);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
        String planCode = "PLAN" + timestamp + random;
        ensurePlanCodeUnique(planCode, null);
        return planCode;
    }
    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1);
    }
}
