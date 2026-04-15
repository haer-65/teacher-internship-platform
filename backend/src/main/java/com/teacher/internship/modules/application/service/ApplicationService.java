package com.teacher.internship.modules.application.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.notice.service.NoticeTriggerService;
import com.teacher.internship.modules.application.dto.ApplicationReviewRequest;
import com.teacher.internship.modules.application.dto.AssignmentAdjustRequest;
import com.teacher.internship.modules.application.dto.AssignmentManualRequest;
import com.teacher.internship.modules.application.dto.StudentApplicationSaveRequest;
import com.teacher.internship.modules.application.entity.BizApplicationPreference;
import com.teacher.internship.modules.application.entity.BizAssignment;
import com.teacher.internship.modules.application.entity.BizStudentApplication;
import com.teacher.internship.modules.application.mapper.BizApplicationPreferenceMapper;
import com.teacher.internship.modules.application.mapper.BizAssignmentMapper;
import com.teacher.internship.modules.application.mapper.BizStudentApplicationMapper;
import com.teacher.internship.modules.application.vo.ApplicationPreferenceVO;
import com.teacher.internship.modules.application.vo.AdminApplicationItemVO;
import com.teacher.internship.modules.application.vo.AdminApplicationPageVO;
import com.teacher.internship.modules.application.vo.AssignmentCandidateItemVO;
import com.teacher.internship.modules.application.vo.AssignmentCandidatePageVO;
import com.teacher.internship.modules.application.vo.AssignmentImportErrorVO;
import com.teacher.internship.modules.application.vo.AssignmentImportResultVO;
import com.teacher.internship.modules.application.vo.AssignmentItemVO;
import com.teacher.internship.modules.application.vo.AssignmentOptionVO;
import com.teacher.internship.modules.application.vo.AssignmentPageVO;
import com.teacher.internship.modules.application.vo.OptionItemVO;
import com.teacher.internship.modules.application.vo.StudentApplicationItemVO;
import com.teacher.internship.modules.application.vo.StudentApplicationPageVO;
import com.teacher.internship.modules.application.vo.StudentAvailablePlanVO;
import com.teacher.internship.modules.base.entity.BaseDepartment;
import com.teacher.internship.modules.base.entity.BaseGrade;
import com.teacher.internship.modules.base.entity.BaseInternshipBase;
import com.teacher.internship.modules.base.entity.BaseMajor;
import com.teacher.internship.modules.base.mapper.BaseDepartmentMapper;
import com.teacher.internship.modules.base.mapper.BaseGradeMapper;
import com.teacher.internship.modules.base.mapper.BaseInternshipBaseMapper;
import com.teacher.internship.modules.base.mapper.BaseMajorMapper;
import com.teacher.internship.modules.plan.entity.BizInternshipPlan;
import com.teacher.internship.modules.plan.entity.BizPlanBase;
import com.teacher.internship.modules.plan.mapper.BizInternshipPlanMapper;
import com.teacher.internship.modules.plan.mapper.BizPlanBaseMapper;
import com.teacher.internship.modules.plan.vo.PlanBaseVO;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import com.teacher.internship.modules.system.service.RbacService;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_INNER_TEACHER = "INNER_TEACHER";
    private static final String ROLE_BASE_TEACHER = "BASE_TEACHER";
    private static final String ROLE_DEPT_ADMIN = "DEPT_ADMIN";
    private static final String ROLE_SYS_ADMIN = "SYS_ADMIN";

    private static final String STATUS_ENABLED = "ENABLED";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_FINISHED = "FINISHED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";
    private static final String ASSIGN_STATUS_ASSIGNED = "ASSIGNED";
    private static final String ASSIGN_STATUS_ADJUSTED = "ADJUSTED";
    private static final String IMPORT_OP_ASSIGN = "ASSIGN";
    private static final String IMPORT_OP_ADJUST = "ADJUST";
    private static final String IMPORT_OP_AUTO = "AUTO";

    private final BizStudentApplicationMapper studentApplicationMapper;
    private final BizApplicationPreferenceMapper applicationPreferenceMapper;
    private final BizAssignmentMapper assignmentMapper;
    private final BizInternshipPlanMapper planMapper;
    private final BizPlanBaseMapper planBaseMapper;
    private final SysUserMapper userMapper;
    private final BaseInternshipBaseMapper internshipBaseMapper;
    private final BaseDepartmentMapper departmentMapper;
    private final BaseMajorMapper majorMapper;
    private final BaseGradeMapper gradeMapper;
    private final RbacService rbacService;
    private final TransactionTemplate transactionTemplate;
    private final NoticeTriggerService noticeTriggerService;

    public ApplicationService(BizStudentApplicationMapper studentApplicationMapper,
                              BizApplicationPreferenceMapper applicationPreferenceMapper,
                              BizAssignmentMapper assignmentMapper,
                              BizInternshipPlanMapper planMapper,
                              BizPlanBaseMapper planBaseMapper,
                              SysUserMapper userMapper,
                              BaseInternshipBaseMapper internshipBaseMapper,
                              BaseDepartmentMapper departmentMapper,
                              BaseMajorMapper majorMapper,
                              BaseGradeMapper gradeMapper,
                              RbacService rbacService,
                              TransactionTemplate transactionTemplate,
                              NoticeTriggerService noticeTriggerService) {
        this.studentApplicationMapper = studentApplicationMapper;
        this.applicationPreferenceMapper = applicationPreferenceMapper;
        this.assignmentMapper = assignmentMapper;
        this.planMapper = planMapper;
        this.planBaseMapper = planBaseMapper;
        this.userMapper = userMapper;
        this.internshipBaseMapper = internshipBaseMapper;
        this.departmentMapper = departmentMapper;
        this.majorMapper = majorMapper;
        this.gradeMapper = gradeMapper;
        this.rbacService = rbacService;
        this.transactionTemplate = transactionTemplate;
        this.noticeTriggerService = noticeTriggerService;
    }

    public List<StudentAvailablePlanVO> queryStudentAvailablePlans(Long userId, String roleCode) {
        SysUser student = requireStudentOperator(userId, roleCode);
        List<BizInternshipPlan> planList = planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getDeleted, 0L)
                .eq(BizInternshipPlan::getPlanStatus, STATUS_PUBLISHED)
                .eq(BizInternshipPlan::getDeptId, student.getDeptId())
                .ge(BizInternshipPlan::getApplyDeadline, LocalDateTime.now())
                .orderByAsc(BizInternshipPlan::getApplyDeadline)
                .orderByDesc(BizInternshipPlan::getId));
        Map<Long, BizStudentApplication> applicationMap = queryApplicationMapByStudentAndPlanIds(
                student.getId(),
                planList.stream().map(BizInternshipPlan::getId).collect(Collectors.toSet())
        );
        Map<Long, List<PlanBaseVO>> planBaseMap = queryPlanBaseVOMapByPlanIds(
                planList.stream().map(BizInternshipPlan::getId).collect(Collectors.toSet()),
                true
        );
        return planList.stream()
                .map(plan -> toStudentAvailablePlanVO(
                        plan,
                        planBaseMap.get(plan.getId()),
                        applicationMap.get(plan.getId())
                ))
                .collect(Collectors.toList());
    }

    public StudentApplicationPageVO queryStudentApplicationPage(long page,
                                                                long size,
                                                                String keyword,
                                                                String applicationStatus,
                                                                Long userId,
                                                                String roleCode) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        SysUser student = requireStudentOperator(userId, roleCode);

        LambdaQueryWrapper<BizStudentApplication> wrapper = new LambdaQueryWrapper<BizStudentApplication>()
                .eq(BizStudentApplication::getDeleted, 0L)
                .eq(BizStudentApplication::getStudentId, student.getId())
                .orderByDesc(BizStudentApplication::getSubmittedTime)
                .orderByDesc(BizStudentApplication::getId);

        if (StringUtils.hasText(applicationStatus)) {
            wrapper.eq(BizStudentApplication::getApplicationStatus, normalizeCode(applicationStatus));
        }

        if (StringUtils.hasText(keyword)) {
            Set<Long> deptPlanIds = queryPlanIdsByDept(student.getDeptId());
            Set<Long> planKeywordIds = findPlanIdsByKeyword(keyword, deptPlanIds);
            wrapper.and(w -> {
                w.like(BizStudentApplication::getPersonalStatement, keyword);
                if (!CollectionUtils.isEmpty(planKeywordIds)) {
                    w.or().in(BizStudentApplication::getPlanId, planKeywordIds);
                }
            });
        }

        Page<BizStudentApplication> pageResult = studentApplicationMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        List<BizStudentApplication> records = pageResult.getRecords();
        Set<Long> planIds = records.stream().map(BizStudentApplication::getPlanId).collect(Collectors.toSet());
        Set<Long> reviewerIds = records.stream().map(BizStudentApplication::getReviewedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> applicationIds = records.stream().map(BizStudentApplication::getId).collect(Collectors.toSet());

        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);
        Map<Long, SysUser> reviewerMap = queryUserMap(reviewerIds);
        Map<Long, BizAssignment> currentAssignmentMap = queryCurrentAssignmentMapByApplicationIds(applicationIds);
        Map<Long, List<ApplicationPreferenceVO>> preferenceMap = queryPreferenceVOMapByApplicationIds(applicationIds);

        List<StudentApplicationItemVO> voRecords = records.stream()
                .map(entity -> toStudentApplicationItemVO(
                        entity,
                        planMap.get(entity.getPlanId()),
                        reviewerMap.get(entity.getReviewedBy()),
                        preferenceMap.get(entity.getId()),
                        currentAssignmentMap.containsKey(entity.getId())
                ))
                .collect(Collectors.toList());

        StudentApplicationPageVO result = new StudentApplicationPageVO();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal(pageResult.getTotal());
        result.setRecords(voRecords);
        return result;
    }

    public StudentApplicationItemVO getStudentApplicationDetail(Long applicationId, Long userId, String roleCode) {
        requireStudentOperator(userId, roleCode);
        BizStudentApplication application = requireApplication(applicationId);
        if (!Objects.equals(application.getStudentId(), userId)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看该申请");
        }
        BizInternshipPlan plan = requirePlan(application.getPlanId());
        SysUser reviewer = requireUserOrNull(application.getReviewedBy());
        boolean hasCurrentAssignment = hasCurrentAssignmentByApplicationId(application.getId());
        List<ApplicationPreferenceVO> preferences = queryPreferenceVOMapByApplicationIds(Collections.singleton(application.getId()))
                .getOrDefault(application.getId(), new ArrayList<>());
        return toStudentApplicationItemVO(application, plan, reviewer, preferences, hasCurrentAssignment);
    }

    @Transactional(rollbackFor = Exception.class)
    public StudentApplicationItemVO submitStudentApplication(StudentApplicationSaveRequest request,
                                                             Long userId,
                                                             String roleCode) {
        SysUser student = requireStudentOperator(userId, roleCode);
        BizInternshipPlan plan = requirePlan(request.getPlanId());
        ensureStudentCanApplyPlan(student, plan);
        List<Long> preferredBaseIds = normalizePreferredBaseIds(request.getPreferredBaseIds(), plan);

        BizStudentApplication existing = findApplicationByPlanAndStudent(plan.getId(), student.getId());
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            BizStudentApplication entity = new BizStudentApplication();
            entity.setPlanId(plan.getId());
            entity.setStudentId(student.getId());
            applyStudentApplicationForm(entity, request);
            entity.setApplicationStatus(STATUS_SUBMITTED);
            entity.setSubmittedTime(now);
            entity.setCreatedBy(userId);
            entity.setUpdatedBy(userId);
            entity.setDeleted(0L);
            studentApplicationMapper.insert(entity);
            replaceApplicationPreferences(entity.getId(), preferredBaseIds, userId);
            noticeTriggerService.notifyApplicationSubmitted(entity, plan, student);
            return toStudentApplicationItemVO(
                    entity,
                    plan,
                    null,
                    queryPreferenceVOMapByApplicationIds(Collections.singleton(entity.getId())).get(entity.getId()),
                    false
            );
        }

        if (!STATUS_WITHDRAWN.equals(existing.getApplicationStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有已撤回的申请才可以重新提交");
        }
        if (hasCurrentAssignmentByApplicationId(existing.getId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该申请已经完成分配，不能重新提交");
        }

        applyStudentApplicationForm(existing, request);
        existing.setApplicationStatus(STATUS_SUBMITTED);
        existing.setReviewComment(null);
        existing.setReviewedBy(null);
        existing.setReviewedTime(null);
        existing.setSubmittedTime(now);
        existing.setUpdatedBy(userId);
        existing.setUpdatedTime(now);
        studentApplicationMapper.updateById(existing);
        replaceApplicationPreferences(existing.getId(), preferredBaseIds, userId);
        noticeTriggerService.notifyApplicationSubmitted(existing, plan, student);
        return toStudentApplicationItemVO(
                existing,
                plan,
                null,
                queryPreferenceVOMapByApplicationIds(Collections.singleton(existing.getId())).get(existing.getId()),
                false
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public StudentApplicationItemVO updateStudentApplication(Long applicationId,
                                                             StudentApplicationSaveRequest request,
                                                             Long userId,
                                                             String roleCode) {
        SysUser student = requireStudentOperator(userId, roleCode);
        BizStudentApplication application = requireApplication(applicationId);
        if (!Objects.equals(application.getStudentId(), student.getId())) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权编辑该申请");
        }
        if (!Objects.equals(application.getPlanId(), request.getPlanId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "不能修改所属计划");
        }
        if (!STATUS_SUBMITTED.equals(application.getApplicationStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有待审核申请才可以编辑");
        }
        if (hasCurrentAssignmentByApplicationId(application.getId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该申请已完成分配，不能编辑");
        }

        BizInternshipPlan plan = requirePlan(application.getPlanId());
        ensureStudentCanApplyPlan(student, plan);
        List<Long> preferredBaseIds = normalizePreferredBaseIds(request.getPreferredBaseIds(), plan);

        applyStudentApplicationForm(application, request);
        application.setUpdatedBy(userId);
        application.setUpdatedTime(LocalDateTime.now());
        studentApplicationMapper.updateById(application);
        replaceApplicationPreferences(application.getId(), preferredBaseIds, userId);
        return toStudentApplicationItemVO(
                application,
                plan,
                null,
                queryPreferenceVOMapByApplicationIds(Collections.singleton(application.getId())).get(application.getId()),
                false
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdrawStudentApplication(Long applicationId, Long userId, String roleCode) {
        SysUser student = requireStudentOperator(userId, roleCode);
        BizStudentApplication application = requireApplication(applicationId);
        if (!Objects.equals(application.getStudentId(), student.getId())) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权撤回该申请");
        }
        if (!STATUS_SUBMITTED.equals(application.getApplicationStatus())
                && !STATUS_APPROVED.equals(application.getApplicationStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "当前状态不允许撤回");
        }
        if (hasCurrentAssignmentByApplicationId(application.getId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该申请已完成分配，不能撤回");
        }

        BizInternshipPlan plan = requirePlan(application.getPlanId());
        ensureStudentCanApplyPlan(student, plan);
        application.setApplicationStatus(STATUS_WITHDRAWN);
        application.setUpdatedBy(userId);
        application.setUpdatedTime(LocalDateTime.now());
        studentApplicationMapper.updateById(application);
    }

    public AdminApplicationPageVO queryAdminApplicationPage(long page,
                                                            long size,
                                                            String keyword,
                                                            Long planId,
                                                            String applicationStatus,
                                                            String studentNo,
                                                            String studentName,
                                                            Long userId,
                                                            String roleCode) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        SysUser admin = requireAdminOperator(userId, roleCode);
        Set<Long> scopePlanIds = resolveAdminPlanScope(admin, roleCode);

        if (scopePlanIds != null && scopePlanIds.isEmpty()) {
            return emptyAdminApplicationPage(safePage, safeSize);
        }

        LambdaQueryWrapper<BizStudentApplication> wrapper = new LambdaQueryWrapper<BizStudentApplication>()
                .eq(BizStudentApplication::getDeleted, 0L)
                .orderByDesc(BizStudentApplication::getSubmittedTime)
                .orderByDesc(BizStudentApplication::getId);

        if (scopePlanIds != null) {
            wrapper.in(BizStudentApplication::getPlanId, scopePlanIds);
        }

        if (planId != null) {
            if (scopePlanIds != null && !scopePlanIds.contains(planId)) {
                return emptyAdminApplicationPage(safePage, safeSize);
            }
            wrapper.eq(BizStudentApplication::getPlanId, planId);
        }

        if (StringUtils.hasText(applicationStatus)) {
            wrapper.eq(BizStudentApplication::getApplicationStatus, normalizeCode(applicationStatus));
        }

        if (StringUtils.hasText(studentNo)) {
            SysUser student = findUserByStudentNo(studentNo.trim());
            if (student == null) {
                return emptyAdminApplicationPage(safePage, safeSize);
            }
            wrapper.eq(BizStudentApplication::getStudentId, student.getId());
        }

        if (StringUtils.hasText(studentName)) {
            Set<Long> studentIds = findStudentIdsByNameKeyword(studentName.trim());
            if (CollectionUtils.isEmpty(studentIds)) {
                return emptyAdminApplicationPage(safePage, safeSize);
            }
            wrapper.in(BizStudentApplication::getStudentId, studentIds);
        }

        if (StringUtils.hasText(keyword)) {
            Set<Long> planKeywordIds = findPlanIdsByKeyword(keyword, scopePlanIds);
            Set<Long> studentKeywordIds = findStudentIdsByKeyword(keyword);
            wrapper.and(w -> {
                w.like(BizStudentApplication::getPersonalStatement, keyword);
                if (!CollectionUtils.isEmpty(planKeywordIds)) {
                    w.or().in(BizStudentApplication::getPlanId, planKeywordIds);
                }
                if (!CollectionUtils.isEmpty(studentKeywordIds)) {
                    w.or().in(BizStudentApplication::getStudentId, studentKeywordIds);
                }
            });
        }

        Page<BizStudentApplication> pageResult = studentApplicationMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        List<BizStudentApplication> records = pageResult.getRecords();
        Set<Long> planIds = records.stream().map(BizStudentApplication::getPlanId).collect(Collectors.toSet());
        Set<Long> studentIds = records.stream().map(BizStudentApplication::getStudentId).collect(Collectors.toSet());
        Set<Long> reviewerIds = records.stream().map(BizStudentApplication::getReviewedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> appIds = records.stream().map(BizStudentApplication::getId).collect(Collectors.toSet());

        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);
        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(studentIds);
        allUserIds.addAll(reviewerIds);
        Map<Long, SysUser> userMap = queryUserMap(allUserIds);
        Map<Long, BaseDepartment> deptMap = queryDepartmentMap(extractDeptIds(userMap.values()));
        Map<Long, BaseMajor> majorMap = queryMajorMap(extractMajorIds(userMap.values()));
        Map<Long, BaseGrade> gradeMap = queryGradeMap(extractGradeIds(userMap.values()));
        Map<Long, BizAssignment> currentAssignmentMap = queryCurrentAssignmentMapByApplicationIds(appIds);
        Map<Long, List<ApplicationPreferenceVO>> preferenceMap = queryPreferenceVOMapByApplicationIds(appIds);

        List<AdminApplicationItemVO> voRecords = records.stream()
                .map(entity -> toAdminApplicationItemVO(
                        entity,
                        planMap.get(entity.getPlanId()),
                        userMap.get(entity.getStudentId()),
                        userMap.get(entity.getReviewedBy()),
                        currentAssignmentMap.get(entity.getId()),
                        preferenceMap.get(entity.getId()),
                        deptMap,
                        majorMap,
                        gradeMap
                ))
                .collect(Collectors.toList());

        AdminApplicationPageVO result = new AdminApplicationPageVO();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal(pageResult.getTotal());
        result.setRecords(voRecords);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminApplicationItemVO reviewApplication(Long applicationId,
                                                    ApplicationReviewRequest request,
                                                    Long userId,
                                                    String roleCode) {
        SysUser admin = requireAdminOperator(userId, roleCode);
        BizStudentApplication application = requireApplication(applicationId);
        BizInternshipPlan plan = requirePlan(application.getPlanId());
        ensureAdminCanAccessPlan(plan, admin, roleCode);

        if (!STATUS_SUBMITTED.equals(application.getApplicationStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有待审核申请才可以审核");
        }

        String nextStatus = normalizeCode(request.getStatus());
        if (!STATUS_APPROVED.equals(nextStatus) && !STATUS_REJECTED.equals(nextStatus)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "审核结果只能为通过或驳回");
        }

        String reviewComment = trimToNull(request.getReviewComment());
        if (STATUS_REJECTED.equals(nextStatus) && !StringUtils.hasText(reviewComment)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "驳回时必须填写审核意见");
        }

        LocalDateTime now = LocalDateTime.now();
        application.setApplicationStatus(nextStatus);
        application.setReviewComment(reviewComment);
        application.setReviewedBy(userId);
        application.setReviewedTime(now);
        application.setUpdatedBy(userId);
        application.setUpdatedTime(now);
        studentApplicationMapper.updateById(application);

        SysUser student = requireUser(application.getStudentId());
        SysUser reviewer = requireUser(userId);
        BizAssignment currentAssignment = findCurrentAssignmentByApplicationId(application.getId());
        Map<Long, BaseDepartment> deptMap = queryDepartmentMap(extractDeptIds(Collections.singleton(student)));
        Map<Long, BaseMajor> majorMap = queryMajorMap(extractMajorIds(Collections.singleton(student)));
        Map<Long, BaseGrade> gradeMap = queryGradeMap(extractGradeIds(Collections.singleton(student)));
        noticeTriggerService.notifyApplicationReviewed(application, plan, reviewer);
        List<ApplicationPreferenceVO> preferences = queryPreferenceVOMapByApplicationIds(Collections.singleton(application.getId()))
                .getOrDefault(application.getId(), new ArrayList<>());
        return toAdminApplicationItemVO(application, plan, student, reviewer, currentAssignment, preferences, deptMap, majorMap, gradeMap);
    }

    public AssignmentCandidatePageVO queryAssignmentCandidatePage(long page,
                                                                  long size,
                                                                  String keyword,
                                                                  Long planId,
                                                                  String studentNo,
                                                                  Long userId,
                                                                  String roleCode) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        SysUser admin = requireAdminOperator(userId, roleCode);
        Set<Long> scopePlanIds = resolveAdminPlanScope(admin, roleCode);

        if (scopePlanIds != null && scopePlanIds.isEmpty()) {
            return emptyAssignmentCandidatePage(safePage, safeSize);
        }

        LambdaQueryWrapper<BizStudentApplication> wrapper = new LambdaQueryWrapper<BizStudentApplication>()
                .eq(BizStudentApplication::getDeleted, 0L)
                .eq(BizStudentApplication::getApplicationStatus, STATUS_APPROVED)
                .orderByDesc(BizStudentApplication::getSubmittedTime)
                .orderByDesc(BizStudentApplication::getId);

        if (scopePlanIds != null) {
            wrapper.in(BizStudentApplication::getPlanId, scopePlanIds);
        }

        if (planId != null) {
            if (scopePlanIds != null && !scopePlanIds.contains(planId)) {
                return emptyAssignmentCandidatePage(safePage, safeSize);
            }
            wrapper.eq(BizStudentApplication::getPlanId, planId);
        }

        if (StringUtils.hasText(studentNo)) {
            SysUser student = findUserByStudentNo(studentNo.trim());
            if (student == null) {
                return emptyAssignmentCandidatePage(safePage, safeSize);
            }
            wrapper.eq(BizStudentApplication::getStudentId, student.getId());
        }

        if (StringUtils.hasText(keyword)) {
            Set<Long> planKeywordIds = findPlanIdsByKeyword(keyword, scopePlanIds);
            Set<Long> studentKeywordIds = findStudentIdsByKeyword(keyword);
            wrapper.and(w -> {
                w.like(BizStudentApplication::getPersonalStatement, keyword);
                if (!CollectionUtils.isEmpty(planKeywordIds)) {
                    w.or().in(BizStudentApplication::getPlanId, planKeywordIds);
                }
                if (!CollectionUtils.isEmpty(studentKeywordIds)) {
                    w.or().in(BizStudentApplication::getStudentId, studentKeywordIds);
                }
            });
        }

        List<BizStudentApplication> approvedList = studentApplicationMapper.selectList(wrapper);
        Set<Long> appIds = approvedList.stream().map(BizStudentApplication::getId).collect(Collectors.toSet());
        Map<Long, BizAssignment> currentAssignmentMap = queryCurrentAssignmentMapByApplicationIds(appIds);
        List<BizStudentApplication> noAssignedList = approvedList.stream()
                .filter(item -> !currentAssignmentMap.containsKey(item.getId()))
                .collect(Collectors.toList());

        Set<Long> planIds = noAssignedList.stream().map(BizStudentApplication::getPlanId).collect(Collectors.toSet());
        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);
        noAssignedList = noAssignedList.stream()
                .filter(item -> isPlanAssignableStatus(planMap.get(item.getPlanId())))
                .collect(Collectors.toList());

        Set<Long> studentIds = noAssignedList.stream().map(BizStudentApplication::getStudentId).collect(Collectors.toSet());
        Set<Long> applicationIds = noAssignedList.stream().map(BizStudentApplication::getId).collect(Collectors.toSet());
        Map<Long, SysUser> studentMap = queryUserMap(studentIds);
        Map<Long, BaseDepartment> deptMap = queryDepartmentMap(extractDeptIds(studentMap.values()));
        Map<Long, BaseMajor> majorMap = queryMajorMap(extractMajorIds(studentMap.values()));
        Map<Long, BaseGrade> gradeMap = queryGradeMap(extractGradeIds(studentMap.values()));
        Map<Long, List<ApplicationPreferenceVO>> preferenceMap = queryPreferenceVOMapByApplicationIds(applicationIds);

        List<AssignmentCandidateItemVO> candidateRecords = noAssignedList.stream()
                .map(item -> toAssignmentCandidateItemVO(
                        item,
                        planMap.get(item.getPlanId()),
                        studentMap.get(item.getStudentId()),
                        preferenceMap.get(item.getId()),
                        deptMap,
                        majorMap,
                        gradeMap
                ))
                .collect(Collectors.toList());

        List<AssignmentCandidateItemVO> pageRecords = slice(candidateRecords, safePage, safeSize);
        AssignmentCandidatePageVO result = new AssignmentCandidatePageVO();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal((long) candidateRecords.size());
        result.setRecords(pageRecords);
        return result;
    }

    public AssignmentOptionVO queryAssignmentOptions(Long userId, String roleCode, Long planId) {
        SysUser admin = requireAdminOperator(userId, roleCode);
        List<BaseInternshipBase> bases;
        if (planId != null) {
            BizInternshipPlan plan = requirePlan(planId);
            ensureAdminCanAccessPlan(plan, admin, roleCode);
            Set<Long> baseIds = queryEnabledPlanBaseEntities(planId).stream()
                    .map(BizPlanBase::getBaseId)
                    .collect(Collectors.toSet());
            bases = CollectionUtils.isEmpty(baseIds)
                    ? new ArrayList<>()
                    : internshipBaseMapper.selectList(new LambdaQueryWrapper<BaseInternshipBase>()
                    .in(BaseInternshipBase::getId, baseIds)
                    .eq(BaseInternshipBase::getDeleted, 0L)
                    .eq(BaseInternshipBase::getStatus, STATUS_ENABLED)
                    .orderByAsc(BaseInternshipBase::getBaseName)
                    .orderByAsc(BaseInternshipBase::getId));
        } else {
            bases = internshipBaseMapper.selectList(new LambdaQueryWrapper<BaseInternshipBase>()
                    .eq(BaseInternshipBase::getDeleted, 0L)
                    .eq(BaseInternshipBase::getStatus, STATUS_ENABLED)
                    .orderByAsc(BaseInternshipBase::getBaseName)
                    .orderByAsc(BaseInternshipBase::getId));
        }

        List<SysUser> teacherCandidates = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0L)
                .eq(SysUser::getStatus, STATUS_ENABLED)
                .isNotNull(SysUser::getTeacherNo)
                .orderByAsc(SysUser::getRealName)
                .orderByAsc(SysUser::getId));

        List<OptionItemVO> baseOptions = bases.stream()
                .map(item -> new OptionItemVO(item.getId(), item.getBaseCode(), buildDisplayName(item.getBaseName(), item.getBaseCode())))
                .collect(Collectors.toList());

        List<OptionItemVO> innerTeacherOptions = new ArrayList<>();
        List<OptionItemVO> baseTeacherOptions = new ArrayList<>();
        boolean deptAdmin = ROLE_DEPT_ADMIN.equals(normalizeCode(roleCode));

        for (SysUser teacher : teacherCandidates) {
            if (rbacService.userHasRoleCode(teacher.getId(), ROLE_INNER_TEACHER)) {
                if (!deptAdmin || Objects.equals(teacher.getDeptId(), admin.getDeptId())) {
                    innerTeacherOptions.add(new OptionItemVO(
                            teacher.getId(),
                            teacher.getTeacherNo(),
                            buildDisplayName(teacher.getRealName(), teacher.getTeacherNo())
                    ));
                }
            }
            if (rbacService.userHasRoleCode(teacher.getId(), ROLE_BASE_TEACHER)) {
                baseTeacherOptions.add(new OptionItemVO(
                        teacher.getId(),
                        teacher.getTeacherNo(),
                        buildDisplayName(teacher.getRealName(), teacher.getTeacherNo())
                ));
            }
        }

        AssignmentOptionVO result = new AssignmentOptionVO();
        result.setInternshipBases(baseOptions);
        result.setInnerTeachers(innerTeacherOptions);
        result.setBaseTeachers(baseTeacherOptions);
        return result;
    }

    public AssignmentPageVO queryAssignmentPage(long page,
                                                long size,
                                                String keyword,
                                                Long planId,
                                                String studentNo,
                                                Integer isCurrent,
                                                Long userId,
                                                String roleCode) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        SysUser admin = requireAdminOperator(userId, roleCode);
        Set<Long> scopePlanIds = resolveAdminPlanScope(admin, roleCode);

        if (scopePlanIds != null && scopePlanIds.isEmpty()) {
            return emptyAssignmentPage(safePage, safeSize);
        }

        LambdaQueryWrapper<BizAssignment> wrapper = new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getDeleted, 0L)
                .orderByDesc(BizAssignment::getAssignedTime)
                .orderByDesc(BizAssignment::getVersionNo)
                .orderByDesc(BizAssignment::getId);

        if (scopePlanIds != null) {
            wrapper.in(BizAssignment::getPlanId, scopePlanIds);
        }

        if (planId != null) {
            if (scopePlanIds != null && !scopePlanIds.contains(planId)) {
                return emptyAssignmentPage(safePage, safeSize);
            }
            wrapper.eq(BizAssignment::getPlanId, planId);
        }

        if (isCurrent != null) {
            wrapper.eq(BizAssignment::getIsCurrent, isCurrent);
        }

        if (StringUtils.hasText(studentNo)) {
            SysUser student = findUserByStudentNo(studentNo.trim());
            if (student == null) {
                return emptyAssignmentPage(safePage, safeSize);
            }
            wrapper.eq(BizAssignment::getStudentId, student.getId());
        }

        if (StringUtils.hasText(keyword)) {
            Set<Long> planKeywordIds = findPlanIdsByKeyword(keyword, scopePlanIds);
            Set<Long> studentKeywordIds = findStudentIdsByKeyword(keyword);
            Set<Long> baseKeywordIds = findBaseIdsByKeyword(keyword);
            Set<Long> teacherKeywordIds = findTeacherIdsByKeyword(keyword);
            wrapper.and(w -> {
                w.like(BizAssignment::getAdjustReason, keyword);
                if (!CollectionUtils.isEmpty(planKeywordIds)) {
                    w.or().in(BizAssignment::getPlanId, planKeywordIds);
                }
                if (!CollectionUtils.isEmpty(studentKeywordIds)) {
                    w.or().in(BizAssignment::getStudentId, studentKeywordIds);
                }
                if (!CollectionUtils.isEmpty(baseKeywordIds)) {
                    w.or().in(BizAssignment::getBaseId, baseKeywordIds);
                }
                if (!CollectionUtils.isEmpty(teacherKeywordIds)) {
                    w.or().in(BizAssignment::getInnerTeacherId, teacherKeywordIds)
                            .or().in(BizAssignment::getBaseTeacherId, teacherKeywordIds);
                }
            });
        }

        Page<BizAssignment> pageResult = assignmentMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        return buildAssignmentPageVO(safePage, safeSize, pageResult);
    }

    public AssignmentPageVO queryMyAssignmentPage(long page,
                                                  long size,
                                                  String keyword,
                                                  Long userId,
                                                  String roleCode) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        requireUser(userId);
        String normalizedRoleCode = normalizeCode(roleCode);
        if (ROLE_SYS_ADMIN.equals(normalizedRoleCode) || ROLE_DEPT_ADMIN.equals(normalizedRoleCode)) {
            return queryAssignmentPage(safePage, safeSize, keyword, null, null, 1, userId, roleCode);
        }

        LambdaQueryWrapper<BizAssignment> wrapper = new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getDeleted, 0L)
                .eq(BizAssignment::getIsCurrent, 1)
                .orderByDesc(BizAssignment::getAssignedTime)
                .orderByDesc(BizAssignment::getVersionNo)
                .orderByDesc(BizAssignment::getId);

        if (ROLE_STUDENT.equals(normalizedRoleCode)) {
            wrapper.eq(BizAssignment::getStudentId, userId);
        } else if (ROLE_INNER_TEACHER.equals(normalizedRoleCode)) {
            wrapper.eq(BizAssignment::getInnerTeacherId, userId);
        } else if (ROLE_BASE_TEACHER.equals(normalizedRoleCode)) {
            wrapper.eq(BizAssignment::getBaseTeacherId, userId);
        } else {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看分配信息");
        }

        if (StringUtils.hasText(keyword)) {
            Set<Long> planKeywordIds = findPlanIdsByKeyword(keyword, null);
            wrapper.and(w -> {
                w.like(BizAssignment::getAdjustReason, keyword);
                if (!CollectionUtils.isEmpty(planKeywordIds)) {
                    w.or().in(BizAssignment::getPlanId, planKeywordIds);
                }
            });
        }

        Page<BizAssignment> pageResult = assignmentMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        return buildAssignmentPageVO(safePage, safeSize, pageResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public AssignmentItemVO manualAssign(AssignmentManualRequest request, Long userId, String roleCode) {
        SysUser admin = requireAdminOperator(userId, roleCode);
        BizStudentApplication application = requireApplication(request.getApplicationId());
        BizInternshipPlan plan = requirePlan(application.getPlanId());
        ensureAdminCanAccessPlan(plan, admin, roleCode);
        ensurePlanAssignable(plan);

        if (!STATUS_APPROVED.equals(application.getApplicationStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有已通过的申请才可以分配");
        }

        SysUser student = requireUser(application.getStudentId());
        ensureStudentInPlanDept(student, plan);

        if (findCurrentAssignmentByPlanAndStudent(plan.getId(), student.getId()) != null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该学生在当前计划下已经存在分配记录");
        }

        ensurePlanBaseAssignable(plan.getId(), request.getBaseId(), null);
        requireTeacherWithRole(request.getInnerTeacherId(), ROLE_INNER_TEACHER, plan, true);
        requireTeacherWithRole(request.getBaseTeacherId(), ROLE_BASE_TEACHER, plan, false);

        int versionNo = nextAssignmentVersionNo(plan.getId(), student.getId());
        BizAssignment assignment = buildAssignmentEntity(
                plan.getId(),
                application.getId(),
                student.getId(),
                request.getBaseId(),
                request.getInnerTeacherId(),
                request.getBaseTeacherId(),
                versionNo,
                1,
                ASSIGN_STATUS_ASSIGNED,
                trimToNull(request.getAdjustReason()),
                userId,
                LocalDateTime.now()
        );
        assignmentMapper.insert(assignment);
        noticeTriggerService.notifyAssignmentCompleted(assignment, plan, userId, false);
        return buildAssignmentItem(assignment);
    }

    @Transactional(rollbackFor = Exception.class)
    public AssignmentItemVO adjustAssignment(Long assignmentId,
                                             AssignmentAdjustRequest request,
                                             Long userId,
                                             String roleCode) {
        SysUser admin = requireAdminOperator(userId, roleCode);
        BizAssignment current = requireAssignment(assignmentId);
        if (current.getIsCurrent() == null || current.getIsCurrent() != 1) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有当前版本的分配记录才可以调整");
        }

        BizInternshipPlan plan = requirePlan(current.getPlanId());
        ensureAdminCanAccessPlan(plan, admin, roleCode);
        ensurePlanAssignable(plan);

        String adjustReason = trimToNull(request.getAdjustReason());
        if (!StringUtils.hasText(adjustReason)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "调整原因不能为空");
        }

        ensurePlanBaseAssignable(plan.getId(), request.getBaseId(), current.getId());
        requireTeacherWithRole(request.getInnerTeacherId(), ROLE_INNER_TEACHER, plan, true);
        requireTeacherWithRole(request.getBaseTeacherId(), ROLE_BASE_TEACHER, plan, false);

        BizAssignment newAssignment = doAdjustAssignment(
                current,
                request.getBaseId(),
                request.getInnerTeacherId(),
                request.getBaseTeacherId(),
                adjustReason,
                userId
        );
        noticeTriggerService.notifyAssignmentCompleted(newAssignment, plan, userId, true);
        return buildAssignmentItem(newAssignment);
    }

    public AssignmentImportResultVO importAssignments(MultipartFile file, Long userId, String roleCode) {
        SysUser admin = requireAdminOperator(userId, roleCode);
        String normalizedRoleCode = normalizeCode(roleCode);

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "请上传导入文件");
        }

        String fileName = file.getOriginalFilename();
        String lowerFileName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (!lowerFileName.endsWith(".xlsx") && !lowerFileName.endsWith(".xls")) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "仅支持上传 .xlsx 或 .xls 文件");
        }

        AssignmentImportResultVO result = new AssignmentImportResultVO();
        result.setTotalRows(0);
        result.setSuccessRows(0);
        result.setFailedRows(0);

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return result;
            }

            DataFormatter formatter = new DataFormatter();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isImportRowEmpty(row, formatter)) {
                    continue;
                }

                int excelRowNo = rowIndex + 1;
                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    transactionTemplate.executeWithoutResult(status ->
                            processImportRow(row, formatter, admin, normalizedRoleCode, userId)
                    );
                    result.setSuccessRows(result.getSuccessRows() + 1);
                } catch (Exception ex) {
                    result.setFailedRows(result.getFailedRows() + 1);
                    result.getErrors().add(new AssignmentImportErrorVO(excelRowNo, rootMessage(ex)));
                }
            }

            return result;
        } catch (IOException ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "读取导入文件失败");
        } catch (RuntimeException ex) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "导入文件内容格式不正确");
        }
    }

    private void processImportRow(Row row,
                                  DataFormatter formatter,
                                  SysUser admin,
                                  String roleCode,
                                  Long operatorId) {
        String planCode = cellText(row, 0, formatter);
        String studentNo = cellText(row, 1, formatter);
        String baseCode = cellText(row, 2, formatter);
        String innerTeacherNo = cellText(row, 3, formatter);
        String baseTeacherNo = cellText(row, 4, formatter);
        String adjustReason = trimToNull(cellText(row, 5, formatter));
        String operationType = normalizeImportOperationType(cellText(row, 6, formatter));

        if (!StringUtils.hasText(planCode)
                || !StringUtils.hasText(studentNo)
                || !StringUtils.hasText(baseCode)
                || !StringUtils.hasText(innerTeacherNo)
                || !StringUtils.hasText(baseTeacherNo)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(),
                    "计划编码、学号、基地编码、校内指导教师工号和基地指导教师工号不能为空");
        }

        if (!StringUtils.hasText(operationType)) {
            operationType = IMPORT_OP_AUTO;
        }
        if (!IMPORT_OP_ASSIGN.equals(operationType)
                && !IMPORT_OP_ADJUST.equals(operationType)
                && !IMPORT_OP_AUTO.equals(operationType)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(),
                    "操作类型只能填写“分配”“调整”“自动”");
        }

        BizInternshipPlan plan = requirePlanByCode(planCode);
        ensureAdminCanAccessPlan(plan, admin, roleCode);
        ensurePlanAssignable(plan);

        SysUser student = requireStudentByStudentNo(studentNo);
        ensureStudentInPlanDept(student, plan);
        BizStudentApplication application = findApplicationByPlanAndStudent(plan.getId(), student.getId());
        if (application == null || !STATUS_APPROVED.equals(application.getApplicationStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(),
                    "计划编码为 " + planCode + "、学号为 " + studentNo + " 的学生不存在已通过申请");
        }

        BaseInternshipBase base = requireEnabledBaseByCode(baseCode);
        SysUser innerTeacher = requireTeacherByNoAndRole(innerTeacherNo, ROLE_INNER_TEACHER);
        SysUser baseTeacher = requireTeacherByNoAndRole(baseTeacherNo, ROLE_BASE_TEACHER);
        if (!Objects.equals(innerTeacher.getDeptId(), plan.getDeptId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(),
                    "校内指导教师必须与计划所属院系一致");
        }

        BizAssignment current = findCurrentAssignmentByPlanAndStudent(plan.getId(), student.getId());
        if (IMPORT_OP_ASSIGN.equals(operationType)) {
            if (current != null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(),
                        "当前已存在分配记录，请改用“调整”或“自动”");
            }
            ensurePlanBaseAssignable(plan.getId(), base.getId(), null);
            int versionNo = nextAssignmentVersionNo(plan.getId(), student.getId());
            BizAssignment assigned = buildAssignmentEntity(
                    plan.getId(),
                    application.getId(),
                    student.getId(),
                    base.getId(),
                    innerTeacher.getId(),
                    baseTeacher.getId(),
                    versionNo,
                    1,
                    ASSIGN_STATUS_ASSIGNED,
                    adjustReason,
                    operatorId,
                    LocalDateTime.now()
            );
            assignmentMapper.insert(assigned);
            noticeTriggerService.notifyAssignmentCompleted(assigned, plan, operatorId, false);
            return;
        }

        if (IMPORT_OP_ADJUST.equals(operationType)) {
            if (current == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(),
                        "当前不存在分配记录，请改用“分配”或“自动”");
            }
            if (!StringUtils.hasText(adjustReason)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(),
                        "操作类型为“调整”时，必须填写调整原因");
            }
            ensurePlanBaseAssignable(plan.getId(), base.getId(), current.getId());
            BizAssignment adjusted = doAdjustAssignment(
                    current,
                    base.getId(),
                    innerTeacher.getId(),
                    baseTeacher.getId(),
                    adjustReason,
                    operatorId
            );
            noticeTriggerService.notifyAssignmentCompleted(adjusted, plan, operatorId, true);
            return;
        }

        if (current == null) {
            ensurePlanBaseAssignable(plan.getId(), base.getId(), null);
            int versionNo = nextAssignmentVersionNo(plan.getId(), student.getId());
            BizAssignment assigned = buildAssignmentEntity(
                    plan.getId(),
                    application.getId(),
                    student.getId(),
                    base.getId(),
                    innerTeacher.getId(),
                    baseTeacher.getId(),
                    versionNo,
                    1,
                    ASSIGN_STATUS_ASSIGNED,
                    adjustReason,
                    operatorId,
                    LocalDateTime.now()
            );
            assignmentMapper.insert(assigned);
            noticeTriggerService.notifyAssignmentCompleted(assigned, plan, operatorId, false);
            return;
        }

        String finalReason = StringUtils.hasText(adjustReason) ? adjustReason : "通过导入批量调整";
        ensurePlanBaseAssignable(plan.getId(), base.getId(), current.getId());
        BizAssignment adjusted = doAdjustAssignment(
                current,
                base.getId(),
                innerTeacher.getId(),
                baseTeacher.getId(),
                finalReason,
                operatorId
        );
        noticeTriggerService.notifyAssignmentCompleted(adjusted, plan, operatorId, true);
    }

    private BizAssignment doAdjustAssignment(BizAssignment current,
                                             Long baseId,
                                             Long innerTeacherId,
                                             Long baseTeacherId,
                                             String adjustReason,
                                             Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        current.setIsCurrent(0);
        current.setAssignmentStatus(ASSIGN_STATUS_ADJUSTED);
        current.setUpdatedBy(operatorId);
        current.setUpdatedTime(now);
        assignmentMapper.updateById(current);

        BizAssignment newAssignment = buildAssignmentEntity(
                current.getPlanId(),
                current.getApplicationId(),
                current.getStudentId(),
                baseId,
                innerTeacherId,
                baseTeacherId,
                (current.getVersionNo() == null ? 0 : current.getVersionNo()) + 1,
                1,
                ASSIGN_STATUS_ASSIGNED,
                adjustReason,
                operatorId,
                now
        );
        assignmentMapper.insert(newAssignment);
        return newAssignment;
    }

    private BizAssignment buildAssignmentEntity(Long planId,
                                                Long applicationId,
                                                Long studentId,
                                                Long baseId,
                                                Long innerTeacherId,
                                                Long baseTeacherId,
                                                Integer versionNo,
                                                Integer isCurrent,
                                                String assignmentStatus,
                                                String adjustReason,
                                                Long operatorId,
                                                LocalDateTime now) {
        BizAssignment assignment = new BizAssignment();
        assignment.setPlanId(planId);
        assignment.setApplicationId(applicationId);
        assignment.setStudentId(studentId);
        assignment.setBaseId(baseId);
        assignment.setInnerTeacherId(innerTeacherId);
        assignment.setBaseTeacherId(baseTeacherId);
        assignment.setVersionNo(versionNo);
        assignment.setIsCurrent(isCurrent);
        assignment.setAssignmentStatus(assignmentStatus);
        assignment.setAdjustReason(trimToNull(adjustReason));
        assignment.setAssignedBy(operatorId);
        assignment.setAssignedTime(now);
        assignment.setCreatedBy(operatorId);
        assignment.setUpdatedBy(operatorId);
        assignment.setDeleted(0L);
        return assignment;
    }

    private String normalizeImportOperationType(String rawValue) {
        String trimmed = trimToNull(rawValue);
        if (!StringUtils.hasText(trimmed)) {
            return null;
        }
        if ("分配".equals(trimmed)) {
            return IMPORT_OP_ASSIGN;
        }
        if ("调整".equals(trimmed)) {
            return IMPORT_OP_ADJUST;
        }
        if ("自动".equals(trimmed)) {
            return IMPORT_OP_AUTO;
        }
        return normalizeCode(trimmed);
    }

    private AssignmentPageVO buildAssignmentPageVO(long page, long size, Page<BizAssignment> pageResult) {
        List<BizAssignment> records = pageResult.getRecords();
        Set<Long> planIds = records.stream().map(BizAssignment::getPlanId).collect(Collectors.toSet());
        Set<Long> userIds = new HashSet<>();
        userIds.addAll(records.stream().map(BizAssignment::getStudentId).collect(Collectors.toSet()));
        userIds.addAll(records.stream().map(BizAssignment::getInnerTeacherId).collect(Collectors.toSet()));
        userIds.addAll(records.stream().map(BizAssignment::getBaseTeacherId).collect(Collectors.toSet()));
        userIds.addAll(records.stream().map(BizAssignment::getAssignedBy).collect(Collectors.toSet()));
        Set<Long> baseIds = records.stream().map(BizAssignment::getBaseId).collect(Collectors.toSet());

        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);
        Map<Long, SysUser> userMap = queryUserMap(userIds);
        Map<Long, BaseInternshipBase> baseMap = queryBaseMap(baseIds);
        Map<Long, BaseDepartment> deptMap = queryDepartmentMap(extractDeptIds(userMap.values()));
        Map<Long, BaseMajor> majorMap = queryMajorMap(extractMajorIds(userMap.values()));
        Map<Long, BaseGrade> gradeMap = queryGradeMap(extractGradeIds(userMap.values()));

        List<AssignmentItemVO> voRecords = records.stream()
                .map(item -> toAssignmentItemVO(item, planMap, userMap, baseMap, deptMap, majorMap, gradeMap))
                .collect(Collectors.toList());

        AssignmentPageVO result = new AssignmentPageVO();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(pageResult.getTotal());
        result.setRecords(voRecords);
        return result;
    }

    public AssignmentItemVO getAssignmentDetail(Long assignmentId, Long userId, String roleCode) {
        SysUser admin = requireAdminOperator(userId, roleCode);
        BizAssignment assignment = requireAssignment(assignmentId);
        BizInternshipPlan plan = requirePlan(assignment.getPlanId());
        ensureAdminCanAccessPlan(plan, admin, roleCode);
        return buildAssignmentItem(assignment);
    }

    private AssignmentItemVO buildAssignmentItem(BizAssignment assignment) {
        Map<Long, BizInternshipPlan> planMap = queryPlanMap(Collections.singleton(assignment.getPlanId()));
        Set<Long> userIds = new HashSet<>();
        userIds.add(assignment.getStudentId());
        userIds.add(assignment.getInnerTeacherId());
        userIds.add(assignment.getBaseTeacherId());
        userIds.add(assignment.getAssignedBy());
        Map<Long, SysUser> userMap = queryUserMap(userIds);
        Map<Long, BaseInternshipBase> baseMap = queryBaseMap(Collections.singleton(assignment.getBaseId()));
        Map<Long, BaseDepartment> deptMap = queryDepartmentMap(extractDeptIds(userMap.values()));
        Map<Long, BaseMajor> majorMap = queryMajorMap(extractMajorIds(userMap.values()));
        Map<Long, BaseGrade> gradeMap = queryGradeMap(extractGradeIds(userMap.values()));
        return toAssignmentItemVO(assignment, planMap, userMap, baseMap, deptMap, majorMap, gradeMap);
    }

    private List<Long> normalizePreferredBaseIds(List<Long> preferredBaseIds, BizInternshipPlan plan) {
        if (CollectionUtils.isEmpty(preferredBaseIds)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "志愿基地不能为空");
        }
        if (preferredBaseIds.size() > 3) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "志愿基地最多选择 3 个");
        }

        List<BizPlanBase> enabledPlanBases = queryEnabledPlanBaseEntities(plan.getId());
        if (CollectionUtils.isEmpty(enabledPlanBases)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "当前计划没有可选的启用实习基地");
        }
        Set<Long> allowedBaseIds = enabledPlanBases.stream()
                .map(BizPlanBase::getBaseId)
                .collect(Collectors.toSet());

        List<Long> normalized = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Long baseId : preferredBaseIds) {
            if (baseId == null) {
                continue;
            }
            if (!seen.add(baseId)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "志愿基地不能重复选择");
            }
            if (!allowedBaseIds.contains(baseId)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "所选志愿基地未配置到当前计划");
            }
            normalized.add(baseId);
        }
        if (normalized.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "至少选择一个志愿基地");
        }
        return normalized;
    }

    private void replaceApplicationPreferences(Long applicationId, List<Long> preferredBaseIds, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        applicationPreferenceMapper.update(null, new LambdaUpdateWrapper<BizApplicationPreference>()
                .eq(BizApplicationPreference::getApplicationId, applicationId)
                .eq(BizApplicationPreference::getDeleted, 0L)
                .set(BizApplicationPreference::getDeleted, System.currentTimeMillis())
                .set(BizApplicationPreference::getUpdatedBy, operatorId)
                .set(BizApplicationPreference::getUpdatedTime, now));

        int order = 1;
        for (Long baseId : preferredBaseIds) {
            BizApplicationPreference entity = new BizApplicationPreference();
            entity.setApplicationId(applicationId);
            entity.setBaseId(baseId);
            entity.setPreferenceOrder(order++);
            entity.setCreatedBy(operatorId);
            entity.setUpdatedBy(operatorId);
            entity.setDeleted(0L);
            applicationPreferenceMapper.insert(entity);
        }
    }

    private Map<Long, List<ApplicationPreferenceVO>> queryPreferenceVOMapByApplicationIds(Set<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return new HashMap<>();
        }
        List<BizApplicationPreference> preferences = applicationPreferenceMapper.selectList(new LambdaQueryWrapper<BizApplicationPreference>()
                .in(BizApplicationPreference::getApplicationId, applicationIds)
                .eq(BizApplicationPreference::getDeleted, 0L)
                .orderByAsc(BizApplicationPreference::getApplicationId)
                .orderByAsc(BizApplicationPreference::getPreferenceOrder)
                .orderByAsc(BizApplicationPreference::getId));
        if (CollectionUtils.isEmpty(preferences)) {
            return new HashMap<>();
        }

        Map<Long, BaseInternshipBase> baseMap = queryBaseMap(preferences.stream()
                .map(BizApplicationPreference::getBaseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        Map<Long, List<ApplicationPreferenceVO>> result = new HashMap<>();
        for (BizApplicationPreference preference : preferences) {
            result.computeIfAbsent(preference.getApplicationId(), key -> new ArrayList<>())
                    .add(toApplicationPreferenceVO(preference, baseMap.get(preference.getBaseId())));
        }
        return result;
    }

    private Map<Long, List<PlanBaseVO>> queryPlanBaseVOMapByPlanIds(Set<Long> planIds, boolean enabledOnly) {
        if (CollectionUtils.isEmpty(planIds)) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<BizPlanBase> wrapper = new LambdaQueryWrapper<BizPlanBase>()
                .in(BizPlanBase::getPlanId, planIds)
                .eq(BizPlanBase::getDeleted, 0L)
                .orderByAsc(BizPlanBase::getPlanId)
                .orderByAsc(BizPlanBase::getSortNo)
                .orderByAsc(BizPlanBase::getId);
        if (enabledOnly) {
            wrapper.eq(BizPlanBase::getStatus, STATUS_ENABLED);
        }
        List<BizPlanBase> planBases = planBaseMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(planBases)) {
            return new HashMap<>();
        }

        Map<Long, BaseInternshipBase> baseMap = queryBaseMap(planBases.stream()
                .map(BizPlanBase::getBaseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Long, Map<Long, Integer>> remainingQuotaMap = queryRemainingQuotaMapByPlanIds(planIds);

        Map<Long, List<PlanBaseVO>> result = new HashMap<>();
        for (BizPlanBase planBase : planBases) {
            BaseInternshipBase base = baseMap.get(planBase.getBaseId());
            if (enabledOnly && (base == null || !STATUS_ENABLED.equals(normalizeCode(base.getStatus())))) {
                continue;
            }
            Integer remainingQuota = null;
            Map<Long, Integer> planRemainingQuotaMap = remainingQuotaMap.get(planBase.getPlanId());
            if (planRemainingQuotaMap != null) {
                remainingQuota = planRemainingQuotaMap.get(planBase.getBaseId());
            }
            result.computeIfAbsent(planBase.getPlanId(), key -> new ArrayList<>())
                    .add(toPlanBaseVO(planBase, base, remainingQuota));
        }
        return result;
    }

    private List<BizPlanBase> queryEnabledPlanBaseEntities(Long planId) {
        if (planId == null) {
            return new ArrayList<>();
        }
        return planBaseMapper.selectList(new LambdaQueryWrapper<BizPlanBase>()
                .eq(BizPlanBase::getPlanId, planId)
                .eq(BizPlanBase::getDeleted, 0L)
                .eq(BizPlanBase::getStatus, STATUS_ENABLED)
                .orderByAsc(BizPlanBase::getSortNo)
                .orderByAsc(BizPlanBase::getId));
    }

    private void ensurePlanBaseAssignable(Long planId, Long baseId, Long currentAssignmentId) {
        if (planId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "计划标识不能为空");
        }
        BaseInternshipBase base = requireEnabledBase(baseId);
        BizPlanBase planBase = planBaseMapper.selectOne(new LambdaQueryWrapper<BizPlanBase>()
                .eq(BizPlanBase::getPlanId, planId)
                .eq(BizPlanBase::getBaseId, base.getId())
                .eq(BizPlanBase::getDeleted, 0L)
                .eq(BizPlanBase::getStatus, STATUS_ENABLED)
                .last("LIMIT 1"));
        if (planBase == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "所选实习基地在当前计划中未启用");
        }
        if (planBase.getBaseQuota() == null || planBase.getBaseQuota() <= 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "所选实习基地当前没有可用名额");
        }

        LambdaQueryWrapper<BizAssignment> wrapper = new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getPlanId, planId)
                .eq(BizAssignment::getBaseId, base.getId())
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L);
        if (currentAssignmentId != null) {
            wrapper.ne(BizAssignment::getId, currentAssignmentId);
        }
        long currentCount = assignmentMapper.selectCount(wrapper);
        if (currentCount >= planBase.getBaseQuota()) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "所选实习基地名额已满");
        }
    }

    private ApplicationPreferenceVO toApplicationPreferenceVO(BizApplicationPreference preference, BaseInternshipBase base) {
        ApplicationPreferenceVO vo = new ApplicationPreferenceVO();
        vo.setBaseId(preference.getBaseId());
        vo.setBaseCode(base == null ? null : base.getBaseCode());
        vo.setBaseName(base == null ? null : base.getBaseName());
        vo.setPreferenceOrder(preference.getPreferenceOrder());
        return vo;
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

    private StudentApplicationItemVO toStudentApplicationItemVO(BizStudentApplication entity,
                                                                BizInternshipPlan plan,
                                                                SysUser reviewer,
                                                                List<ApplicationPreferenceVO> preferredBases,
                                                                boolean hasCurrentAssignment) {
        StudentApplicationItemVO vo = new StudentApplicationItemVO();
        vo.setId(entity.getId());
        vo.setPlanId(entity.getPlanId());
        vo.setPlanCode(plan == null ? null : plan.getPlanCode());
        vo.setPlanName(plan == null ? null : plan.getPlanName());
        vo.setApplyDeadline(plan == null ? null : plan.getApplyDeadline());
        vo.setPersonalStatement(entity.getPersonalStatement());
        vo.setPreferredBases(preferredBases == null ? new ArrayList<>() : preferredBases);
        vo.setApplicationStatus(entity.getApplicationStatus());
        vo.setReviewComment(entity.getReviewComment());
        vo.setReviewedBy(entity.getReviewedBy());
        vo.setReviewedByName(reviewer == null ? null : reviewer.getRealName());
        vo.setReviewedTime(entity.getReviewedTime());
        vo.setSubmittedTime(entity.getSubmittedTime());
        boolean editable = STATUS_SUBMITTED.equals(entity.getApplicationStatus()) && isPlanAcceptingApplication(plan);
        vo.setCanEdit(editable);
        vo.setCanWithdraw(editable && !hasCurrentAssignment);
        return vo;
    }

    private AdminApplicationItemVO toAdminApplicationItemVO(BizStudentApplication entity,
                                                            BizInternshipPlan plan,
                                                            SysUser student,
                                                            SysUser reviewer,
                                                            BizAssignment currentAssignment,
                                                            List<ApplicationPreferenceVO> preferredBases,
                                                            Map<Long, BaseDepartment> deptMap,
                                                            Map<Long, BaseMajor> majorMap,
                                                            Map<Long, BaseGrade> gradeMap) {
        AdminApplicationItemVO vo = new AdminApplicationItemVO();
        vo.setId(entity.getId());
        vo.setPlanId(entity.getPlanId());
        vo.setPlanCode(plan == null ? null : plan.getPlanCode());
        vo.setPlanName(plan == null ? null : plan.getPlanName());
        vo.setPlanStatus(plan == null ? null : plan.getPlanStatus());
        vo.setStudentId(entity.getStudentId());
        vo.setStudentNo(student == null ? null : student.getStudentNo());
        vo.setStudentName(student == null ? null : student.getRealName());
        vo.setStudentDeptId(student == null ? null : student.getDeptId());
        vo.setStudentDeptName(resolveDeptName(student == null ? null : student.getDeptId(), deptMap));
        vo.setStudentMajorName(resolveMajorName(student == null ? null : student.getMajorId(), majorMap));
        vo.setStudentGradeName(resolveGradeName(student == null ? null : student.getGradeId(), gradeMap));
        vo.setPersonalStatement(entity.getPersonalStatement());
        vo.setPreferredBases(preferredBases == null ? new ArrayList<>() : preferredBases);
        vo.setApplicationStatus(entity.getApplicationStatus());
        vo.setReviewComment(entity.getReviewComment());
        vo.setReviewedBy(entity.getReviewedBy());
        vo.setReviewedByName(reviewer == null ? null : reviewer.getRealName());
        vo.setReviewedTime(entity.getReviewedTime());
        vo.setSubmittedTime(entity.getSubmittedTime());
        vo.setCurrentAssignmentId(currentAssignment == null ? null : currentAssignment.getId());
        vo.setCurrentAssignmentVersionNo(currentAssignment == null ? null : currentAssignment.getVersionNo());
        return vo;
    }

    private AssignmentCandidateItemVO toAssignmentCandidateItemVO(BizStudentApplication application,
                                                                  BizInternshipPlan plan,
                                                                  SysUser student,
                                                                  List<ApplicationPreferenceVO> preferredBases,
                                                                  Map<Long, BaseDepartment> deptMap,
                                                                  Map<Long, BaseMajor> majorMap,
                                                                  Map<Long, BaseGrade> gradeMap) {
        AssignmentCandidateItemVO vo = new AssignmentCandidateItemVO();
        vo.setApplicationId(application.getId());
        vo.setPlanId(application.getPlanId());
        vo.setPlanCode(plan == null ? null : plan.getPlanCode());
        vo.setPlanName(plan == null ? null : plan.getPlanName());
        vo.setPlanStatus(plan == null ? null : plan.getPlanStatus());
        vo.setStudentId(application.getStudentId());
        vo.setStudentNo(student == null ? null : student.getStudentNo());
        vo.setStudentName(student == null ? null : student.getRealName());
        vo.setStudentDeptName(resolveDeptName(student == null ? null : student.getDeptId(), deptMap));
        vo.setStudentMajorName(resolveMajorName(student == null ? null : student.getMajorId(), majorMap));
        vo.setStudentGradeName(resolveGradeName(student == null ? null : student.getGradeId(), gradeMap));
        vo.setPreferredBases(preferredBases == null ? new ArrayList<>() : preferredBases);
        vo.setSubmittedTime(application.getSubmittedTime());
        return vo;
    }

    private AssignmentItemVO toAssignmentItemVO(BizAssignment assignment,
                                                Map<Long, BizInternshipPlan> planMap,
                                                Map<Long, SysUser> userMap,
                                                Map<Long, BaseInternshipBase> baseMap,
                                                Map<Long, BaseDepartment> deptMap,
                                                Map<Long, BaseMajor> majorMap,
                                                Map<Long, BaseGrade> gradeMap) {
        BizInternshipPlan plan = planMap.get(assignment.getPlanId());
        SysUser student = userMap.get(assignment.getStudentId());
        SysUser innerTeacher = userMap.get(assignment.getInnerTeacherId());
        SysUser baseTeacher = userMap.get(assignment.getBaseTeacherId());
        SysUser assignedBy = userMap.get(assignment.getAssignedBy());
        BaseInternshipBase base = baseMap.get(assignment.getBaseId());

        AssignmentItemVO vo = new AssignmentItemVO();
        vo.setId(assignment.getId());
        vo.setPlanId(assignment.getPlanId());
        vo.setPlanCode(plan == null ? null : plan.getPlanCode());
        vo.setPlanName(plan == null ? null : plan.getPlanName());
        vo.setPlanStatus(plan == null ? null : plan.getPlanStatus());
        vo.setApplicationId(assignment.getApplicationId());
        vo.setStudentId(assignment.getStudentId());
        vo.setStudentNo(student == null ? null : student.getStudentNo());
        vo.setStudentName(student == null ? null : student.getRealName());
        vo.setStudentDeptName(resolveDeptName(student == null ? null : student.getDeptId(), deptMap));
        vo.setStudentMajorName(resolveMajorName(student == null ? null : student.getMajorId(), majorMap));
        vo.setStudentGradeName(resolveGradeName(student == null ? null : student.getGradeId(), gradeMap));
        vo.setBaseId(assignment.getBaseId());
        vo.setBaseCode(base == null ? null : base.getBaseCode());
        vo.setBaseName(base == null ? null : base.getBaseName());
        vo.setInnerTeacherId(assignment.getInnerTeacherId());
        vo.setInnerTeacherNo(innerTeacher == null ? null : innerTeacher.getTeacherNo());
        vo.setInnerTeacherName(innerTeacher == null ? null : innerTeacher.getRealName());
        vo.setBaseTeacherId(assignment.getBaseTeacherId());
        vo.setBaseTeacherNo(baseTeacher == null ? null : baseTeacher.getTeacherNo());
        vo.setBaseTeacherName(baseTeacher == null ? null : baseTeacher.getRealName());
        vo.setVersionNo(assignment.getVersionNo());
        vo.setIsCurrent(assignment.getIsCurrent());
        vo.setAssignmentStatus(assignment.getAssignmentStatus());
        vo.setAdjustReason(assignment.getAdjustReason());
        vo.setAssignedBy(assignment.getAssignedBy());
        vo.setAssignedByName(assignedBy == null ? null : assignedBy.getRealName());
        vo.setAssignedTime(assignment.getAssignedTime());
        return vo;
    }

    private StudentAvailablePlanVO toStudentAvailablePlanVO(BizInternshipPlan plan,
                                                            List<PlanBaseVO> planBases,
                                                            BizStudentApplication application) {
        StudentAvailablePlanVO vo = new StudentAvailablePlanVO();
        vo.setId(plan.getId());
        if (application != null) {
            vo.setApplicationId(application.getId());
            vo.setApplicationStatus(application.getApplicationStatus());
        }
        vo.setPlanCode(plan.getPlanCode());
        vo.setPlanName(plan.getPlanName());
        vo.setAcademicYear(plan.getAcademicYear());
        vo.setTerm(plan.getTerm());
        vo.setApplyDeadline(plan.getApplyDeadline());
        vo.setPlanBases(planBases == null ? new ArrayList<>() : planBases);
        return vo;
    }

    private void applyStudentApplicationForm(BizStudentApplication entity, StudentApplicationSaveRequest request) {
        entity.setIntentionRegion(null);
        entity.setSchoolType(null);
        entity.setPersonalStatement(trimToNull(request.getPersonalStatement()));
    }

    private boolean isPlanAcceptingApplication(BizInternshipPlan plan) {
        return plan != null
                && STATUS_PUBLISHED.equals(plan.getPlanStatus())
                && plan.getApplyDeadline() != null
                && !LocalDateTime.now().isAfter(plan.getApplyDeadline());
    }

    private boolean isPlanAssignableStatus(BizInternshipPlan plan) {
        return plan != null && isPlanAssignableStatus(plan.getPlanStatus());
    }

    private boolean isPlanAssignableStatus(String planStatus) {
        String status = normalizeCode(planStatus);
        return STATUS_PUBLISHED.equals(status) || STATUS_FINISHED.equals(status);
    }

    private void ensurePlanCanApply(BizInternshipPlan plan) {
        if (!STATUS_PUBLISHED.equals(plan.getPlanStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有已发布计划才可以申请");
        }
        if (plan.getApplyDeadline() == null || LocalDateTime.now().isAfter(plan.getApplyDeadline())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "申请截止时间已过");
        }
    }

    private void ensurePlanAssignable(BizInternshipPlan plan) {
        if (!STATUS_PUBLISHED.equals(normalizeCode(plan.getPlanStatus()))) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "\u8BA1\u5212\u5DF2\u7ED3\u675F\uFF0C\u4E0D\u80FD\u518D\u5206\u914D\u6216\u8C03\u6574");
        }
    }

    private void ensureStudentInPlanDept(SysUser student, BizInternshipPlan plan) {
        if (!Objects.equals(student.getDeptId(), plan.getDeptId())) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "学生所属院系与计划院系不一致");
        }
    }

    private void ensureStudentCanApplyPlan(SysUser student, BizInternshipPlan plan) {
        ensureStudentInPlanDept(student, plan);
        ensurePlanCanApply(plan);
    }

    private SysUser requireStudentOperator(Long userId, String roleCode) {
        if (!ROLE_STUDENT.equals(normalizeCode(roleCode))) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色不是学生");
        }
        return requireUser(userId);
    }

    private SysUser requireAdminOperator(Long userId, String roleCode) {
        String normalizedRoleCode = normalizeCode(roleCode);
        if (!ROLE_SYS_ADMIN.equals(normalizedRoleCode) && !ROLE_DEPT_ADMIN.equals(normalizedRoleCode)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色不是管理员");
        }
        return requireUser(userId);
    }

    private void ensureAdminCanAccessPlan(BizInternshipPlan plan, SysUser admin, String roleCode) {
        String normalizedRoleCode = normalizeCode(roleCode);
        if (ROLE_SYS_ADMIN.equals(normalizedRoleCode)) {
            return;
        }
        if (!ROLE_DEPT_ADMIN.equals(normalizedRoleCode)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权管理该计划");
        }
        if (!Objects.equals(plan.getDeptId(), admin.getDeptId())) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权管理其他院系的计划");
        }
    }

    private Set<Long> resolveAdminPlanScope(SysUser admin, String roleCode) {
        String normalizedRoleCode = normalizeCode(roleCode);
        if (ROLE_SYS_ADMIN.equals(normalizedRoleCode)) {
            return null;
        }
        if (!ROLE_DEPT_ADMIN.equals(normalizedRoleCode)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查询计划范围");
        }
        return queryPlanIdsByDept(admin.getDeptId());
    }

    private Set<Long> queryPlanIdsByDept(Long deptId) {
        if (deptId == null) {
            return new HashSet<>();
        }
        List<BizInternshipPlan> plans = planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getDeleted, 0L)
                .eq(BizInternshipPlan::getDeptId, deptId));
        return plans.stream().map(BizInternshipPlan::getId).collect(Collectors.toSet());
    }

    private BizInternshipPlan requirePlanByCode(String planCode) {
        BizInternshipPlan plan = planMapper.selectOne(new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getPlanCode, planCode)
                .eq(BizInternshipPlan::getDeleted, 0L)
                .last("LIMIT 1"));
        if (plan == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "未找到对应计划，计划编码：" + planCode);
        }
        return plan;
    }

    private SysUser findUserByStudentNo(String studentNo) {
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStudentNo, studentNo)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
    }

    private SysUser requireStudentByStudentNo(String studentNo) {
        SysUser user = findUserByStudentNo(studentNo);
        if (user == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "未找到对应学生，学号：" + studentNo);
        }
        if (!STATUS_ENABLED.equals(user.getStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该学生账号未启用，学号：" + studentNo);
        }
        if (!rbacService.userHasRoleCode(user.getId(), ROLE_STUDENT)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该账号不是学生身份，学号：" + studentNo);
        }
        return user;
    }

    private SysUser requireTeacherByNoAndRole(String teacherNo, String roleCode) {
        SysUser teacher = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTeacherNo, teacherNo)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        if (teacher == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "未找到对应教师，工号：" + teacherNo);
        }
        if (!STATUS_ENABLED.equals(teacher.getStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "教师账号未启用，工号：" + teacherNo);
        }
        if (!rbacService.userHasRoleCode(teacher.getId(), roleCode)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "教师角色不匹配，工号：" + teacherNo + "，角色：" + roleCode);
        }
        return teacher;
    }

    private SysUser requireTeacherWithRole(Long userId,
                                           String roleCode,
                                           BizInternshipPlan plan,
                                           boolean requireSameDept) {
        SysUser teacher = requireEnabledUser(userId);
        if (!rbacService.userHasRoleCode(teacher.getId(), roleCode)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "所选用户未分配角色：" + roleCode);
        }
        if (requireSameDept && !Objects.equals(teacher.getDeptId(), plan.getDeptId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "校内指导教师必须与计划所属院系一致");
        }
        return teacher;
    }

    private SysUser requireEnabledUser(Long userId) {
        SysUser user = requireUser(userId);
        if (!STATUS_ENABLED.equals(user.getStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "用户账号未启用");
        }
        return user;
    }

    private BaseInternshipBase requireEnabledBase(Long baseId) {
        if (baseId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "实习基地不能为空");
        }
        BaseInternshipBase base = internshipBaseMapper.selectOne(new LambdaQueryWrapper<BaseInternshipBase>()
                .eq(BaseInternshipBase::getId, baseId)
                .eq(BaseInternshipBase::getDeleted, 0L)
                .last("LIMIT 1"));
        if (base == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "实习基地不存在");
        }
        if (!STATUS_ENABLED.equals(base.getStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "实习基地未启用");
        }
        return base;
    }

    private BaseInternshipBase requireEnabledBaseByCode(String baseCode) {
        BaseInternshipBase base = internshipBaseMapper.selectOne(new LambdaQueryWrapper<BaseInternshipBase>()
                .eq(BaseInternshipBase::getBaseCode, baseCode)
                .eq(BaseInternshipBase::getDeleted, 0L)
                .last("LIMIT 1"));
        if (base == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "未找到对应实习基地，编码：" + baseCode);
        }
        if (!STATUS_ENABLED.equals(base.getStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该实习基地未启用，编码：" + baseCode);
        }
        return base;
    }

    private BizStudentApplication findApplicationByPlanAndStudent(Long planId, Long studentId) {
        return studentApplicationMapper.selectOne(new LambdaQueryWrapper<BizStudentApplication>()
                .eq(BizStudentApplication::getPlanId, planId)
                .eq(BizStudentApplication::getStudentId, studentId)
                .eq(BizStudentApplication::getDeleted, 0L)
                .last("LIMIT 1"));
    }

    private Map<Long, BizStudentApplication> queryApplicationMapByStudentAndPlanIds(Long studentId, Set<Long> planIds) {
        if (studentId == null || CollectionUtils.isEmpty(planIds)) {
            return new HashMap<>();
        }
        List<BizStudentApplication> applications = studentApplicationMapper.selectList(new LambdaQueryWrapper<BizStudentApplication>()
                .eq(BizStudentApplication::getStudentId, studentId)
                .eq(BizStudentApplication::getDeleted, 0L)
                .in(BizStudentApplication::getPlanId, planIds)
                .orderByDesc(BizStudentApplication::getSubmittedTime)
                .orderByDesc(BizStudentApplication::getId));
        Map<Long, BizStudentApplication> result = new HashMap<>();
        for (BizStudentApplication application : applications) {
            result.putIfAbsent(application.getPlanId(), application);
        }
        return result;
    }

    private BizAssignment findCurrentAssignmentByApplicationId(Long applicationId) {
        if (applicationId == null) {
            return null;
        }
        return assignmentMapper.selectOne(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getApplicationId, applicationId)
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L)
                .last("LIMIT 1"));
    }

    private BizAssignment findCurrentAssignmentByPlanAndStudent(Long planId, Long studentId) {
        return assignmentMapper.selectOne(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getPlanId, planId)
                .eq(BizAssignment::getStudentId, studentId)
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L)
                .last("LIMIT 1"));
    }

    private boolean hasCurrentAssignmentByApplicationId(Long applicationId) {
        return findCurrentAssignmentByApplicationId(applicationId) != null;
    }

    private int nextAssignmentVersionNo(Long planId, Long studentId) {
        BizAssignment latest = assignmentMapper.selectOne(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getPlanId, planId)
                .eq(BizAssignment::getStudentId, studentId)
                .eq(BizAssignment::getDeleted, 0L)
                .orderByDesc(BizAssignment::getVersionNo)
                .last("LIMIT 1"));
        if (latest == null || latest.getVersionNo() == null) {
            return 1;
        }
        return latest.getVersionNo() + 1;
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

    private Map<Long, BaseInternshipBase> queryBaseMap(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return internshipBaseMapper.selectList(new LambdaQueryWrapper<BaseInternshipBase>()
                        .in(BaseInternshipBase::getId, ids)
                        .eq(BaseInternshipBase::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BaseInternshipBase::getId, item -> item, (left, right) -> left));
    }

    private Map<Long, BaseDepartment> queryDepartmentMap(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return departmentMapper.selectList(new LambdaQueryWrapper<BaseDepartment>()
                        .in(BaseDepartment::getId, ids)
                        .eq(BaseDepartment::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BaseDepartment::getId, item -> item, (left, right) -> left));
    }

    private Map<Long, BaseMajor> queryMajorMap(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return majorMapper.selectList(new LambdaQueryWrapper<BaseMajor>()
                        .in(BaseMajor::getId, ids)
                        .eq(BaseMajor::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BaseMajor::getId, item -> item, (left, right) -> left));
    }

    private Map<Long, BaseGrade> queryGradeMap(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return gradeMapper.selectList(new LambdaQueryWrapper<BaseGrade>()
                        .in(BaseGrade::getId, ids)
                        .eq(BaseGrade::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BaseGrade::getId, item -> item, (left, right) -> left));
    }

    private Set<Long> extractDeptIds(Iterable<SysUser> users) {
        Set<Long> ids = new HashSet<>();
        if (users == null) {
            return ids;
        }
        for (SysUser user : users) {
            if (user != null && user.getDeptId() != null) {
                ids.add(user.getDeptId());
            }
        }
        return ids;
    }

    private Set<Long> extractMajorIds(Iterable<SysUser> users) {
        Set<Long> ids = new HashSet<>();
        if (users == null) {
            return ids;
        }
        for (SysUser user : users) {
            if (user != null && user.getMajorId() != null) {
                ids.add(user.getMajorId());
            }
        }
        return ids;
    }

    private Set<Long> extractGradeIds(Iterable<SysUser> users) {
        Set<Long> ids = new HashSet<>();
        if (users == null) {
            return ids;
        }
        for (SysUser user : users) {
            if (user != null && user.getGradeId() != null) {
                ids.add(user.getGradeId());
            }
        }
        return ids;
    }

    private String resolveDeptName(Long deptId, Map<Long, BaseDepartment> deptMap) {
        if (deptId == null) {
            return null;
        }
        BaseDepartment department = deptMap.get(deptId);
        return department == null ? null : department.getDeptName();
    }

    private String resolveMajorName(Long majorId, Map<Long, BaseMajor> majorMap) {
        if (majorId == null) {
            return null;
        }
        BaseMajor major = majorMap.get(majorId);
        return major == null ? null : major.getMajorName();
    }

    private String resolveGradeName(Long gradeId, Map<Long, BaseGrade> gradeMap) {
        if (gradeId == null) {
            return null;
        }
        BaseGrade grade = gradeMap.get(gradeId);
        return grade == null ? null : grade.getGradeName();
    }

    private Map<Long, BizAssignment> queryCurrentAssignmentMapByApplicationIds(Set<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return new HashMap<>();
        }
        return assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                        .in(BizAssignment::getApplicationId, applicationIds)
                        .eq(BizAssignment::getIsCurrent, 1)
                        .eq(BizAssignment::getDeleted, 0L))
                .stream()
                .collect(Collectors.toMap(BizAssignment::getApplicationId, item -> item, (left, right) -> left));
    }

    private Map<Long, Map<Long, Integer>> queryRemainingQuotaMapByPlanIds(Set<Long> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return new HashMap<>();
        }
        List<BizPlanBase> planBases = planBaseMapper.selectList(new LambdaQueryWrapper<BizPlanBase>()
                .in(BizPlanBase::getPlanId, planIds)
                .eq(BizPlanBase::getDeleted, 0L)
                .eq(BizPlanBase::getStatus, STATUS_ENABLED)
                .orderByAsc(BizPlanBase::getPlanId)
                .orderByAsc(BizPlanBase::getSortNo)
                .orderByAsc(BizPlanBase::getId));
        if (CollectionUtils.isEmpty(planBases)) {
            return new HashMap<>();
        }

        Set<Long> baseIds = planBases.stream()
                .map(BizPlanBase::getBaseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, BaseInternshipBase> baseMap = queryBaseMap(baseIds);
        Map<Long, Map<Long, Long>> currentCountMap = assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                        .in(BizAssignment::getPlanId, planIds)
                        .eq(BizAssignment::getIsCurrent, 1)
                        .eq(BizAssignment::getDeleted, 0L))
                .stream()
                .filter(item -> item.getPlanId() != null && item.getBaseId() != null)
                .collect(Collectors.groupingBy(
                        BizAssignment::getPlanId,
                        Collectors.groupingBy(BizAssignment::getBaseId, Collectors.counting())
                ));

        Map<Long, Map<Long, Integer>> result = new HashMap<>();
        for (BizPlanBase planBase : planBases) {
            BaseInternshipBase base = baseMap.get(planBase.getBaseId());
            if (base == null || !STATUS_ENABLED.equals(normalizeCode(base.getStatus()))) {
                continue;
            }
            Map<Long, Integer> planRemainingQuotaMap = result.computeIfAbsent(planBase.getPlanId(), key -> new HashMap<>());
            Map<Long, Long> planCountMap = currentCountMap.get(planBase.getPlanId());
            long used = planCountMap == null ? 0L : planCountMap.getOrDefault(planBase.getBaseId(), 0L);
            int quota = planBase.getBaseQuota() == null ? 0 : planBase.getBaseQuota();
            planRemainingQuotaMap.put(planBase.getBaseId(), Math.max(quota - (int) used, 0));
        }
        return result;
    }

    private Set<Long> findPlanIdsByKeyword(String keyword, Set<Long> scopePlanIds) {
        LambdaQueryWrapper<BizInternshipPlan> wrapper = new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getDeleted, 0L)
                .and(w -> w.like(BizInternshipPlan::getPlanCode, keyword)
                        .or().like(BizInternshipPlan::getPlanName, keyword)
                        .or().like(BizInternshipPlan::getAcademicYear, keyword)
                        .or().like(BizInternshipPlan::getTerm, keyword));
        if (scopePlanIds != null) {
            if (scopePlanIds.isEmpty()) {
                return new HashSet<>();
            }
            wrapper.in(BizInternshipPlan::getId, scopePlanIds);
        }
        return planMapper.selectList(wrapper).stream()
                .map(BizInternshipPlan::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> findStudentIdsByNameKeyword(String keyword) {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0L)
                        .and(w -> w.like(SysUser::getRealName, keyword)
                                .or().like(SysUser::getStudentNo, keyword)
                                .or().like(SysUser::getLoginName, keyword)))
                .stream()
                .filter(user -> rbacService.userHasRoleCode(user.getId(), ROLE_STUDENT))
                .map(SysUser::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> findStudentIdsByKeyword(String keyword) {
        return findStudentIdsByNameKeyword(keyword);
    }

    private Set<Long> findTeacherIdsByKeyword(String keyword) {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0L)
                        .isNotNull(SysUser::getTeacherNo)
                        .and(w -> w.like(SysUser::getRealName, keyword)
                                .or().like(SysUser::getTeacherNo, keyword)
                                .or().like(SysUser::getLoginName, keyword)))
                .stream()
                .map(SysUser::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> findBaseIdsByKeyword(String keyword) {
        return internshipBaseMapper.selectList(new LambdaQueryWrapper<BaseInternshipBase>()
                        .eq(BaseInternshipBase::getDeleted, 0L)
                        .and(w -> w.like(BaseInternshipBase::getBaseCode, keyword)
                                .or().like(BaseInternshipBase::getBaseName, keyword)
                                .or().like(BaseInternshipBase::getCity, keyword)
                                .or().like(BaseInternshipBase::getDistrict, keyword)))
                .stream()
                .map(BaseInternshipBase::getId)
                .collect(Collectors.toSet());
    }

    private boolean isImportRowEmpty(Row row, DataFormatter formatter) {
        for (int i = 0; i <= 6; i++) {
            if (StringUtils.hasText(cellText(row, i, formatter))) {
                return false;
            }
        }
        return true;
    }

    private String cellText(Row row, int cellIndex, DataFormatter formatter) {
        if (row.getCell(cellIndex) == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(cellIndex)).trim();
    }

    private String rootMessage(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }
        if (cursor instanceof BusinessException) {
            return cursor.getMessage();
        }
        String message = cursor.getMessage();
        if (!StringUtils.hasText(message) && throwable != null) {
            message = throwable.getMessage();
        }
        return StringUtils.hasText(message) ? message : "Unknown error";
    }

    private List<AssignmentCandidateItemVO> slice(List<AssignmentCandidateItemVO> source, long page, long size) {
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

    private AssignmentCandidatePageVO emptyAssignmentCandidatePage(long page, long size) {
        AssignmentCandidatePageVO result = new AssignmentCandidatePageVO();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(0L);
        return result;
    }

    private String buildDisplayName(String name, String code) {
        if (!StringUtils.hasText(code)) {
            return name;
        }
        if (!StringUtils.hasText(name)) {
            return code;
        }
        return name + " (" + code + ")";
    }

    private StudentApplicationPageVO emptyStudentApplicationPage(long page, long size) {
        StudentApplicationPageVO result = new StudentApplicationPageVO();
        result.setPage(normalizePage(page));
        result.setSize(normalizeSize(size));
        result.setTotal(0L);
        return result;
    }

    private AdminApplicationPageVO emptyAdminApplicationPage(long page, long size) {
        AdminApplicationPageVO result = new AdminApplicationPageVO();
        result.setPage(normalizePage(page));
        result.setSize(normalizeSize(size));
        result.setTotal(0L);
        return result;
    }

    private AssignmentPageVO emptyAssignmentPage(long page, long size) {
        AssignmentPageVO result = new AssignmentPageVO();
        result.setPage(normalizePage(page));
        result.setSize(normalizeSize(size));
        result.setTotal(0L);
        return result;
    }

    private long normalizePage(long page) {
        return page <= 0 ? 1 : page;
    }

    private long normalizeSize(long size) {
        return size <= 0 ? 10 : size;
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

    private SysUser requireUserOrNull(Long userId) {
        if (userId == null) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
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

    private BizStudentApplication requireApplication(Long applicationId) {
        if (applicationId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "申请标识不能为空");
        }
        BizStudentApplication entity = studentApplicationMapper.selectOne(new LambdaQueryWrapper<BizStudentApplication>()
                .eq(BizStudentApplication::getId, applicationId)
                .eq(BizStudentApplication::getDeleted, 0L)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "申请记录不存在");
        }
        return entity;
    }

    private BizAssignment requireAssignment(Long assignmentId) {
        if (assignmentId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "分配标识不能为空");
        }
        BizAssignment entity = assignmentMapper.selectOne(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getId, assignmentId)
                .eq(BizAssignment::getDeleted, 0L)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "分配记录不存在");
        }
        return entity;
    }
}
