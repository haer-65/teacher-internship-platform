package com.teacher.internship.modules.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.notice.service.NoticeTriggerService;
import com.teacher.internship.modules.application.entity.BizAssignment;
import com.teacher.internship.modules.application.mapper.BizAssignmentMapper;
import com.teacher.internship.modules.evaluation.dto.EvaluationScoreItemRequest;
import com.teacher.internship.modules.evaluation.dto.FinalEvaluationSubmitRequest;
import com.teacher.internship.modules.evaluation.dto.ProcessEvaluationSubmitRequest;
import com.teacher.internship.modules.evaluation.entity.BizEvaluation;
import com.teacher.internship.modules.evaluation.mapper.BizEvaluationMapper;
import com.teacher.internship.modules.evaluation.vo.EvaluationRecordVO;
import com.teacher.internship.modules.evaluation.vo.EvaluationScoreItemVO;
import com.teacher.internship.modules.evaluation.vo.EvaluationScoreSummaryVO;
import com.teacher.internship.modules.evaluation.vo.FinalEvaluationDetailVO;
import com.teacher.internship.modules.evaluation.vo.ProcessEvaluationDetailVO;
import com.teacher.internship.modules.evaluation.vo.StudentEvaluationItemVO;
import com.teacher.internship.modules.evaluation.vo.StudentEvaluationPageVO;
import com.teacher.internship.modules.evaluation.vo.TeacherFinalPendingItemVO;
import com.teacher.internship.modules.evaluation.vo.TeacherFinalPendingPageVO;
import com.teacher.internship.modules.evaluation.vo.TeacherProcessPendingItemVO;
import com.teacher.internship.modules.evaluation.vo.TeacherProcessPendingPageVO;
import com.teacher.internship.modules.material.entity.BizMaterial;
import com.teacher.internship.modules.material.entity.BizMaterialVersion;
import com.teacher.internship.modules.material.mapper.BizMaterialMapper;
import com.teacher.internship.modules.material.mapper.BizMaterialVersionMapper;
import com.teacher.internship.modules.plan.entity.BizInternshipPlan;
import com.teacher.internship.modules.plan.entity.BizMaterialType;
import com.teacher.internship.modules.plan.mapper.BizInternshipPlanMapper;
import com.teacher.internship.modules.plan.mapper.BizMaterialTypeMapper;
import com.teacher.internship.modules.score.service.ScoreService;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EvaluationService {

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_INNER_TEACHER = "INNER_TEACHER";
    private static final String ROLE_BASE_TEACHER = "BASE_TEACHER";
    private static final String ROLE_DEPT_ADMIN = "DEPT_ADMIN";
    private static final String ROLE_ACADEMIC_ADMIN = "ACADEMIC_ADMIN";
    private static final String ROLE_SYS_ADMIN = "SYS_ADMIN";
    private static final String TYPE_PROCESS = "PROCESS";
    private static final String TYPE_FINAL = "FINAL";

    private static final String PLAN_STATUS_FINISHED = "FINISHED";
    private static final String PLAN_STATUS_ARCHIVED = "ARCHIVED";
    private static final BigDecimal SCORE_BASE = new BigDecimal("100");

    private final BizEvaluationMapper evaluationMapper;
    private final BizAssignmentMapper assignmentMapper;
    private final BizMaterialMapper materialMapper;
    private final BizMaterialVersionMapper materialVersionMapper;
    private final BizMaterialTypeMapper materialTypeMapper;
    private final BizInternshipPlanMapper planMapper;
    private final ScoreService scoreService;
    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final NoticeTriggerService noticeTriggerService;

    public EvaluationService(BizEvaluationMapper evaluationMapper,
                             BizAssignmentMapper assignmentMapper,
                             BizMaterialMapper materialMapper,
                             BizMaterialVersionMapper materialVersionMapper,
                             BizMaterialTypeMapper materialTypeMapper,
                             BizInternshipPlanMapper planMapper,
                             ScoreService scoreService,
                             SysUserMapper userMapper,
                             ObjectMapper objectMapper,
                             NoticeTriggerService noticeTriggerService) {
        this.evaluationMapper = evaluationMapper;
        this.assignmentMapper = assignmentMapper;
        this.materialMapper = materialMapper;
        this.materialVersionMapper = materialVersionMapper;
        this.materialTypeMapper = materialTypeMapper;
        this.planMapper = planMapper;
        this.scoreService = scoreService;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.noticeTriggerService = noticeTriggerService;
    }

    public TeacherProcessPendingPageVO queryTeacherProcessPage(long page,
                                                               long size,
                                                               String keyword,
                                                               Long planId,
                                                               String studentNo,
                                                               Integer pendingOnly,
                                                               Long userId,
                                                               String roleCode) {
        SysUser teacher = requireTeacherOperator(userId, roleCode);
        List<BizAssignment> assignments = queryTeacherCurrentAssignments(teacher.getId(), roleCode);
        if (CollectionUtils.isEmpty(assignments)) {
            return emptyTeacherProcessPage(normalizePage(page), normalizeSize(size));
        }

        Set<Long> assignmentIds = assignments.stream().map(BizAssignment::getId).collect(Collectors.toSet());
        Set<Long> planIds = assignments.stream().map(BizAssignment::getPlanId).collect(Collectors.toSet());
        Set<Long> studentIds = assignments.stream().map(BizAssignment::getStudentId).collect(Collectors.toSet());

        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);
        Map<Long, SysUser> studentMap = queryUserMap(studentIds);

        List<BizMaterial> materials = materialMapper.selectList(new LambdaQueryWrapper<BizMaterial>()
                .in(BizMaterial::getAssignmentId, assignmentIds)
                .eq(BizMaterial::getDeleted, 0L)
                .gt(BizMaterial::getLatestVersionNo, 0));
        if (CollectionUtils.isEmpty(materials)) {
            return emptyTeacherProcessPage(normalizePage(page), normalizeSize(size));
        }

        Set<Long> materialIds = materials.stream().map(BizMaterial::getId).collect(Collectors.toSet());
        Set<Long> materialTypeIds = materials.stream().map(BizMaterial::getMaterialTypeId).collect(Collectors.toSet());

        Map<Long, BizMaterialType> materialTypeMap = queryMaterialTypeMap(materialTypeIds);
        Map<Long, BizMaterialVersion> currentVersionMap = materialVersionMapper.selectList(new LambdaQueryWrapper<BizMaterialVersion>()
                        .in(BizMaterialVersion::getMaterialId, materialIds)
                        .eq(BizMaterialVersion::getIsCurrent, 1)
                        .eq(BizMaterialVersion::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BizMaterialVersion::getMaterialId, item -> item, (left, right) -> left));

        Set<Long> versionIds = currentVersionMap.values().stream().map(BizMaterialVersion::getId).collect(Collectors.toSet());
        Map<Long, EvaluationStat> myStatMap = buildEvaluationStatMap(versionIds, teacher.getId(), roleCode, TYPE_PROCESS);

        Map<Long, BizAssignment> assignmentMap = assignments.stream()
                .collect(Collectors.toMap(BizAssignment::getId, item -> item, (left, right) -> left));

        List<TeacherProcessPendingItemVO> allItems = new ArrayList<>();
        for (BizMaterial material : materials) {
            BizAssignment assignment = assignmentMap.get(material.getAssignmentId());
            if (assignment == null) {
                continue;
            }
            BizMaterialVersion version = currentVersionMap.get(material.getId());
            if (version == null) {
                continue;
            }
            BizInternshipPlan plan = planMap.get(assignment.getPlanId());
            SysUser student = studentMap.get(assignment.getStudentId());
            BizMaterialType materialType = materialTypeMap.get(material.getMaterialTypeId());
            if (plan == null || student == null || materialType == null) {
                continue;
            }

            TeacherProcessPendingItemVO item = new TeacherProcessPendingItemVO();
            item.setMaterialId(material.getId());
            item.setMaterialVersionId(version.getId());
            item.setMaterialVersionNo(version.getVersionNo());
            item.setFileName(version.getFileName());
            item.setSubmittedTime(version.getSubmittedTime());
            item.setAssignmentId(assignment.getId());
            item.setPlanId(plan.getId());
            item.setPlanCode(plan.getPlanCode());
            item.setPlanName(plan.getPlanName());
            item.setStudentId(student.getId());
            item.setStudentNo(student.getStudentNo());
            item.setStudentName(student.getRealName());
            item.setMaterialTypeId(materialType.getId());
            item.setMaterialTypeCode(materialType.getTypeCode());
            item.setMaterialTypeName(materialType.getTypeName());

            EvaluationStat stat = myStatMap.get(version.getId());
            int count = stat == null ? 0 : stat.count;
            item.setMyEvaluationCount(count);
            item.setMyLatestEvaluatedTime(stat == null ? null : stat.latestTime);
            item.setMyLatestScore(stat == null ? null : stat.latestScore);
            item.setPending(count <= 0);
            allItems.add(item);
        }

        List<TeacherProcessPendingItemVO> filtered = allItems.stream()
                .filter(item -> filterTeacherProcessItem(item, keyword, planId, studentNo, pendingOnly))
                .sorted(Comparator.comparing(TeacherProcessPendingItemVO::getSubmittedTime, Comparator.nullsLast(LocalDateTime::compareTo)).reversed()
                        .thenComparing(TeacherProcessPendingItemVO::getMaterialVersionId, Comparator.nullsLast(Long::compareTo)).reversed())
                .collect(Collectors.toList());

        return buildTeacherProcessPage(filtered, page, size);
    }

    public ProcessEvaluationDetailVO getTeacherProcessDetail(Long materialVersionId,
                                                             Long userId,
                                                             String roleCode) {
        SysUser teacher = requireTeacherOperator(userId, roleCode);
        BizMaterialVersion version = requireMaterialVersion(materialVersionId);
        BizMaterial material = requireMaterial(version.getMaterialId());
        BizAssignment assignment = requireAssignment(material.getAssignmentId());
        ensureTeacherCanEvaluateAssignment(assignment, teacher.getId(), roleCode);

        BizInternshipPlan plan = requirePlan(assignment.getPlanId());
        SysUser student = requireUser(assignment.getStudentId());
        BizMaterialType materialType = requireMaterialType(material.getMaterialTypeId());

        ProcessEvaluationDetailVO detail = new ProcessEvaluationDetailVO();
        detail.setMaterialId(material.getId());
        detail.setMaterialVersionId(version.getId());
        detail.setMaterialVersionNo(version.getVersionNo());
        detail.setFileName(version.getFileName());
        detail.setFileSize(version.getFileSize());
        detail.setFileExt(version.getFileExt());
        detail.setMimeType(version.getMimeType());
        detail.setPreviewUrl("/api/v1/material/file/preview/" + version.getId());
        detail.setDownloadUrl("/api/v1/material/file/download/" + version.getId());
        detail.setSubmitRemark(version.getSubmitRemark());
        detail.setSubmittedTime(version.getSubmittedTime());

        detail.setAssignmentId(assignment.getId());
        detail.setPlanId(plan.getId());
        detail.setPlanCode(plan.getPlanCode());
        detail.setPlanName(plan.getPlanName());

        detail.setStudentId(student.getId());
        detail.setStudentNo(student.getStudentNo());
        detail.setStudentName(student.getRealName());

        detail.setMaterialTypeId(materialType.getId());
        detail.setMaterialTypeCode(materialType.getTypeCode());
        detail.setMaterialTypeName(materialType.getTypeName());
        detail.setMaterialDeadlineTime(materialType.getDeadlineTime());
        detail.setCanEvaluate(true);

        List<EvaluationRecordVO> records = queryRecordVOByMaterialVersion(version.getId(), teacher.getId(), roleCode, TYPE_PROCESS);
        detail.setRecords(records);
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public EvaluationRecordVO submitProcessEvaluation(ProcessEvaluationSubmitRequest request,
                                                      Long userId,
                                                      String roleCode) {
        SysUser teacher = requireTeacherOperator(userId, roleCode);
        ScoreComputationResult scoreResult = computeScoreItemsAndTotal(request.getScoreItems());
        BigDecimal totalScore = scoreResult.hasScoreItems() ? scoreResult.getComputedScore() : request.getScore();
        validateMainScore(totalScore);

        BizMaterialVersion version = requireMaterialVersion(request.getMaterialVersionId());
        BizMaterial material = requireMaterial(version.getMaterialId());
        BizAssignment assignment = requireAssignment(material.getAssignmentId());
        ensureTeacherCanEvaluateAssignment(assignment, teacher.getId(), roleCode);

        int nextRecordNo = nextRecordNo(assignment.getId(), teacher.getId(), TYPE_PROCESS, version.getId());
        LocalDateTime now = LocalDateTime.now();

        BizEvaluation entity = new BizEvaluation();
        entity.setPlanId(assignment.getPlanId());
        entity.setAssignmentId(assignment.getId());
        entity.setStudentId(assignment.getStudentId());
        entity.setMaterialId(material.getId());
        entity.setMaterialVersionId(version.getId());
        entity.setEvaluatorId(teacher.getId());
        entity.setEvaluatorRole(normalizeCode(roleCode));
        entity.setEvaluationType(TYPE_PROCESS);
        entity.setRecordNo(nextRecordNo);
        entity.setScore(normalizeScore(totalScore));
        entity.setScoreItemsJson(scoreResult.getScoreItemsJson());
        entity.setCommentText(trimToNull(request.getCommentText()));
        entity.setEvaluatedTime(now);
        entity.setCreatedBy(teacher.getId());
        entity.setUpdatedBy(teacher.getId());
        entity.setDeleted(0L);
        evaluationMapper.insert(entity);

        BizInternshipPlan plan = requirePlan(entity.getPlanId());
        BizMaterialType materialType = requireMaterialType(material.getMaterialTypeId());
        noticeTriggerService.notifyMaterialEvaluated(entity, materialType, plan, teacher);

        return toEvaluationRecordVO(entity,
                plan,
                requireUser(entity.getStudentId()),
                material,
                version,
                materialType,
                teacher);
    }

    public TeacherFinalPendingPageVO queryTeacherFinalPage(long page,
                                                           long size,
                                                           String keyword,
                                                           Long planId,
                                                           String studentNo,
                                                           Integer pendingOnly,
                                                           Long userId,
                                                           String roleCode) {
        SysUser teacher = requireTeacherOperator(userId, roleCode);
        List<BizAssignment> assignments = queryTeacherCurrentAssignments(teacher.getId(), roleCode);
        if (CollectionUtils.isEmpty(assignments)) {
            return emptyTeacherFinalPage(normalizePage(page), normalizeSize(size));
        }

        Set<Long> assignmentIds = assignments.stream().map(BizAssignment::getId).collect(Collectors.toSet());
        Set<Long> planIds = assignments.stream().map(BizAssignment::getPlanId).collect(Collectors.toSet());
        Set<Long> studentIds = assignments.stream().map(BizAssignment::getStudentId).collect(Collectors.toSet());

        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);
        Map<Long, SysUser> studentMap = queryUserMap(studentIds);
        Map<Long, EvaluationStat> myStatMap = buildEvaluationStatMapByAssignment(assignmentIds, teacher.getId(), roleCode, TYPE_FINAL);

        List<TeacherFinalPendingItemVO> allItems = new ArrayList<>();
        for (BizAssignment assignment : assignments) {
            BizInternshipPlan plan = planMap.get(assignment.getPlanId());
            SysUser student = studentMap.get(assignment.getStudentId());
            if (plan == null || student == null) {
                continue;
            }
            EvaluationStat stat = myStatMap.get(assignment.getId());
            int count = stat == null ? 0 : stat.count;
            boolean canEvaluate = canSubmitFinalEvaluation(plan) && count <= 0;

            TeacherFinalPendingItemVO item = new TeacherFinalPendingItemVO();
            item.setAssignmentId(assignment.getId());
            item.setPlanId(plan.getId());
            item.setPlanCode(plan.getPlanCode());
            item.setPlanName(plan.getPlanName());
            item.setPlanStatus(plan.getPlanStatus());
            item.setStudentId(student.getId());
            item.setStudentNo(student.getStudentNo());
            item.setStudentName(student.getRealName());
            item.setMyEvaluationCount(count);
            item.setMyLatestEvaluatedTime(stat == null ? null : stat.latestTime);
            item.setMyLatestScore(stat == null ? null : stat.latestScore);
            item.setCanEvaluate(canEvaluate);
            item.setPending(canEvaluate && count <= 0);
            allItems.add(item);
        }

        List<TeacherFinalPendingItemVO> filtered = allItems.stream()
                .filter(item -> filterTeacherFinalItem(item, keyword, planId, studentNo, pendingOnly))
                .sorted(Comparator.comparing(TeacherFinalPendingItemVO::getCanEvaluate).reversed()
                        .thenComparing(TeacherFinalPendingItemVO::getMyLatestEvaluatedTime, Comparator.nullsLast(LocalDateTime::compareTo)).reversed()
                        .thenComparing(TeacherFinalPendingItemVO::getAssignmentId, Comparator.nullsLast(Long::compareTo)).reversed())
                .collect(Collectors.toList());

        return buildTeacherFinalPage(filtered, page, size);
    }

    public FinalEvaluationDetailVO getTeacherFinalDetail(Long assignmentId,
                                                         Long userId,
                                                         String roleCode) {
        SysUser teacher = requireTeacherOperator(userId, roleCode);
        BizAssignment assignment = requireAssignment(assignmentId);
        ensureTeacherCanEvaluateAssignment(assignment, teacher.getId(), roleCode);

        BizInternshipPlan plan = requirePlan(assignment.getPlanId());
        SysUser student = requireUser(assignment.getStudentId());

        FinalEvaluationDetailVO detail = new FinalEvaluationDetailVO();
        detail.setAssignmentId(assignment.getId());
        detail.setPlanId(plan.getId());
        detail.setPlanCode(plan.getPlanCode());
        detail.setPlanName(plan.getPlanName());
        detail.setPlanStatus(plan.getPlanStatus());
        detail.setStudentId(student.getId());
        detail.setStudentNo(student.getStudentNo());
        detail.setStudentName(student.getRealName());
        detail.setInnerTeacherId(assignment.getInnerTeacherId());
        detail.setBaseTeacherId(assignment.getBaseTeacherId());
        int existingCount = countTeacherEvaluations(assignment.getId(), teacher.getId(), roleCode, TYPE_FINAL, null);
        detail.setCanEvaluate(canSubmitFinalEvaluation(plan) && existingCount <= 0);

        List<EvaluationRecordVO> records = queryRecordVOByAssignment(assignment.getId(), teacher.getId(), roleCode, TYPE_FINAL);
        detail.setRecords(records);
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public EvaluationRecordVO submitFinalEvaluation(FinalEvaluationSubmitRequest request,
                                                    Long userId,
                                                    String roleCode) {
        SysUser teacher = requireTeacherOperator(userId, roleCode);
        ScoreComputationResult scoreResult = computeScoreItemsAndTotal(request.getScoreItems());
        BigDecimal totalScore = scoreResult.hasScoreItems() ? scoreResult.getComputedScore() : request.getScore();
        validateMainScore(totalScore);

        BizAssignment assignment = requireAssignment(request.getAssignmentId());
        ensureTeacherCanEvaluateAssignment(assignment, teacher.getId(), roleCode);
        BizInternshipPlan plan = requirePlan(assignment.getPlanId());
        int existingCount = countTeacherEvaluations(assignment.getId(), teacher.getId(), roleCode, TYPE_FINAL, null);
        if (!canSubmitFinalEvaluation(plan)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有在实习结束后才可以提交综合评价");
        }

        if (existingCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "当前角色已提交过综合评价");
        }

        int nextRecordNo = nextRecordNo(assignment.getId(), teacher.getId(), TYPE_FINAL, null);
        LocalDateTime now = LocalDateTime.now();

        BizEvaluation entity = new BizEvaluation();
        entity.setPlanId(assignment.getPlanId());
        entity.setAssignmentId(assignment.getId());
        entity.setStudentId(assignment.getStudentId());
        entity.setMaterialId(null);
        entity.setMaterialVersionId(null);
        entity.setEvaluatorId(teacher.getId());
        entity.setEvaluatorRole(normalizeCode(roleCode));
        entity.setEvaluationType(TYPE_FINAL);
        entity.setRecordNo(nextRecordNo);
        entity.setScore(normalizeScore(totalScore));
        entity.setScoreItemsJson(scoreResult.getScoreItemsJson());
        entity.setCommentText(trimToNull(request.getCommentText()));
        entity.setEvaluatedTime(now);
        entity.setCreatedBy(teacher.getId());
        entity.setUpdatedBy(teacher.getId());
        entity.setDeleted(0L);
        evaluationMapper.insert(entity);

        if (isFinalEvaluationReady(plan.getId())) {
            scoreService.recalculateByPlan(plan.getId(), teacher.getId(), ROLE_SYS_ADMIN);
        }

        return toEvaluationRecordVO(entity,
                plan,
                requireUser(entity.getStudentId()),
                null,
                null,
                null,
                teacher);
    }

    public StudentEvaluationPageVO queryStudentEvaluationPage(long page,
                                                              long size,
                                                              String keyword,
                                                              String evaluationType,
                                                              Long userId,
                                                              String roleCode) {
        SysUser student = requireStudentOperator(userId, roleCode);

        List<BizEvaluation> evaluations = evaluationMapper.selectList(new LambdaQueryWrapper<BizEvaluation>()
                .eq(BizEvaluation::getStudentId, student.getId())
                .eq(BizEvaluation::getDeleted, 0L)
                .orderByDesc(BizEvaluation::getEvaluatedTime)
                .orderByDesc(BizEvaluation::getId));

        if (StringUtils.hasText(evaluationType)) {
            String type = normalizeCode(evaluationType);
            evaluations = evaluations.stream()
                    .filter(item -> type.equals(normalizeCode(item.getEvaluationType())))
                    .collect(Collectors.toList());
        }

        Set<Long> planIds = evaluations.stream().map(BizEvaluation::getPlanId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> materialIds = evaluations.stream().map(BizEvaluation::getMaterialId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> versionIds = evaluations.stream().map(BizEvaluation::getMaterialVersionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> evaluatorIds = evaluations.stream().map(BizEvaluation::getEvaluatorId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);
        Map<Long, BizMaterial> materialMap = queryMaterialMap(materialIds);
        Map<Long, BizMaterialVersion> versionMap = queryMaterialVersionMap(versionIds);
        Set<Long> materialTypeIds = materialMap.values().stream().map(BizMaterial::getMaterialTypeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, BizMaterialType> materialTypeMap = queryMaterialTypeMap(materialTypeIds);
        Map<Long, SysUser> evaluatorMap = queryUserMap(evaluatorIds);

        List<StudentEvaluationItemVO> allItems = evaluations.stream()
                .map(item -> toStudentEvaluationItemVO(item, planMap, materialMap, versionMap, materialTypeMap, evaluatorMap))
                .filter(item -> filterStudentEvaluationItem(item, keyword))
                .collect(Collectors.toList());

        StudentEvaluationPageVO result = new StudentEvaluationPageVO();
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal((long) allItems.size());
        result.setRecords(slice(allItems, safePage, safeSize));
        return result;
    }

    public EvaluationScoreSummaryVO queryScoreSummary(Long assignmentId,
                                                      Long userId,
                                                      String roleCode) {
        BizAssignment assignment = requireAssignment(assignmentId);
        BizInternshipPlan plan = requirePlan(assignment.getPlanId());
        SysUser user = requireUser(userId);

        ensureScoreSummaryAccess(assignment, plan, user, roleCode);

        List<BizEvaluation> evaluations = evaluationMapper.selectList(new LambdaQueryWrapper<BizEvaluation>()
                .eq(BizEvaluation::getAssignmentId, assignmentId)
                .eq(BizEvaluation::getDeleted, 0L)
                .orderByDesc(BizEvaluation::getEvaluatedTime)
                .orderByDesc(BizEvaluation::getId));

        List<BizEvaluation> processInner = filterByTypeAndRole(evaluations, TYPE_PROCESS, ROLE_INNER_TEACHER);
        List<BizEvaluation> processBase = filterByTypeAndRole(evaluations, TYPE_PROCESS, ROLE_BASE_TEACHER);
        List<BizEvaluation> finalInner = filterByTypeAndRole(evaluations, TYPE_FINAL, ROLE_INNER_TEACHER);
        List<BizEvaluation> finalBase = filterByTypeAndRole(evaluations, TYPE_FINAL, ROLE_BASE_TEACHER);

        BigDecimal innerProcessAvg = avgScore(processInner);
        BigDecimal baseProcessAvg = avgScore(processBase);
        BigDecimal innerFinalLatest = latestScore(finalInner);
        BigDecimal baseFinalLatest = latestScore(finalBase);

        EvaluationScoreSummaryVO summary = new EvaluationScoreSummaryVO();
        summary.setAssignmentId(assignment.getId());
        summary.setPlanId(assignment.getPlanId());
        summary.setStudentId(assignment.getStudentId());
        summary.setInnerTeacherProcessAvg(innerProcessAvg);
        summary.setBaseTeacherProcessAvg(baseProcessAvg);
        summary.setInnerTeacherFinalLatest(innerFinalLatest);
        summary.setBaseTeacherFinalLatest(baseFinalLatest);
        summary.setProcessCompositeAvg(avgNullable(innerProcessAvg, baseProcessAvg));
        summary.setFinalCompositeAvg(avgNullable(innerFinalLatest, baseFinalLatest));
        return summary;
    }

    private List<BizEvaluation> filterByTypeAndRole(List<BizEvaluation> source, String type, String role) {
        return source.stream()
                .filter(item -> type.equals(normalizeCode(item.getEvaluationType())))
                .filter(item -> role.equals(normalizeCode(item.getEvaluatorRole())))
                .collect(Collectors.toList());
    }

    private BigDecimal avgScore(List<BizEvaluation> source) {
        if (CollectionUtils.isEmpty(source)) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BizEvaluation item : source) {
            if (item.getScore() == null) {
                continue;
            }
            sum = sum.add(item.getScore());
            count++;
        }
        if (count <= 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal latestScore(List<BizEvaluation> source) {
        if (CollectionUtils.isEmpty(source)) {
            return null;
        }
        return source.stream()
                .filter(item -> item.getScore() != null)
                .sorted(Comparator.comparing(BizEvaluation::getEvaluatedTime, Comparator.nullsLast(LocalDateTime::compareTo)).reversed()
                        .thenComparing(BizEvaluation::getId, Comparator.nullsLast(Long::compareTo)).reversed())
                .map(BizEvaluation::getScore)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal avgNullable(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return null;
        }
        if (left == null) {
            return normalizeScore(right);
        }
        if (right == null) {
            return normalizeScore(left);
        }
        return left.add(right).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
    }

    private StudentEvaluationItemVO toStudentEvaluationItemVO(BizEvaluation entity,
                                                              Map<Long, BizInternshipPlan> planMap,
                                                              Map<Long, BizMaterial> materialMap,
                                                              Map<Long, BizMaterialVersion> versionMap,
                                                              Map<Long, BizMaterialType> materialTypeMap,
                                                              Map<Long, SysUser> evaluatorMap) {
        StudentEvaluationItemVO vo = new StudentEvaluationItemVO();
        vo.setId(entity.getId());
        vo.setEvaluationType(entity.getEvaluationType());
        vo.setRecordNo(entity.getRecordNo());
        vo.setAssignmentId(entity.getAssignmentId());
        vo.setPlanId(entity.getPlanId());

        BizInternshipPlan plan = planMap.get(entity.getPlanId());
        if (plan != null) {
            vo.setPlanCode(plan.getPlanCode());
            vo.setPlanName(plan.getPlanName());
        }

        vo.setMaterialId(entity.getMaterialId());
        vo.setMaterialVersionId(entity.getMaterialVersionId());

        BizMaterialVersion version = versionMap.get(entity.getMaterialVersionId());
        if (version != null) {
            vo.setMaterialVersionNo(version.getVersionNo());
        }

        BizMaterial material = materialMap.get(entity.getMaterialId());
        if (material != null) {
            BizMaterialType materialType = materialTypeMap.get(material.getMaterialTypeId());
            if (materialType != null) {
                vo.setMaterialTypeName(materialType.getTypeName());
            }
        }

        vo.setEvaluatorId(entity.getEvaluatorId());
        vo.setEvaluatorRole(entity.getEvaluatorRole());
        SysUser evaluator = evaluatorMap.get(entity.getEvaluatorId());
        if (evaluator != null) {
            vo.setEvaluatorName(evaluator.getRealName());
        }

        vo.setScore(entity.getScore());
        vo.setCommentText(entity.getCommentText());
        vo.setEvaluatedTime(entity.getEvaluatedTime());
        return vo;
    }

    private boolean filterStudentEvaluationItem(StudentEvaluationItemVO item, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String value = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(item.getPlanCode(), value)
                || contains(item.getPlanName(), value)
                || contains(item.getMaterialTypeName(), value)
                || contains(item.getEvaluatorName(), value)
                || contains(item.getEvaluatorRole(), value)
                || contains(item.getEvaluationType(), value)
                || contains(item.getCommentText(), value);
    }

    private boolean filterTeacherProcessItem(TeacherProcessPendingItemVO item,
                                             String keyword,
                                             Long planId,
                                             String studentNo,
                                             Integer pendingOnly) {
        if (planId != null && !Objects.equals(item.getPlanId(), planId)) {
            return false;
        }
        if (StringUtils.hasText(studentNo) && !contains(item.getStudentNo(), studentNo.trim().toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (pendingOnly != null) {
            if (pendingOnly == 1 && !Boolean.TRUE.equals(item.getPending())) {
                return false;
            }
            if (pendingOnly == 0 && Boolean.TRUE.equals(item.getPending())) {
                return false;
            }
        }
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String value = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(item.getPlanCode(), value)
                || contains(item.getPlanName(), value)
                || contains(item.getStudentNo(), value)
                || contains(item.getStudentName(), value)
                || contains(item.getMaterialTypeName(), value)
                || contains(item.getMaterialTypeCode(), value)
                || contains(item.getFileName(), value);
    }

    private boolean filterTeacherFinalItem(TeacherFinalPendingItemVO item,
                                           String keyword,
                                           Long planId,
                                           String studentNo,
                                           Integer pendingOnly) {
        if (planId != null && !Objects.equals(item.getPlanId(), planId)) {
            return false;
        }
        if (StringUtils.hasText(studentNo) && !contains(item.getStudentNo(), studentNo.trim().toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (pendingOnly != null) {
            if (pendingOnly == 1 && !Boolean.TRUE.equals(item.getPending())) {
                return false;
            }
            if (pendingOnly == 0 && Boolean.TRUE.equals(item.getPending())) {
                return false;
            }
        }
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String value = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(item.getPlanCode(), value)
                || contains(item.getPlanName(), value)
                || contains(item.getStudentNo(), value)
                || contains(item.getStudentName(), value)
                || contains(item.getPlanStatus(), value);
    }

    private boolean contains(String text, String keywordLowerCase) {
        return StringUtils.hasText(text) && text.toLowerCase(Locale.ROOT).contains(keywordLowerCase);
    }

    private TeacherProcessPendingPageVO buildTeacherProcessPage(List<TeacherProcessPendingItemVO> allItems,
                                                                long page,
                                                                long size) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);

        TeacherProcessPendingPageVO result = new TeacherProcessPendingPageVO();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal((long) allItems.size());
        result.setRecords(slice(allItems, safePage, safeSize));
        return result;
    }

    private TeacherFinalPendingPageVO buildTeacherFinalPage(List<TeacherFinalPendingItemVO> allItems,
                                                            long page,
                                                            long size) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);

        TeacherFinalPendingPageVO result = new TeacherFinalPendingPageVO();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal((long) allItems.size());
        result.setRecords(slice(allItems, safePage, safeSize));
        return result;
    }

    private TeacherProcessPendingPageVO emptyTeacherProcessPage(long page, long size) {
        TeacherProcessPendingPageVO result = new TeacherProcessPendingPageVO();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(0L);
        return result;
    }

    private TeacherFinalPendingPageVO emptyTeacherFinalPage(long page, long size) {
        TeacherFinalPendingPageVO result = new TeacherFinalPendingPageVO();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(0L);
        return result;
    }

    private <T> List<T> slice(List<T> source, long page, long size) {
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

    private List<EvaluationRecordVO> queryRecordVOByMaterialVersion(Long materialVersionId,
                                                                     Long evaluatorId,
                                                                     String roleCode,
                                                                     String evaluationType) {
        List<BizEvaluation> entities = evaluationMapper.selectList(new LambdaQueryWrapper<BizEvaluation>()
                .eq(BizEvaluation::getMaterialVersionId, materialVersionId)
                .eq(BizEvaluation::getEvaluatorId, evaluatorId)
                .eq(BizEvaluation::getEvaluatorRole, normalizeCode(roleCode))
                .eq(BizEvaluation::getEvaluationType, evaluationType)
                .eq(BizEvaluation::getDeleted, 0L)
                .orderByDesc(BizEvaluation::getEvaluatedTime)
                .orderByDesc(BizEvaluation::getId));
        return toEvaluationRecordVOList(entities);
    }

    private List<EvaluationRecordVO> queryRecordVOByAssignment(Long assignmentId,
                                                              Long evaluatorId,
                                                              String roleCode,
                                                              String evaluationType) {
        List<BizEvaluation> entities = evaluationMapper.selectList(new LambdaQueryWrapper<BizEvaluation>()
                .eq(BizEvaluation::getAssignmentId, assignmentId)
                .eq(BizEvaluation::getEvaluatorId, evaluatorId)
                .eq(BizEvaluation::getEvaluatorRole, normalizeCode(roleCode))
                .eq(BizEvaluation::getEvaluationType, evaluationType)
                .eq(BizEvaluation::getDeleted, 0L)
                .orderByDesc(BizEvaluation::getEvaluatedTime)
                .orderByDesc(BizEvaluation::getId));
        return toEvaluationRecordVOList(entities);
    }

    private List<EvaluationRecordVO> toEvaluationRecordVOList(List<BizEvaluation> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        Set<Long> planIds = entities.stream().map(BizEvaluation::getPlanId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> studentIds = entities.stream().map(BizEvaluation::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> materialIds = entities.stream().map(BizEvaluation::getMaterialId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> versionIds = entities.stream().map(BizEvaluation::getMaterialVersionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> evaluatorIds = entities.stream().map(BizEvaluation::getEvaluatorId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);
        Map<Long, SysUser> studentMap = queryUserMap(studentIds);
        Map<Long, BizMaterial> materialMap = queryMaterialMap(materialIds);
        Map<Long, BizMaterialVersion> versionMap = queryMaterialVersionMap(versionIds);
        Set<Long> materialTypeIds = materialMap.values().stream().map(BizMaterial::getMaterialTypeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, BizMaterialType> materialTypeMap = queryMaterialTypeMap(materialTypeIds);
        Map<Long, SysUser> evaluatorMap = queryUserMap(evaluatorIds);

        return entities.stream()
                .map(entity -> toEvaluationRecordVO(
                        entity,
                        planMap.get(entity.getPlanId()),
                        studentMap.get(entity.getStudentId()),
                        materialMap.get(entity.getMaterialId()),
                        versionMap.get(entity.getMaterialVersionId()),
                        resolveMaterialType(materialMap.get(entity.getMaterialId()), materialTypeMap),
                        evaluatorMap.get(entity.getEvaluatorId())
                ))
                .collect(Collectors.toList());
    }

    private BizMaterialType resolveMaterialType(BizMaterial material, Map<Long, BizMaterialType> materialTypeMap) {
        if (material == null || material.getMaterialTypeId() == null) {
            return null;
        }
        return materialTypeMap.get(material.getMaterialTypeId());
    }

    private EvaluationRecordVO toEvaluationRecordVO(BizEvaluation entity,
                                                    BizInternshipPlan plan,
                                                    SysUser student,
                                                    BizMaterial material,
                                                    BizMaterialVersion version,
                                                    BizMaterialType materialType,
                                                    SysUser evaluator) {
        EvaluationRecordVO vo = new EvaluationRecordVO();
        vo.setId(entity.getId());
        vo.setPlanId(entity.getPlanId());
        vo.setAssignmentId(entity.getAssignmentId());
        vo.setStudentId(entity.getStudentId());
        vo.setMaterialId(entity.getMaterialId());
        vo.setMaterialVersionId(entity.getMaterialVersionId());
        vo.setEvaluatorId(entity.getEvaluatorId());
        vo.setEvaluatorRole(entity.getEvaluatorRole());
        vo.setEvaluationType(entity.getEvaluationType());
        vo.setRecordNo(entity.getRecordNo());
        vo.setScore(entity.getScore());
        vo.setCommentText(entity.getCommentText());
        vo.setEvaluatedTime(entity.getEvaluatedTime());

        if (plan != null) {
            vo.setPlanCode(plan.getPlanCode());
            vo.setPlanName(plan.getPlanName());
        }
        if (student != null) {
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getRealName());
        }
        if (version != null) {
            vo.setMaterialVersionNo(version.getVersionNo());
            vo.setFileName(version.getFileName());
        }
        if (materialType != null) {
            vo.setMaterialTypeCode(materialType.getTypeCode());
            vo.setMaterialTypeName(materialType.getTypeName());
        }
        if (evaluator != null) {
            vo.setEvaluatorName(evaluator.getRealName());
        }
        vo.setScoreItems(parseScoreItems(entity.getScoreItemsJson()));
        return vo;
    }

    private List<EvaluationScoreItemVO> parseScoreItems(String scoreItemsJson) {
        if (!StringUtils.hasText(scoreItemsJson)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(scoreItemsJson, new TypeReference<List<EvaluationScoreItemVO>>() {
            });
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private ScoreComputationResult computeScoreItemsAndTotal(List<EvaluationScoreItemRequest> scoreItems) {
        if (CollectionUtils.isEmpty(scoreItems)) {
            return new ScoreComputationResult(null, null, false);
        }

        List<EvaluationScoreItemVO> normalizedItems = new ArrayList<>();
        BigDecimal weightedTotal = BigDecimal.ZERO;
        BigDecimal weightTotal = BigDecimal.ZERO;

        for (EvaluationScoreItemRequest item : scoreItems) {
            if (item == null) {
                continue;
            }
            if (!StringUtils.hasText(item.getItemName())) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "分项评分名称不能为空");
            }
            if (item.getItemScore() == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "分项评分分数不能为空");
            }
            if (item.getItemWeight() == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "分项评分权重不能为空");
            }

            validateMainScore(item.getItemScore());
            validateMainScore(item.getItemWeight());

            BigDecimal normalizedScore = normalizeScore(item.getItemScore());
            BigDecimal normalizedWeight = normalizeScore(item.getItemWeight());

            EvaluationScoreItemVO normalized = new EvaluationScoreItemVO();
            normalized.setItemName(item.getItemName().trim());
            normalized.setItemScore(normalizedScore);
            normalized.setItemWeight(normalizedWeight);
            normalizedItems.add(normalized);

            weightedTotal = weightedTotal.add(normalizedScore.multiply(normalizedWeight));
            weightTotal = weightTotal.add(normalizedWeight);
        }

        if (CollectionUtils.isEmpty(normalizedItems)) {
            return new ScoreComputationResult(null, null, false);
        }
        if (weightTotal.compareTo(SCORE_BASE) != 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "分项权重总和必须等于 100%");
        }

        BigDecimal computedScore = weightedTotal.divide(SCORE_BASE, 2, RoundingMode.HALF_UP);
        try {
            return new ScoreComputationResult(objectMapper.writeValueAsString(normalizedItems), computedScore, true);
        } catch (Exception ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "分项评分序列化失败");
        }
    }

    private Map<Long, EvaluationStat> buildEvaluationStatMap(Set<Long> materialVersionIds,
                                                             Long evaluatorId,
                                                             String roleCode,
                                                             String evaluationType) {
        if (CollectionUtils.isEmpty(materialVersionIds)) {
            return new HashMap<>();
        }
        List<BizEvaluation> records = evaluationMapper.selectList(new LambdaQueryWrapper<BizEvaluation>()
                .in(BizEvaluation::getMaterialVersionId, materialVersionIds)
                .eq(BizEvaluation::getEvaluatorId, evaluatorId)
                .eq(BizEvaluation::getEvaluatorRole, normalizeCode(roleCode))
                .eq(BizEvaluation::getEvaluationType, evaluationType)
                .eq(BizEvaluation::getDeleted, 0L)
                .orderByDesc(BizEvaluation::getEvaluatedTime)
                .orderByDesc(BizEvaluation::getId));

        Map<Long, EvaluationStat> statMap = new HashMap<>();
        for (BizEvaluation record : records) {
            EvaluationStat stat = statMap.computeIfAbsent(record.getMaterialVersionId(), key -> new EvaluationStat());
            stat.count++;
            if (stat.latestTime == null) {
                stat.latestTime = record.getEvaluatedTime();
                stat.latestScore = record.getScore();
            }
        }
        return statMap;
    }

    private Map<Long, EvaluationStat> buildEvaluationStatMapByAssignment(Set<Long> assignmentIds,
                                                                          Long evaluatorId,
                                                                          String roleCode,
                                                                          String evaluationType) {
        if (CollectionUtils.isEmpty(assignmentIds)) {
            return new HashMap<>();
        }
        List<BizEvaluation> records = evaluationMapper.selectList(new LambdaQueryWrapper<BizEvaluation>()
                .in(BizEvaluation::getAssignmentId, assignmentIds)
                .eq(BizEvaluation::getEvaluatorId, evaluatorId)
                .eq(BizEvaluation::getEvaluatorRole, normalizeCode(roleCode))
                .eq(BizEvaluation::getEvaluationType, evaluationType)
                .eq(BizEvaluation::getDeleted, 0L)
                .orderByDesc(BizEvaluation::getEvaluatedTime)
                .orderByDesc(BizEvaluation::getId));

        Map<Long, EvaluationStat> statMap = new HashMap<>();
        for (BizEvaluation record : records) {
            EvaluationStat stat = statMap.computeIfAbsent(record.getAssignmentId(), key -> new EvaluationStat());
            stat.count++;
            if (stat.latestTime == null) {
                stat.latestTime = record.getEvaluatedTime();
                stat.latestScore = record.getScore();
            }
        }
        return statMap;
    }

    private int nextRecordNo(Long assignmentId,
                             Long evaluatorId,
                             String evaluationType,
                             Long materialVersionId) {
        LambdaQueryWrapper<BizEvaluation> wrapper = new LambdaQueryWrapper<BizEvaluation>()
                .eq(BizEvaluation::getAssignmentId, assignmentId)
                .eq(BizEvaluation::getEvaluatorId, evaluatorId)
                .eq(BizEvaluation::getEvaluationType, evaluationType)
                .eq(BizEvaluation::getDeleted, 0L)
                .orderByDesc(BizEvaluation::getRecordNo)
                .last("LIMIT 1");

        if (materialVersionId == null) {
            wrapper.isNull(BizEvaluation::getMaterialVersionId);
        } else {
            wrapper.eq(BizEvaluation::getMaterialVersionId, materialVersionId);
        }

        BizEvaluation last = evaluationMapper.selectOne(wrapper);
        if (last == null || last.getRecordNo() == null || last.getRecordNo() <= 0) {
            return 1;
        }
        return last.getRecordNo() + 1;
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

    private void ensureScoreSummaryAccess(BizAssignment assignment,
                                          BizInternshipPlan plan,
                                          SysUser user,
                                          String roleCode) {
        String normalizedRole = normalizeCode(roleCode);
        if (ROLE_SYS_ADMIN.equals(normalizedRole) || ROLE_ACADEMIC_ADMIN.equals(normalizedRole)) {
            return;
        }
        if (ROLE_DEPT_ADMIN.equals(normalizedRole)) {
            if (Objects.equals(plan.getDeptId(), user.getDeptId())) {
                return;
            }
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "鏃犳潈鏌ョ湅鎴愮哗姹囨€?");
        }
        if (ROLE_STUDENT.equals(normalizedRole)) {
            if (Objects.equals(assignment.getStudentId(), user.getId())) {
                return;
            }
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看成绩汇总");
        }
        if (ROLE_INNER_TEACHER.equals(normalizedRole) && Objects.equals(assignment.getInnerTeacherId(), user.getId())) {
            return;
        }
        if (ROLE_BASE_TEACHER.equals(normalizedRole) && Objects.equals(assignment.getBaseTeacherId(), user.getId())) {
            return;
        }
        throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看成绩汇总");
    }

    private SysUser requireTeacherOperator(Long userId, String roleCode) {
        String normalizedRole = normalizeCode(roleCode);
        if (!ROLE_INNER_TEACHER.equals(normalizedRole) && !ROLE_BASE_TEACHER.equals(normalizedRole)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色必须是教师");
        }
        return requireUser(userId);
    }

    private SysUser requireStudentOperator(Long userId, String roleCode) {
        String normalizedRole = normalizeCode(roleCode);
        if (!ROLE_STUDENT.equals(normalizedRole)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色必须是师范生");
        }
        return requireUser(userId);
    }

    private void ensureTeacherCanEvaluateAssignment(BizAssignment assignment,
                                                    Long teacherId,
                                                    String roleCode) {
        String normalizedRole = normalizeCode(roleCode);
        if (ROLE_INNER_TEACHER.equals(normalizedRole) && Objects.equals(assignment.getInnerTeacherId(), teacherId)) {
            return;
        }
        if (ROLE_BASE_TEACHER.equals(normalizedRole) && Objects.equals(assignment.getBaseTeacherId(), teacherId)) {
            return;
        }
        throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权评价该学生");
    }

    private boolean canSubmitFinalEvaluation(BizInternshipPlan plan) {
        String status = normalizeCode(plan.getPlanStatus());
        return PLAN_STATUS_FINISHED.equals(status) || PLAN_STATUS_ARCHIVED.equals(status);
    }

    private List<BizAssignment> queryTeacherCurrentAssignments(Long userId, String roleCode) {
        String normalizedRole = normalizeCode(roleCode);
        LambdaQueryWrapper<BizAssignment> wrapper = new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L);

        if (ROLE_INNER_TEACHER.equals(normalizedRole)) {
            wrapper.eq(BizAssignment::getInnerTeacherId, userId);
        } else if (ROLE_BASE_TEACHER.equals(normalizedRole)) {
            wrapper.eq(BizAssignment::getBaseTeacherId, userId);
        } else {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色必须是教师");
        }

        return assignmentMapper.selectList(wrapper);
    }

    private boolean isFinalEvaluationReady(Long planId) {
        List<BizAssignment> assignments = assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getPlanId, planId)
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L));
        if (CollectionUtils.isEmpty(assignments)) {
            return false;
        }

        Set<Long> assignmentIds = assignments.stream()
                .map(BizAssignment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<BizEvaluation> finalEvaluations = evaluationMapper.selectList(new LambdaQueryWrapper<BizEvaluation>()
                .in(BizEvaluation::getAssignmentId, assignmentIds)
                .eq(BizEvaluation::getEvaluationType, TYPE_FINAL)
                .eq(BizEvaluation::getDeleted, 0L));

        Map<Long, Set<String>> evaluatedRolesByAssignment = new HashMap<>();
        for (BizEvaluation evaluation : finalEvaluations) {
            if (evaluation.getAssignmentId() == null || !StringUtils.hasText(evaluation.getEvaluatorRole())) {
                continue;
            }
            evaluatedRolesByAssignment
                    .computeIfAbsent(evaluation.getAssignmentId(), key -> new HashSet<>())
                    .add(normalizeCode(evaluation.getEvaluatorRole()));
        }

        for (BizAssignment assignment : assignments) {
            Set<String> roles = evaluatedRolesByAssignment.get(assignment.getId());
            if (roles == null || !roles.contains(ROLE_INNER_TEACHER) || !roles.contains(ROLE_BASE_TEACHER)) {
                return false;
            }
        }
        return true;
    }

    private Map<Long, BizInternshipPlan> queryPlanMap(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                        .in(BizInternshipPlan::getId, ids)
                        .eq(BizInternshipPlan::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BizInternshipPlan::getId, item -> item, (left, right) -> left));
    }

    private Map<Long, SysUser> queryUserMap(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getId, ids)
                        .eq(SysUser::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, item -> item, (left, right) -> left));
    }

    private Map<Long, BizMaterialType> queryMaterialTypeMap(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return materialTypeMapper.selectList(new LambdaQueryWrapper<BizMaterialType>()
                        .in(BizMaterialType::getId, ids)
                        .eq(BizMaterialType::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BizMaterialType::getId, item -> item, (left, right) -> left));
    }

    private Map<Long, BizMaterial> queryMaterialMap(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return materialMapper.selectList(new LambdaQueryWrapper<BizMaterial>()
                        .in(BizMaterial::getId, ids)
                        .eq(BizMaterial::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BizMaterial::getId, item -> item, (left, right) -> left));
    }

    private Map<Long, BizMaterialVersion> queryMaterialVersionMap(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return materialVersionMapper.selectList(new LambdaQueryWrapper<BizMaterialVersion>()
                        .in(BizMaterialVersion::getId, ids)
                        .eq(BizMaterialVersion::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BizMaterialVersion::getId, item -> item, (left, right) -> left));
    }

    private void validateMainScore(BigDecimal score) {
        if (score == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "评分不能为空");
        }
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "评分必须在 0 到 100 之间");
        }
    }

    private BigDecimal normalizeScore(BigDecimal score) {
        if (score == null) {
            return null;
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private static final class ScoreComputationResult {
        private final String scoreItemsJson;
        private final BigDecimal computedScore;
        private final boolean hasScoreItems;

        private ScoreComputationResult(String scoreItemsJson, BigDecimal computedScore, boolean hasScoreItems) {
            this.scoreItemsJson = scoreItemsJson;
            this.computedScore = computedScore;
            this.hasScoreItems = hasScoreItems;
        }

        private String getScoreItemsJson() {
            return scoreItemsJson;
        }

        private BigDecimal getComputedScore() {
            return computedScore;
        }

        private boolean hasScoreItems() {
            return hasScoreItems;
        }
    }

    private long normalizePage(long page) {
        return page <= 0 ? 1 : page;
    }

    private long normalizeSize(long size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 200);
    }

    private String normalizeCode(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private BizAssignment requireAssignment(Long assignmentId) {
        if (assignmentId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "分配标识不能为空");
        }
        BizAssignment assignment = assignmentMapper.selectOne(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getId, assignmentId)
                .eq(BizAssignment::getDeleted, 0L)
                .last("LIMIT 1"));
        if (assignment == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "分配记录不存在");
        }
        return assignment;
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

    private BizMaterialVersion requireMaterialVersion(Long materialVersionId) {
        BizMaterialVersion version = materialVersionMapper.selectOne(new LambdaQueryWrapper<BizMaterialVersion>()
                .eq(BizMaterialVersion::getId, materialVersionId)
                .eq(BizMaterialVersion::getDeleted, 0L)
                .last("LIMIT 1"));
        if (version == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "材料版本不存在");
        }
        return version;
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

    private static class EvaluationStat {
        private int count;
        private LocalDateTime latestTime;
        private BigDecimal latestScore;
    }
}
