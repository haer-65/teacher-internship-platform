package com.teacher.internship.modules.notice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.internship.config.NoticeProperties;
import com.teacher.internship.modules.application.entity.BizAssignment;
import com.teacher.internship.modules.application.entity.BizStudentApplication;
import com.teacher.internship.modules.application.mapper.BizAssignmentMapper;
import com.teacher.internship.modules.evaluation.entity.BizEvaluation;
import com.teacher.internship.modules.material.entity.BizMaterial;
import com.teacher.internship.modules.material.mapper.BizMaterialMapper;
import com.teacher.internship.modules.base.entity.BaseDepartment;
import com.teacher.internship.modules.base.entity.BaseGrade;
import com.teacher.internship.modules.base.entity.BaseMajor;
import com.teacher.internship.modules.plan.entity.BizInternshipPlan;
import com.teacher.internship.modules.plan.entity.BizMaterialType;
import com.teacher.internship.modules.plan.mapper.BizInternshipPlanMapper;
import com.teacher.internship.modules.plan.mapper.BizMaterialTypeMapper;
import com.teacher.internship.modules.score.entity.BizScoreSheet;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import com.teacher.internship.modules.system.service.SystemParamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoticeTriggerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoticeTriggerService.class);

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_DEPT_ADMIN = "DEPT_ADMIN";
    private static final String ROLE_SYS_ADMIN = "SYS_ADMIN";
    private static final String STATUS_ENABLED = "ENABLED";
    private static final String NOTICE_TYPE_BUSINESS = "BUSINESS";
    private static final String NOTICE_LEVEL_NORMAL = "NORMAL";
    private static final String NOTICE_LEVEL_URGENT = "URGENT";
    private static final String MATERIAL_STATUS_SUBMITTED = "SUBMITTED";
    private static final String REMINDER_PARAM_CODE = "NOTICE_NEAR_DEADLINE_DAYS";
    private static final String BIZ_TYPE_PLAN_PUBLISHED = "PLAN_PUBLISHED";
    private static final String BIZ_TYPE_APPLICATION_SUBMITTED = "APPLICATION_SUBMITTED";
    private static final String BIZ_TYPE_APPLICATION_REVIEWED = "APPLICATION_REVIEWED";
    private static final String BIZ_TYPE_ASSIGNMENT_COMPLETED = "ASSIGNMENT_COMPLETED";
    private static final String BIZ_TYPE_MATERIAL_SUBMITTED = "MATERIAL_SUBMITTED";
    private static final String BIZ_TYPE_MATERIAL_EVALUATED = "MATERIAL_EVALUATED";
    private static final String BIZ_TYPE_MATERIAL_DEADLINE = "MATERIAL_DEADLINE";
    private static final String BIZ_TYPE_PLAN_FINISHED = "PLAN_FINISHED";
    private static final String BIZ_TYPE_SCORE_PUBLISHED = "SCORE_PUBLISHED";
    private static final String BIZ_TYPE_SCORE_ADJUSTED = "SCORE_ADJUSTED";
    private static final String BIZ_TYPE_USER_REGISTER_SUBMITTED = "USER_REGISTER_SUBMITTED";
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NoticeService noticeService;
    private final BizAssignmentMapper assignmentMapper;
    private final BizMaterialMapper materialMapper;
    private final BizMaterialTypeMapper materialTypeMapper;
    private final BizInternshipPlanMapper planMapper;
    private final SystemParamService paramService;
    private final SysUserMapper userMapper;
    private final NoticeProperties noticeProperties;

    public NoticeTriggerService(NoticeService noticeService,
                                BizAssignmentMapper assignmentMapper,
                                BizMaterialMapper materialMapper,
                                BizMaterialTypeMapper materialTypeMapper,
                                BizInternshipPlanMapper planMapper,
                                SystemParamService paramService,
                                SysUserMapper userMapper,
                                NoticeProperties noticeProperties) {
        this.noticeService = noticeService;
        this.assignmentMapper = assignmentMapper;
        this.materialMapper = materialMapper;
        this.materialTypeMapper = materialTypeMapper;
        this.planMapper = planMapper;
        this.paramService = paramService;
        this.userMapper = userMapper;
        this.noticeProperties = noticeProperties;
    }

    public void notifyStudentRegisterSubmitted(SysUser user,
                                               BaseDepartment department,
                                               BaseMajor major,
                                               BaseGrade grade) {
        if (user == null || user.getId() == null) {
            return;
        }
        try {
            Set<Long> receiverIds = new HashSet<>();
            receiverIds.addAll(noticeService.queryEnabledUserIdsByRole(ROLE_SYS_ADMIN, null));
            if (CollectionUtils.isEmpty(receiverIds)) {
                return;
            }

            String title = "新师范生注册申请待审核";
            String content = "姓名：" + safeText(user.getRealName(), "-")
                    + "；学号：" + safeText(user.getStudentNo(), "-")
                    + "；院系：" + safeText(department == null ? null : department.getDeptName(), "-")
                    + "；专业：" + safeText(major == null ? null : major.getMajorName(), "-")
                    + "；年级：" + safeText(grade == null ? null : grade.getGradeName(), "-")
                    + "。请尽快前往用户管理完成审核。";

            noticeService.publishNotice(
                    NOTICE_TYPE_BUSINESS,
                    NOTICE_LEVEL_NORMAL,
                    title,
                    content,
                    null,
                    ROLE_SYS_ADMIN,
                    BIZ_TYPE_USER_REGISTER_SUBMITTED,
                    user.getId(),
                    receiverIds,
                    false
            );
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger student register submitted notice, userId={}", user.getId(), ex);
        }
    }

    public void notifyPlanPublished(BizInternshipPlan plan, Long operatorId) {
        if (plan == null || plan.getId() == null) {
            return;
        }
        try {
            Set<Long> receiverIds = noticeService.queryEnabledUserIdsByRole(ROLE_STUDENT, plan.getDeptId());
            if (CollectionUtils.isEmpty(receiverIds)) {
                return;
            }

            String title = "实习计划已发布：" + safeText(plan.getPlanName(), plan.getPlanCode());
            String content = "计划编码：" + safeText(plan.getPlanCode(), "-")
                    + "；计划名称：" + safeText(plan.getPlanName(), "-")
                    + "；申请截止时间：" + formatDateTime(plan.getApplyDeadline())
                    + "。请在截止前及时提交申请。";

            noticeService.publishNotice(
                    NOTICE_TYPE_BUSINESS,
                    NOTICE_LEVEL_NORMAL,
                    title,
                    content,
                    operatorId,
                    ROLE_STUDENT,
                    BIZ_TYPE_PLAN_PUBLISHED,
                    plan.getId(),
                    receiverIds,
                    false
            );
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger plan published notice, planId={}", plan.getId(), ex);
        }
    }

    public void notifyApplicationSubmitted(BizStudentApplication application,
                                           BizInternshipPlan plan,
                                           SysUser student) {
        if (application == null || application.getId() == null) {
            return;
        }
        try {
            Set<Long> receiverIds = new HashSet<>();
            receiverIds.addAll(noticeService.queryEnabledUserIdsByRole(ROLE_DEPT_ADMIN, plan == null ? null : plan.getDeptId()));
            if (CollectionUtils.isEmpty(receiverIds)) {
                return;
            }

            String title = "实习申请待审核";
            String content = "学生 " + safeText(student == null ? null : student.getRealName(), "-")
                    + "（" + safeText(student == null ? null : student.getStudentNo(), "-") + "）"
                    + " 提交了计划 "
                    + safeText(plan == null ? null : plan.getPlanName(), "-")
                    + "（" + safeText(plan == null ? null : plan.getPlanCode(), "-") + "）"
                    + " 的申请，请及时审核。";

            noticeService.publishNotice(
                    NOTICE_TYPE_BUSINESS,
                    NOTICE_LEVEL_NORMAL,
                    title,
                    content,
                    student == null ? null : student.getId(),
                    ROLE_DEPT_ADMIN,
                    BIZ_TYPE_APPLICATION_SUBMITTED,
                    application.getId(),
                    receiverIds,
                    false
            );
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger application submitted notice, applicationId={}", application.getId(), ex);
        }
    }

    public void notifyApplicationReviewed(BizStudentApplication application,
                                          BizInternshipPlan plan,
                                          SysUser reviewer) {
        if (application == null || application.getId() == null || application.getStudentId() == null) {
            return;
        }
        try {
            Set<Long> receiverIds = new HashSet<>();
            receiverIds.add(application.getStudentId());

            String status = normalizeCode(application.getApplicationStatus());
            String statusText = "APPROVED".equals(status) ? "已通过"
                    : "REJECTED".equals(status) ? "已驳回"
                    : status;
            String title = "申请审核结果：" + statusText;
            String content = "计划名称：" + safeText(plan == null ? null : plan.getPlanName(), "-")
                    + "；审核结果：" + statusText
                    + "；审核人：" + safeText(reviewer == null ? null : reviewer.getRealName(), "-")
                    + "；审核意见：" + safeText(application.getReviewComment(), "无")
                    + "。";

            noticeService.publishNotice(
                    NOTICE_TYPE_BUSINESS,
                    "REJECTED".equals(status) ? NOTICE_LEVEL_URGENT : NOTICE_LEVEL_NORMAL,
                    title,
                    content,
                    reviewer == null ? null : reviewer.getId(),
                    null,
                    BIZ_TYPE_APPLICATION_REVIEWED,
                    application.getId(),
                    receiverIds,
                    false
            );
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger application reviewed notice, applicationId={}", application.getId(), ex);
        }
    }

    public void notifyAssignmentCompleted(BizAssignment assignment,
                                          BizInternshipPlan plan,
                                          Long operatorId,
                                          boolean adjusted) {
        if (assignment == null || assignment.getId() == null) {
            return;
        }
        try {
            Set<Long> receiverIds = buildAssignmentReceivers(assignment);
            if (CollectionUtils.isEmpty(receiverIds)) {
                return;
            }

            SysUser student = queryUser(assignment.getStudentId());
            String title = adjusted ? "实习分配已调整" : "实习分配已完成";
            String content = "计划名称：" + safeText(plan == null ? null : plan.getPlanName(), "-")
                    + "；学生姓名：" + safeText(student == null ? null : student.getRealName(), "-")
                    + "；版本号：" + (assignment.getVersionNo() == null ? "-" : assignment.getVersionNo())
                    + "。请及时到系统中查看分配详情。";

            noticeService.publishNotice(
                    NOTICE_TYPE_BUSINESS,
                    NOTICE_LEVEL_NORMAL,
                    title,
                    content,
                    operatorId,
                    null,
                    BIZ_TYPE_ASSIGNMENT_COMPLETED,
                    assignment.getId(),
                    receiverIds,
                    false
            );
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger assignment notice, assignmentId={}", assignment.getId(), ex);
        }
    }

    public void notifyPlanFinished(BizInternshipPlan plan, Long operatorId) {
        if (plan == null || plan.getId() == null) {
            return;
        }
        try {
            Set<Long> receiverIds = queryFinalEvaluationReceivers(plan.getId());
            if (CollectionUtils.isEmpty(receiverIds)) {
                return;
            }

            String title = "\u8ba1\u5212\u5df2\u7ed3\u675f\uff0c\u8bf7\u5f00\u59cb\u7efc\u5408\u8bc4\u4ef7";
            String content = "\u5b9e\u4e60\u8ba1\u5212\u300c" + safeText(plan.getPlanName(), "-")
                    + "\u300d\u5df2\u7ed3\u675f\uff0c\u8bf7\u5bf9\u5f53\u524d\u5206\u914d\u8fdb\u884c\u7efc\u5408\u8bc4\u4ef7\u3002"
                    + "\u8ba1\u5212\u7f16\u7801\uff1a" + safeText(plan.getPlanCode(), "-")
                    + "\u3001\u5b66\u5e74\uff1a" + safeText(plan.getAcademicYear(), "-")
                    + "\u3001\u5b66\u671f\uff1a" + safeText(plan.getTerm(), "-")
                    + "\u3002";

            noticeService.publishNotice(
                    NOTICE_TYPE_BUSINESS,
                    NOTICE_LEVEL_NORMAL,
                    title,
                    content,
                    operatorId,
                    null,
                    BIZ_TYPE_PLAN_FINISHED,
                    plan.getId(),
                    receiverIds,
                    false
            );
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger plan finished notice, planId={}", plan.getId(), ex);
        }
    }

    public void notifyMaterialEvaluated(BizEvaluation evaluation,
                                        BizMaterialType materialType,
                                        BizInternshipPlan plan,
                                        SysUser evaluator) {
        if (evaluation == null || evaluation.getId() == null || evaluation.getStudentId() == null) {
            return;
        }
        try {
            Set<Long> receiverIds = new HashSet<>();
            receiverIds.add(evaluation.getStudentId());

            String materialTypeName = safeText(materialType == null ? null : materialType.getTypeName(), "过程材料");
            String title = "材料评价已完成：" + materialTypeName;
            String content = "计划名称：" + safeText(plan == null ? null : plan.getPlanName(), "-")
                    + "；材料类型：" + materialTypeName
                    + "；得分：" + (evaluation.getScore() == null ? "-" : evaluation.getScore().toPlainString())
                    + "；评价人：" + safeText(evaluator == null ? null : evaluator.getRealName(), "-")
                    + "。请查看评价详情。";

            noticeService.publishNotice(
                    NOTICE_TYPE_BUSINESS,
                    NOTICE_LEVEL_NORMAL,
                    title,
                    content,
                    evaluator == null ? null : evaluator.getId(),
                    ROLE_STUDENT,
                    BIZ_TYPE_MATERIAL_EVALUATED,
                    evaluation.getId(),
                    receiverIds,
                    false
            );
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger material evaluated notice, evaluationId={}", evaluation.getId(), ex);
        }
    }

    public void notifyMaterialSubmitted(BizMaterial material,
                                        BizMaterialType materialType,
                                        BizInternshipPlan plan,
                                        SysUser student,
                                        BizAssignment assignment,
                                        Long operatorId) {
        if (material == null || material.getId() == null) {
            return;
        }
        try {
            Set<Long> receiverIds = new HashSet<>();
            if (assignment != null) {
                if (assignment.getInnerTeacherId() != null) {
                    receiverIds.add(assignment.getInnerTeacherId());
                }
                if (assignment.getBaseTeacherId() != null) {
                    receiverIds.add(assignment.getBaseTeacherId());
                }
            }
            if (CollectionUtils.isEmpty(receiverIds)) {
                return;
            }

            String materialTypeName = safeText(materialType == null ? null : materialType.getTypeName(), "材料");
            String title = "学生提交了" + materialTypeName;
            String content = "学生 " + safeText(student == null ? null : student.getRealName(), "-")
                    + "（" + safeText(student == null ? null : student.getStudentNo(), "-") + "）"
                    + " 在计划 " + safeText(plan == null ? null : plan.getPlanName(), "-")
                    + " 中提交了 " + materialTypeName
                    + "，当前版本为第 " + (material.getLatestVersionNo() == null ? "-" : material.getLatestVersionNo()) + " 版。";

            noticeService.publishNotice(
                    NOTICE_TYPE_BUSINESS,
                    NOTICE_LEVEL_NORMAL,
                    title,
                    content,
                    operatorId,
                    null,
                    BIZ_TYPE_MATERIAL_SUBMITTED,
                    material.getId(),
                    receiverIds,
                    false
            );
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger material submitted notice, materialId={}", material.getId(), ex);
        }
    }

    private Set<Long> queryFinalEvaluationReceivers(Long planId) {
        if (planId == null) {
            return Set.of();
        }
        List<BizAssignment> assignments = assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getPlanId, planId)
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L));
        if (CollectionUtils.isEmpty(assignments)) {
            return Set.of();
        }
        Set<Long> receiverIds = new HashSet<>();
        for (BizAssignment assignment : assignments) {
            if (assignment.getInnerTeacherId() != null) {
                receiverIds.add(assignment.getInnerTeacherId());
            }
            if (assignment.getBaseTeacherId() != null) {
                receiverIds.add(assignment.getBaseTeacherId());
            }
        }
        return receiverIds;
    }

    public void notifyScorePublished(BizInternshipPlan plan, List<BizScoreSheet> scoreSheets, Long operatorId) {
        if (plan == null || plan.getId() == null || CollectionUtils.isEmpty(scoreSheets)) {
            return;
        }
        try {
            Set<Long> studentIds = scoreSheets.stream()
                    .map(BizScoreSheet::getStudentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(studentIds)) {
                return;
            }

            Set<Long> receiverIds = new HashSet<>(studentIds);
            List<BizAssignment> assignments = assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                    .eq(BizAssignment::getPlanId, plan.getId())
                    .eq(BizAssignment::getIsCurrent, 1)
                    .eq(BizAssignment::getDeleted, 0L)
                    .in(BizAssignment::getStudentId, studentIds));
            for (BizAssignment assignment : assignments) {
                if (assignment.getInnerTeacherId() != null) {
                    receiverIds.add(assignment.getInnerTeacherId());
                }
                if (assignment.getBaseTeacherId() != null) {
                    receiverIds.add(assignment.getBaseTeacherId());
                }
            }

            String title = "实习成绩已发布：" + safeText(plan.getPlanName(), plan.getPlanCode());
            String content = "计划编码：" + safeText(plan.getPlanCode(), "-")
                    + "；计划名称：" + safeText(plan.getPlanName(), "-")
                    + "；已发布成绩单数量：" + scoreSheets.size()
                    + "。请到成绩模块查看详情。";

            noticeService.publishNotice(
                    NOTICE_TYPE_BUSINESS,
                    NOTICE_LEVEL_NORMAL,
                    title,
                    content,
                    operatorId,
                    null,
                    BIZ_TYPE_SCORE_PUBLISHED,
                    plan.getId(),
                    receiverIds,
                    false
            );
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger score published notice, planId={}", plan.getId(), ex);
        }
    }

    public void notifyScoreAdjusted(BizScoreSheet scoreSheet,
                                    BizInternshipPlan plan,
                                    Long operatorId,
                                    java.math.BigDecimal oldScore,
                                    java.math.BigDecimal newScore,
                                    String reason) {
        if (scoreSheet == null || scoreSheet.getId() == null) {
            return;
        }
        try {
            Set<Long> receiverIds = new HashSet<>();
            if (scoreSheet.getStudentId() != null) {
                receiverIds.add(scoreSheet.getStudentId());
            }

            BizAssignment assignment = null;
            if (scoreSheet.getAssignmentId() != null) {
                assignment = assignmentMapper.selectOne(new LambdaQueryWrapper<BizAssignment>()
                        .eq(BizAssignment::getId, scoreSheet.getAssignmentId())
                        .eq(BizAssignment::getDeleted, 0L)
                        .last("LIMIT 1"));
            }
            if (assignment != null) {
                receiverIds.addAll(buildAssignmentReceivers(assignment));
            }

            if (CollectionUtils.isEmpty(receiverIds)) {
                return;
            }

            SysUser student = queryUser(scoreSheet.getStudentId());
            String title = "成绩已调整：" + safeText(plan == null ? null : plan.getPlanName(), "-");
            String content = "学生：" + safeText(student == null ? null : student.getRealName(), "-")
                    + "（" + safeText(student == null ? null : student.getStudentNo(), "-") + "）"
                    + "；原成绩：" + (oldScore == null ? "-" : oldScore.stripTrailingZeros().toPlainString())
                    + "；调整后：" + (newScore == null ? "-" : newScore.stripTrailingZeros().toPlainString())
                    + "；调整原因：" + safeText(reason, "-");

            noticeService.publishNotice(
                    NOTICE_TYPE_BUSINESS,
                    NOTICE_LEVEL_NORMAL,
                    title,
                    content,
                    operatorId,
                    null,
                    BIZ_TYPE_SCORE_ADJUSTED,
                    scoreSheet.getId(),
                    receiverIds,
                    false
            );
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger score adjusted notice, scoreSheetId={}", scoreSheet.getId(), ex);
        }
    }

    public long triggerMaterialDeadlineReminder() {
        try {
            int remindDays = resolveDeadlineReminderDays();
            if (remindDays <= 0) {
                return 0;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deadlineEnd = now.plusDays(remindDays);
            List<BizMaterialType> dueMaterialTypes = materialTypeMapper.selectList(new LambdaQueryWrapper<BizMaterialType>()
                    .eq(BizMaterialType::getStatus, STATUS_ENABLED)
                    .eq(BizMaterialType::getDeleted, 0L)
                    .isNotNull(BizMaterialType::getDeadlineTime)
                    .ge(BizMaterialType::getDeadlineTime, now)
                    .le(BizMaterialType::getDeadlineTime, deadlineEnd));
            if (CollectionUtils.isEmpty(dueMaterialTypes)) {
                return 0;
            }

            Map<Long, BizMaterialType> materialTypeMap = dueMaterialTypes.stream()
                    .collect(Collectors.toMap(BizMaterialType::getId, item -> item, (left, right) -> left));
            Set<Long> materialTypeIds = materialTypeMap.keySet();

            List<BizMaterial> materials = materialMapper.selectList(new LambdaQueryWrapper<BizMaterial>()
                    .in(BizMaterial::getMaterialTypeId, materialTypeIds)
                    .eq(BizMaterial::getDeleted, 0L));
            if (CollectionUtils.isEmpty(materials)) {
                return 0;
            }

            materials = materials.stream()
                    .filter(item -> !MATERIAL_STATUS_SUBMITTED.equals(normalizeCode(item.getMaterialStatus())))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(materials)) {
                return 0;
            }

            Set<Long> assignmentIds = materials.stream()
                    .map(BizMaterial::getAssignmentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, BizAssignment> assignmentMap = assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                            .in(BizAssignment::getId, assignmentIds)
                            .eq(BizAssignment::getIsCurrent, 1)
                            .eq(BizAssignment::getDeleted, 0L))
                    .stream()
                    .collect(Collectors.toMap(BizAssignment::getId, item -> item, (left, right) -> left));

            Set<Long> planIds = assignmentMap.values().stream()
                    .map(BizAssignment::getPlanId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, BizInternshipPlan> planMap = planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                            .in(BizInternshipPlan::getId, planIds)
                            .eq(BizInternshipPlan::getDeleted, 0L))
                    .stream()
                    .collect(Collectors.toMap(BizInternshipPlan::getId, item -> item, (left, right) -> left));

            long generatedCount = 0;
            for (BizMaterial material : materials) {
                BizAssignment assignment = assignmentMap.get(material.getAssignmentId());
                BizMaterialType materialType = materialTypeMap.get(material.getMaterialTypeId());
                if (assignment == null || materialType == null || materialType.getDeadlineTime() == null) {
                    continue;
                }
                if (noticeService.existsSentNotice(BIZ_TYPE_MATERIAL_DEADLINE, material.getId())) {
                    continue;
                }

                Set<Long> receiverIds = buildAssignmentReceivers(assignment);
                if (CollectionUtils.isEmpty(receiverIds)) {
                    continue;
                }

                BizInternshipPlan plan = planMap.get(assignment.getPlanId());
                String title = "材料截止提醒：" + safeText(materialType.getTypeName(), "-");
                String content = "计划名称：" + safeText(plan == null ? null : plan.getPlanName(), "-")
                        + "；材料类型：" + safeText(materialType.getTypeName(), "-")
                        + "；截止时间：" + formatDateTime(materialType.getDeadlineTime())
                        + "。请在截止前完成提交。";

                Long noticeId = noticeService.publishNotice(
                        NOTICE_TYPE_BUSINESS,
                        NOTICE_LEVEL_URGENT,
                        title,
                        content,
                        null,
                        null,
                        BIZ_TYPE_MATERIAL_DEADLINE,
                        material.getId(),
                        receiverIds,
                        false
                );
                if (noticeId != null) {
                    generatedCount++;
                }
            }
            return generatedCount;
        } catch (Exception ex) {
            LOGGER.error("Failed to trigger material deadline notice", ex);
            return 0;
        }
    }

    private int resolveDeadlineReminderDays() {
        int defaultDays = noticeProperties.getDeadlineReminderDays() > 0
                ? noticeProperties.getDeadlineReminderDays()
                : 3;
        int parsed = paramService.getIntValue(REMINDER_PARAM_CODE, defaultDays);
        return parsed > 0 ? parsed : defaultDays;
    }

    private Set<Long> buildAssignmentReceivers(BizAssignment assignment) {
        Set<Long> receiverIds = new HashSet<>();
        if (assignment.getStudentId() != null) {
            receiverIds.add(assignment.getStudentId());
        }
        if (assignment.getInnerTeacherId() != null) {
            receiverIds.add(assignment.getInnerTeacherId());
        }
        if (assignment.getBaseTeacherId() != null) {
            receiverIds.add(assignment.getBaseTeacherId());
        }
        return receiverIds;
    }

    private SysUser queryUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : DT_FORMATTER.format(dateTime);
    }

    private String safeText(String text, String fallback) {
        return StringUtils.hasText(text) ? text.trim() : fallback;
    }

    private String normalizeCode(String text) {
        return text == null ? "" : text.trim().toUpperCase(Locale.ROOT);
    }
}
