package com.teacher.internship.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.base.entity.BaseDepartment;
import com.teacher.internship.modules.base.entity.BaseGrade;
import com.teacher.internship.modules.base.entity.BaseMajor;
import com.teacher.internship.modules.base.mapper.BaseDepartmentMapper;
import com.teacher.internship.modules.base.mapper.BaseGradeMapper;
import com.teacher.internship.modules.base.mapper.BaseMajorMapper;
import com.teacher.internship.modules.application.entity.BizAssignment;
import com.teacher.internship.modules.application.entity.BizStudentApplication;
import com.teacher.internship.modules.application.mapper.BizAssignmentMapper;
import com.teacher.internship.modules.application.mapper.BizStudentApplicationMapper;
import com.teacher.internship.modules.evaluation.entity.BizEvaluation;
import com.teacher.internship.modules.evaluation.mapper.BizEvaluationMapper;
import com.teacher.internship.modules.material.entity.BizMaterial;
import com.teacher.internship.modules.material.entity.BizMaterialVersion;
import com.teacher.internship.modules.material.mapper.BizMaterialMapper;
import com.teacher.internship.modules.material.mapper.BizMaterialVersionMapper;
import com.teacher.internship.modules.notice.service.NoticeTriggerService;
import com.teacher.internship.modules.score.entity.BizScoreSheet;
import com.teacher.internship.modules.score.mapper.BizScoreSheetMapper;
import com.teacher.internship.modules.system.dto.CreateUserRequest;
import com.teacher.internship.modules.system.dto.ResetPasswordRequest;
import com.teacher.internship.modules.system.dto.StudentRegisterRequest;
import com.teacher.internship.modules.system.dto.UpdatePasswordRequest;
import com.teacher.internship.modules.system.dto.UpdateProfileRequest;
import com.teacher.internship.modules.system.entity.SysRole;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.entity.SysUserRole;
import com.teacher.internship.modules.system.mapper.SysLoginLogMapper;
import com.teacher.internship.modules.system.mapper.SysOperationLogMapper;
import com.teacher.internship.modules.system.mapper.SysRoleMapper;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import com.teacher.internship.modules.system.mapper.SysUserRoleMapper;
import com.teacher.internship.modules.system.vo.IdNameOptionVO;
import com.teacher.internship.modules.system.vo.UserImportErrorVO;
import com.teacher.internship.modules.system.vo.UserImportResultVO;
import com.teacher.internship.modules.system.vo.UserItemVO;
import com.teacher.internship.modules.system.vo.UserPageVO;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String DEFAULT_RESET_PASSWORD = "123456";
    private static final String STATUS_ENABLED = "ENABLED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String IDENTITY_TEACHER = "TEACHER";
    private static final String IDENTITY_ADMIN = "ADMIN";
    private static final String ROLE_INNER_TEACHER = "INNER_TEACHER";
    private static final String ROLE_BASE_TEACHER = "BASE_TEACHER";
    private static final String ROLE_DEPT_ADMIN = "DEPT_ADMIN";
    private static final String ROLE_ACADEMIC_ADMIN = "ACADEMIC_ADMIN";
    private static final String ROLE_SYS_ADMIN = "SYS_ADMIN";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final BaseDepartmentMapper departmentMapper;
    private final BaseMajorMapper majorMapper;
    private final BaseGradeMapper gradeMapper;
    private final BizStudentApplicationMapper studentApplicationMapper;
    private final BizAssignmentMapper assignmentMapper;
    private final BizMaterialMapper materialMapper;
    private final BizMaterialVersionMapper materialVersionMapper;
    private final BizEvaluationMapper evaluationMapper;
    private final BizScoreSheetMapper scoreSheetMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final SysOperationLogMapper operationLogMapper;
    private final TransactionTemplate transactionTemplate;
    private final PasswordEncoder passwordEncoder;
    private final NoticeTriggerService noticeTriggerService;

    public UserService(SysUserMapper sysUserMapper,
                       SysRoleMapper sysRoleMapper,
                       SysUserRoleMapper sysUserRoleMapper,
                       BaseDepartmentMapper departmentMapper,
                       BaseMajorMapper majorMapper,
                       BaseGradeMapper gradeMapper,
                       BizStudentApplicationMapper studentApplicationMapper,
                       BizAssignmentMapper assignmentMapper,
                       BizMaterialMapper materialMapper,
                       BizMaterialVersionMapper materialVersionMapper,
                       BizEvaluationMapper evaluationMapper,
                       BizScoreSheetMapper scoreSheetMapper,
                       SysLoginLogMapper loginLogMapper,
                       SysOperationLogMapper operationLogMapper,
                       TransactionTemplate transactionTemplate,
                       PasswordEncoder passwordEncoder,
                       NoticeTriggerService noticeTriggerService) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.departmentMapper = departmentMapper;
        this.majorMapper = majorMapper;
        this.gradeMapper = gradeMapper;
        this.studentApplicationMapper = studentApplicationMapper;
        this.assignmentMapper = assignmentMapper;
        this.materialMapper = materialMapper;
        this.materialVersionMapper = materialVersionMapper;
        this.evaluationMapper = evaluationMapper;
        this.scoreSheetMapper = scoreSheetMapper;
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
        this.transactionTemplate = transactionTemplate;
        this.passwordEncoder = passwordEncoder;
        this.noticeTriggerService = noticeTriggerService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void registerStudent(StudentRegisterRequest request) {
        if (!Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "两次输入的密码不一致");
        }
        ensureLoginNameUnique(request.getStudentNo(), null);
        ensureStudentNoUnique(request.getStudentNo(), null);
        BaseDepartment department = requireEnabledDepartment(request.getDeptId());
        BaseMajor major = requireEnabledMajor(request.getMajorId());
        BaseGrade grade = requireEnabledGrade(request.getGradeId());
        validateDepartmentMajorRelation(department, major);

        SysUser user = new SysUser();
        user.setLoginName(request.getStudentNo());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setIdentityType("STUDENT");
        user.setStudentNo(request.getStudentNo());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDeptId(department.getId());
        user.setMajorId(major.getId());
        user.setGradeId(grade.getId());
        user.setStatus("PENDING");
        user.setMustChangePassword(0);
        user.setCreatedBy(0L);
        user.setUpdatedBy(0L);
        user.setDeleted(0L);
        sysUserMapper.insert(user);

        SysRole studentRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, "STUDENT")
                .eq(SysRole::getDeleted, 0L)
                .eq(SysRole::getStatus, "ENABLED")
                .last("LIMIT 1"));
        if (studentRole == null) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "学生角色不存在或未启用");
        }

        SysUserRole relation = new SysUserRole();
        relation.setUserId(user.getId());
        relation.setRoleId(studentRole.getId());
        relation.setCreatedBy(0L);
        relation.setUpdatedBy(0L);
        relation.setDeleted(0L);
        sysUserRoleMapper.insert(relation);

        noticeTriggerService.notifyStudentRegisterSubmitted(user, department, major, grade);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        SysUser user = requireUser(userId);
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUpdatedBy(userId);
        user.setUpdatedTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "两次输入的新密码不一致");
        }

        SysUser user = requireUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "当前密码不正确");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "新密码不能与当前密码相同");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(0);
        user.setUpdatedBy(userId);
        user.setUpdatedTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long operatorId, Long userId, ResetPasswordRequest request) {
        SysUser user = requireUser(userId);
        String newPassword = request == null || !StringUtils.hasText(request.getNewPassword())
                ? DEFAULT_RESET_PASSWORD
                : request.getNewPassword();

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(1);
        user.setUpdatedBy(operatorId);
        user.setUpdatedTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    public UserPageVO queryUsers(long page, long size, String keyword, String status) {
        long safePage = page <= 0 ? 1 : page;
        long safeSize = size <= 0 ? 10 : size;

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0L)
                .orderByDesc(SysUser::getCreatedTime);

        if (StringUtils.hasText(status)) {
            wrapper.eq(SysUser::getStatus, status.trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getStudentNo, keyword)
                    .or().like(SysUser::getTeacherNo, keyword));
        }

        Page<SysUser> pageResult = sysUserMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        Map<Long, String> roleNameMapByUserId = buildRoleNameMapByUserId(pageResult.getRecords());
        Map<Long, List<String>> roleCodeMapByUserId = buildRoleCodeMapByUserId(pageResult.getRecords());
        Map<Long, String> deptNameMap = buildDepartmentNameMap(pageResult.getRecords());
        Map<Long, String> majorNameMap = buildMajorNameMap(pageResult.getRecords());
        Map<Long, String> gradeNameMap = buildGradeNameMap(pageResult.getRecords());
        UserPageVO vo = new UserPageVO();
        vo.setPage(safePage);
        vo.setSize(safeSize);
        vo.setTotal(pageResult.getTotal());
        vo.setRecords(pageResult.getRecords().stream()
                .map(user -> toUserItemVO(user, roleNameMapByUserId, roleCodeMapByUserId, deptNameMap, majorNameMap, gradeNameMap))
                .collect(Collectors.toList()));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long operatorId, Long userId, String status) {
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!"ENABLED".equals(normalizedStatus) && !"DISABLED".equals(normalizedStatus)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "用户状态仅支持 ENABLED 或 DISABLED");
        }
        SysUser user = requireUser(userId);
        user.setStatus(normalizedStatus);
        user.setUpdatedBy(operatorId);
        user.setUpdatedTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createUser(Long operatorId, CreateUserRequest request) {
        requireUser(operatorId);

        String normalizedIdentityType = normalizeIdentityType(request.getIdentityType());
        if (!ROLE_STUDENT.equals(normalizedIdentityType)
                && !IDENTITY_TEACHER.equals(normalizedIdentityType)
                && !IDENTITY_ADMIN.equals(normalizedIdentityType)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "身份类型仅支持师范生、教师或管理员");
        }

        String realName = trimToNull(request.getRealName());
        String studentNo = trimToNull(request.getStudentNo());
        String teacherNo = trimToNull(request.getTeacherNo());
        String phone = trimToNull(request.getPhone());
        String email = trimToNull(request.getEmail());

        if (CollectionUtils.isEmpty(request.getRoleCodes())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "请至少选择一个角色");
        }
        Map<String, SysRole> enabledRoleCodeMap = queryEnabledRoleCodeMap();
        Set<String> normalizedRoleCodes = request.getRoleCodes().stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeCode)
                .collect(Collectors.toCollection(HashSet::new));
        if (CollectionUtils.isEmpty(normalizedRoleCodes)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "请至少选择一个有效角色");
        }
        for (String normalizedCode : normalizedRoleCodes) {
            if (!enabledRoleCodeMap.containsKey(normalizedCode)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "角色不存在或未启用: " + normalizedCode);
            }
        }

        validateCreateUserRequest(normalizedIdentityType, normalizedRoleCodes, studentNo, teacherNo,
                request.getDeptId(), request.getMajorId(), request.getGradeId());

        Long effectiveDeptId = shouldPersistDepartment(normalizedIdentityType, normalizedRoleCodes) ? request.getDeptId() : null;
        Long effectiveMajorId = ROLE_STUDENT.equals(normalizedIdentityType) ? request.getMajorId() : null;
        Long effectiveGradeId = ROLE_STUDENT.equals(normalizedIdentityType) ? request.getGradeId() : null;

        BaseDepartment department = effectiveDeptId == null ? null : requireEnabledDepartment(effectiveDeptId);
        BaseMajor major = effectiveMajorId == null ? null : requireEnabledMajor(effectiveMajorId);
        BaseGrade grade = effectiveGradeId == null ? null : requireEnabledGrade(effectiveGradeId);
        validateDepartmentMajorRelation(department, major);

        String loginName = resolveLoginName(normalizedIdentityType, studentNo, teacherNo);
        ensureLoginNameUnique(loginName, null);
        if (StringUtils.hasText(studentNo)) {
            ensureStudentNoUnique(studentNo, null);
        }
        if (StringUtils.hasText(teacherNo)) {
            ensureTeacherNoUnique(teacherNo, null);
        }

        SysUser user = new SysUser();
        user.setLoginName(loginName);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(realName);
        user.setIdentityType(normalizedIdentityType);
        user.setStudentNo(ROLE_STUDENT.equals(normalizedIdentityType) ? studentNo : null);
        user.setTeacherNo(ROLE_STUDENT.equals(normalizedIdentityType) ? null : teacherNo);
        user.setPhone(phone);
        user.setEmail(email);
        user.setDeptId(department == null ? null : department.getId());
        user.setMajorId(major == null ? null : major.getId());
        user.setGradeId(grade == null ? null : grade.getId());
        user.setStatus("ENABLED");
        user.setMustChangePassword(1);
        user.setCreatedBy(operatorId);
        user.setUpdatedBy(operatorId);
        user.setDeleted(0L);
        sysUserMapper.insert(user);

        for (String normalizedCode : normalizedRoleCodes) {
            SysRole role = enabledRoleCodeMap.get(normalizedCode);
            SysUserRole relation = new SysUserRole();
            relation.setUserId(user.getId());
            relation.setRoleId(role.getId());
            relation.setCreatedBy(operatorId);
            relation.setUpdatedBy(operatorId);
            relation.setDeleted(0L);
            sysUserRoleMapper.insert(relation);
        }

        return user.getId();
    }

    private String resolveLoginName(String identityType, String studentNo, String teacherNo) {
        String normalizedIdentity = normalizeCode(identityType);
        if (ROLE_STUDENT.equals(normalizedIdentity)) {
            return studentNo;
        }
        if (IDENTITY_TEACHER.equals(normalizedIdentity) || IDENTITY_ADMIN.equals(normalizedIdentity)) {
            return teacherNo;
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long operatorId, Long userId) {
        if (operatorId == null || userId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "用户标识不能为空");
        }
        if (Objects.equals(operatorId, userId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "不能删除当前登录账号");
        }

        SysUser user = requireUser(userId);
        ensureUserDeletable(user);

        LocalDateTime now = LocalDateTime.now();
        List<SysUserRole> relations = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getDeleted, 0L));
        for (SysUserRole relation : relations) {
            relation.setDeleted(relation.getId());
            relation.setUpdatedBy(operatorId);
            relation.setUpdatedTime(now);
            sysUserRoleMapper.updateById(relation);
        }

        user.setStatus("DISABLED");
        user.setDeleted(user.getId());
        user.setUpdatedBy(operatorId);
        user.setUpdatedTime(now);
        sysUserMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteUsers(Long operatorId, List<Long> userIds) {
        requireUser(operatorId);
        if (CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "请选择要删除的用户");
        }

        Set<Long> normalizedUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (CollectionUtils.isEmpty(normalizedUserIds)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "请选择要删除的用户");
        }
        if (normalizedUserIds.contains(operatorId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "不能删除当前登录账号");
        }

        int deletedCount = 0;
        for (Long userId : normalizedUserIds) {
            deleteUser(operatorId, userId);
            deletedCount++;
        }
        return deletedCount;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserRoles(Long operatorId, Long userId, List<String> roleCodes) {
        requireUser(operatorId);
        SysUser user = requireUser(userId);

        if (CollectionUtils.isEmpty(roleCodes)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "请至少分配一个角色");
        }

        Map<String, SysRole> enabledRoleCodeMap = queryEnabledRoleCodeMap();
        Set<String> normalizedRoleCodes = roleCodes.stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeCode)
                .collect(Collectors.toCollection(HashSet::new));
        if (CollectionUtils.isEmpty(normalizedRoleCodes)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "请至少分配一个有效角色");
        }

        for (String roleCode : normalizedRoleCodes) {
            if (!enabledRoleCodeMap.containsKey(roleCode)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "无效的角色编码：" + roleCode);
            }
        }
        LocalDateTime now = LocalDateTime.now();
        List<SysUserRole> relations = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getDeleted, 0L));
        for (SysUserRole relation : relations) {
            relation.setDeleted(relation.getId());
            relation.setUpdatedBy(operatorId);
            relation.setUpdatedTime(now);
            sysUserRoleMapper.updateById(relation);
        }

        for (String roleCode : normalizedRoleCodes) {
            SysRole role = enabledRoleCodeMap.get(roleCode);
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(role.getId());
            relation.setCreatedBy(operatorId);
            relation.setUpdatedBy(operatorId);
            relation.setDeleted(0L);
            sysUserRoleMapper.insert(relation);
        }

        user.setUpdatedBy(operatorId);
        user.setUpdatedTime(now);
        sysUserMapper.updateById(user);
    }

    public UserImportResultVO importUsers(MultipartFile file, Long operatorId) {
        requireUser(operatorId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "请上传表格文件");
        }
        String fileName = file.getOriginalFilename();
        String lowerFileName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (!lowerFileName.endsWith(".xlsx") && !lowerFileName.endsWith(".xls")) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "仅支持 .xlsx 或 .xls 文件");
        }

        Map<String, SysRole> roleCodeMap = queryEnabledRoleCodeMap();
        Map<String, BaseDepartment> deptCodeMap = queryDepartmentCodeMap();
        Map<String, BaseMajor> majorCodeMap = queryMajorCodeMap();
        Map<String, BaseGrade> gradeCodeMap = queryGradeCodeMap();
        Map<String, List<BaseDepartment>> deptNameGroupMap = queryDepartmentNameGroupMap();
        Map<String, List<BaseMajor>> majorNameGroupMap = queryMajorNameGroupMap();
        Map<String, List<BaseGrade>> gradeNameGroupMap = queryGradeNameGroupMap();

        Set<String> importAccountNos = new HashSet<>();
        Set<String> importStudentNos = new HashSet<>();
        Set<String> importTeacherNos = new HashSet<>();

        UserImportResultVO result = new UserImportResultVO();
        DataFormatter formatter = new DataFormatter();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet == null ? 0 : sheet.getLastRowNum();
            for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isImportRowEmpty(row, formatter)) {
                    continue;
                }
                int excelRowNum = rowIndex + 1;
                result.setTotalRows(result.getTotalRows() + 1);
                try {
                    ImportRowData rowData = parseImportRow(
                            row,
                            formatter,
                            roleCodeMap,
                            deptCodeMap,
                            majorCodeMap,
                            gradeCodeMap,
                            deptNameGroupMap,
                            majorNameGroupMap,
                            gradeNameGroupMap
                    );
                    validateImportRowUniqueness(rowData, importAccountNos, importStudentNos, importTeacherNos);
                    transactionTemplate.executeWithoutResult(status ->
                            saveImportRow(rowData, operatorId, roleCodeMap));
                    importAccountNos.add(rowData.accountNo);
                    if (StringUtils.hasText(rowData.studentNo)) {
                        importStudentNos.add(rowData.studentNo);
                    }
                    if (StringUtils.hasText(rowData.teacherNo)) {
                        importTeacherNos.add(rowData.teacherNo);
                    }
                    result.setSuccessRows(result.getSuccessRows() + 1);
                } catch (Exception ex) {
                    UserImportErrorVO error = new UserImportErrorVO();
                    error.setRowNumber(excelRowNum);
                    error.setMessage(rootMessage(ex));
                    result.getErrors().add(error);
                }
            }
        } catch (Exception ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "表格文件读取失败");
        }
        result.setFailedRows(result.getTotalRows() - result.getSuccessRows());
        return result;
    }

    private UserItemVO toUserItemVO(SysUser user,
                                    Map<Long, String> roleNameMapByUserId,
                                    Map<Long, List<String>> roleCodeMapByUserId,
                                    Map<Long, String> deptNameMap,
                                    Map<Long, String> majorNameMap,
                                    Map<Long, String> gradeNameMap) {
        UserItemVO vo = new UserItemVO();
        vo.setId(user.getId());
        vo.setAccountNo(resolveAccountNo(user));
        vo.setRealName(user.getRealName());
        vo.setIdentityType(user.getIdentityType());
        vo.setRoleNames(roleNameMapByUserId.get(user.getId()));
        vo.setRoleCodes(roleCodeMapByUserId.get(user.getId()));
        vo.setStudentNo(user.getStudentNo());
        vo.setTeacherNo(user.getTeacherNo());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setDeptId(user.getDeptId());
        vo.setDeptName(user.getDeptId() == null ? null : deptNameMap.get(user.getDeptId()));
        vo.setMajorId(user.getMajorId());
        vo.setMajorName(user.getMajorId() == null ? null : majorNameMap.get(user.getMajorId()));
        vo.setGradeId(user.getGradeId());
        vo.setGradeName(user.getGradeId() == null ? null : gradeNameMap.get(user.getGradeId()));
        vo.setMustChangePassword(user.getMustChangePassword());
        vo.setCreatedTime(user.getCreatedTime());
        return vo;
    }

    private Map<Long, String> buildRoleNameMapByUserId(List<SysUser> users) {
        Set<Long> userIds = users.stream()
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<>();
        }
        List<SysUserRole> relations = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds)
                .eq(SysUserRole::getDeleted, 0L));
        if (CollectionUtils.isEmpty(relations)) {
            return new HashMap<>();
        }
        Set<Long> roleIds = relations.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> roleNameMap = sysRoleMapper.selectBatchIds(roleIds).stream()
                .filter(Objects::nonNull)
                .filter(role -> role.getDeleted() != null && role.getDeleted() == 0L)
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName, (left, right) -> left));
        return relations.stream()
                .filter(relation -> relation.getUserId() != null && relation.getRoleId() != null)
                .collect(Collectors.groupingBy(SysUserRole::getUserId,
                        Collectors.mapping(relation -> roleNameMap.get(relation.getRoleId()),
                                Collectors.collectingAndThen(Collectors.toList(), roleNames -> roleNames.stream()
                                        .filter(StringUtils::hasText)
                                        .distinct()
                                        .collect(Collectors.joining("、"))))));
    }

    private Map<Long, List<String>> buildRoleCodeMapByUserId(List<SysUser> users) {
        Set<Long> userIds = users.stream()
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<>();
        }
        List<SysUserRole> relations = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds)
                .eq(SysUserRole::getDeleted, 0L));
        if (CollectionUtils.isEmpty(relations)) {
            return new HashMap<>();
        }
        Set<Long> roleIds = relations.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> roleCodeMap = sysRoleMapper.selectBatchIds(roleIds).stream()
                .filter(Objects::nonNull)
                .filter(role -> role.getDeleted() != null && role.getDeleted() == 0L)
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleCode, (left, right) -> left));
        return relations.stream()
                .filter(relation -> relation.getUserId() != null && relation.getRoleId() != null)
                .collect(Collectors.groupingBy(SysUserRole::getUserId,
                        Collectors.mapping(relation -> roleCodeMap.get(relation.getRoleId()),
                                Collectors.collectingAndThen(Collectors.toList(), roleCodes -> roleCodes.stream()
                                        .filter(StringUtils::hasText)
                                        .distinct()
                                        .collect(Collectors.toList())))));
    }

    private Map<Long, String> buildDepartmentNameMap(List<SysUser> users) {
        Set<Long> deptIds = users.stream()
                .map(SysUser::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(deptIds)) {
            return new HashMap<>();
        }
        return departmentMapper.selectBatchIds(deptIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BaseDepartment::getId, BaseDepartment::getDeptName, (left, right) -> left));
    }

    private Map<Long, String> buildMajorNameMap(List<SysUser> users) {
        Set<Long> majorIds = users.stream()
                .map(SysUser::getMajorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(majorIds)) {
            return new HashMap<>();
        }
        return majorMapper.selectBatchIds(majorIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BaseMajor::getId, BaseMajor::getMajorName, (left, right) -> left));
    }

    private Map<Long, String> buildGradeNameMap(List<SysUser> users) {
        Set<Long> gradeIds = users.stream()
                .map(SysUser::getGradeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(gradeIds)) {
            return new HashMap<>();
        }
        return gradeMapper.selectBatchIds(gradeIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BaseGrade::getId, BaseGrade::getGradeName, (left, right) -> left));
    }

    private void ensureUserDeletable(SysUser user) {
        if (user == null || user.getId() == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "用户不存在");
        }
        Long userId = user.getId();
        long applicationCount = studentApplicationMapper.selectCount(new LambdaQueryWrapper<BizStudentApplication>()
                .eq(BizStudentApplication::getStudentId, userId)
                .eq(BizStudentApplication::getDeleted, 0L));
        long assignmentStudentCount = assignmentMapper.selectCount(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getStudentId, userId)
                .eq(BizAssignment::getDeleted, 0L));
        long assignmentTeacherCount = assignmentMapper.selectCount(new LambdaQueryWrapper<BizAssignment>()
                .and(w -> w.eq(BizAssignment::getInnerTeacherId, userId)
                        .or().eq(BizAssignment::getBaseTeacherId, userId)
                        .or().eq(BizAssignment::getAssignedBy, userId))
                .eq(BizAssignment::getDeleted, 0L));
        long materialCount = materialMapper.selectCount(new LambdaQueryWrapper<BizMaterial>()
                .eq(BizMaterial::getStudentId, userId)
                .eq(BizMaterial::getDeleted, 0L));
        long materialVersionCount = materialVersionMapper.selectCount(new LambdaQueryWrapper<BizMaterialVersion>()
                .eq(BizMaterialVersion::getSubmittedBy, userId)
                .eq(BizMaterialVersion::getDeleted, 0L));
        long evaluationCount = evaluationMapper.selectCount(new LambdaQueryWrapper<BizEvaluation>()
                .and(w -> w.eq(BizEvaluation::getStudentId, userId)
                        .or().eq(BizEvaluation::getEvaluatorId, userId))
                .eq(BizEvaluation::getDeleted, 0L));
        long scoreCount = scoreSheetMapper.selectCount(new LambdaQueryWrapper<BizScoreSheet>()
                .and(w -> w.eq(BizScoreSheet::getStudentId, userId)
                        .or().eq(BizScoreSheet::getAdjustBy, userId)
                        .or().eq(BizScoreSheet::getPublishedBy, userId))
                .eq(BizScoreSheet::getDeleted, 0L));
        if (applicationCount > 0 || assignmentStudentCount > 0 || assignmentTeacherCount > 0
                || materialCount > 0 || materialVersionCount > 0 || evaluationCount > 0 || scoreCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该用户已参与业务流程，不能删除，只能禁用");
        }
    }

    private SysUser requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "用户标识不能为空");
        }
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return user;
    }

    private void ensureLoginNameUnique(String loginName, Long excludeUserId) {
        SysUser existing = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getLoginName, loginName)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeUserId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "登录账号已存在");
        }
    }

    private void ensureStudentNoUnique(String studentNo, Long excludeUserId) {
        SysUser existing = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStudentNo, studentNo)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeUserId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "学号已存在");
        }
    }

    private void ensureTeacherNoUnique(String teacherNo, Long excludeUserId) {
        SysUser existing = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTeacherNo, teacherNo)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeUserId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "工号已存在");
        }
    }

    private Map<String, SysRole> queryEnabledRoleCodeMap() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getDeleted, 0L)
                        .eq(SysRole::getStatus, STATUS_ENABLED))
                .stream()
                .collect(Collectors.toMap(item -> normalizeCode(item.getRoleCode()), item -> item, (left, right) -> left));
    }

    private void validateCreateUserRequest(String identityType,
                                           Set<String> roleCodes,
                                           String studentNo,
                                           String teacherNo,
                                           Long deptId,
                                           Long majorId,
                                           Long gradeId) {
        if (ROLE_STUDENT.equals(identityType)) {
            if (!StringUtils.hasText(studentNo)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "师范生账号必须填写学号");
            }
            if (deptId == null || majorId == null || gradeId == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "师范生账号必须填写院系、专业和年级");
            }
            return;
        }

        if (!StringUtils.hasText(teacherNo)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "教师或管理员账号必须填写工号");
        }
        if (IDENTITY_TEACHER.equals(identityType) && deptId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "教师账号必须选择院系");
        }

        if (roleCodes.contains(ROLE_DEPT_ADMIN) && deptId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "院系管理员必须选择院系");
        }
    }

    private void validateRoleIdentityConsistency(String identityType, Set<String> roleCodes) {
        if (ROLE_STUDENT.equals(identityType)) {
            if (!roleCodes.contains(ROLE_STUDENT)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "师范生账号必须分配“师范生”角色");
            }
            if (roleCodes.size() > 1) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "师范生账号不能同时分配教师或管理员角色");
            }
            return;
        }

        if (IDENTITY_TEACHER.equals(identityType)) {
            boolean hasNonTeacherRole = roleCodes.stream().anyMatch(roleCode -> !isTeacherRole(roleCode));
            if (hasNonTeacherRole) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "教师账号只能分配教师角色");
            }
            return;
        }

        if (IDENTITY_ADMIN.equals(identityType)) {
            boolean hasNonAdminRole = roleCodes.stream().anyMatch(roleCode -> !isAdminRole(roleCode));
            if (hasNonAdminRole) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "管理员账号只能分配管理员角色");
            }
            return;
        }

        if (roleCodes.contains(ROLE_STUDENT)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "教师账号不能分配“师范生”角色");
        }
    }

    private boolean isAdminRole(String roleCode) {
        return ROLE_DEPT_ADMIN.equals(roleCode)
                || ROLE_ACADEMIC_ADMIN.equals(roleCode)
                || ROLE_SYS_ADMIN.equals(roleCode);
    }

    private boolean isTeacherRole(String roleCode) {
        return ROLE_INNER_TEACHER.equals(roleCode)
                || ROLE_BASE_TEACHER.equals(roleCode);
    }

    private boolean shouldPersistDepartment(String identityType, Set<String> roleCodes) {
        if (ROLE_STUDENT.equals(identityType) || IDENTITY_TEACHER.equals(identityType)) {
            return true;
        }
        return IDENTITY_ADMIN.equals(identityType) && roleCodes.contains(ROLE_DEPT_ADMIN);
    }

    private BaseDepartment requireEnabledDepartment(Long deptId) {
        BaseDepartment department = departmentMapper.selectOne(new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getId, deptId)
                .eq(BaseDepartment::getDeleted, 0L)
                .eq(BaseDepartment::getStatus, STATUS_ENABLED)
                .last("LIMIT 1"));
        if (department == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "所选院系不存在或未启用");
        }
        return department;
    }

    private BaseMajor requireEnabledMajor(Long majorId) {
        BaseMajor major = majorMapper.selectOne(new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getId, majorId)
                .eq(BaseMajor::getDeleted, 0L)
                .eq(BaseMajor::getStatus, STATUS_ENABLED)
                .last("LIMIT 1"));
        if (major == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "所选专业不存在或未启用");
        }
        return major;
    }

    private BaseGrade requireEnabledGrade(Long gradeId) {
        BaseGrade grade = gradeMapper.selectOne(new LambdaQueryWrapper<BaseGrade>()
                .eq(BaseGrade::getId, gradeId)
                .eq(BaseGrade::getDeleted, 0L)
                .eq(BaseGrade::getStatus, STATUS_ENABLED)
                .last("LIMIT 1"));
        if (grade == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "所选年级不存在或未启用");
        }
        return grade;
    }

    private void validateDepartmentMajorRelation(BaseDepartment department, BaseMajor major) {
        if (department == null && major != null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "选择专业时必须同时选择院系");
        }
        if (department != null && major != null && !Objects.equals(major.getDeptId(), department.getId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "所选专业不属于当前院系");
        }
    }

    private Map<String, BaseDepartment> queryDepartmentCodeMap() {
        return departmentMapper.selectList(new LambdaQueryWrapper<BaseDepartment>()
                        .eq(BaseDepartment::getDeleted, 0L)
                        .eq(BaseDepartment::getStatus, STATUS_ENABLED))
                .stream()
                .collect(Collectors.toMap(item -> normalizeCode(item.getDeptCode()), item -> item, (left, right) -> left));
    }

    private Map<String, List<BaseDepartment>> queryDepartmentNameGroupMap() {
        return departmentMapper.selectList(new LambdaQueryWrapper<BaseDepartment>()
                        .eq(BaseDepartment::getDeleted, 0L)
                        .eq(BaseDepartment::getStatus, STATUS_ENABLED))
                .stream()
                .collect(Collectors.groupingBy(item -> normalizeLookupText(item.getDeptName())));
    }

    private Map<String, BaseMajor> queryMajorCodeMap() {
        return majorMapper.selectList(new LambdaQueryWrapper<BaseMajor>()
                        .eq(BaseMajor::getDeleted, 0L)
                        .eq(BaseMajor::getStatus, STATUS_ENABLED))
                .stream()
                .collect(Collectors.toMap(item -> normalizeCode(item.getMajorCode()), item -> item, (left, right) -> left));
    }

    private Map<String, List<BaseMajor>> queryMajorNameGroupMap() {
        return majorMapper.selectList(new LambdaQueryWrapper<BaseMajor>()
                        .eq(BaseMajor::getDeleted, 0L)
                        .eq(BaseMajor::getStatus, STATUS_ENABLED))
                .stream()
                .collect(Collectors.groupingBy(item -> normalizeLookupText(item.getMajorName())));
    }

    private Map<String, BaseGrade> queryGradeCodeMap() {
        return gradeMapper.selectList(new LambdaQueryWrapper<BaseGrade>()
                        .eq(BaseGrade::getDeleted, 0L)
                        .eq(BaseGrade::getStatus, STATUS_ENABLED))
                .stream()
                .collect(Collectors.toMap(item -> normalizeCode(item.getGradeCode()), item -> item, (left, right) -> left));
    }

    private Map<String, List<BaseGrade>> queryGradeNameGroupMap() {
        return gradeMapper.selectList(new LambdaQueryWrapper<BaseGrade>()
                        .eq(BaseGrade::getDeleted, 0L)
                        .eq(BaseGrade::getStatus, STATUS_ENABLED))
                .stream()
                .collect(Collectors.groupingBy(item -> normalizeLookupText(item.getGradeName())));
    }

    private ImportRowData parseImportRow(Row row,
                                         DataFormatter formatter,
                                         Map<String, SysRole> roleCodeMap,
                                         Map<String, BaseDepartment> deptCodeMap,
                                         Map<String, BaseMajor> majorCodeMap,
                                         Map<String, BaseGrade> gradeCodeMap,
                                         Map<String, List<BaseDepartment>> deptNameGroupMap,
                                         Map<String, List<BaseMajor>> majorNameGroupMap,
                                         Map<String, List<BaseGrade>> gradeNameGroupMap) {
        ImportRowData data = new ImportRowData();
        data.realName = trimToNull(cellText(row, 0, formatter));
        String rawIdentityType = normalizeCode(cellText(row, 1, formatter));
        data.identityType = normalizeIdentityType(rawIdentityType);
        data.studentNo = trimToNull(cellText(row, 2, formatter));
        data.teacherNo = trimToNull(cellText(row, 3, formatter));
        data.phone = trimToNull(cellText(row, 4, formatter));
        data.email = trimToNull(cellText(row, 5, formatter));
        String deptText = trimToNull(cellText(row, 6, formatter));
        String majorText = trimToNull(cellText(row, 7, formatter));
        String gradeText = trimToNull(cellText(row, 8, formatter));
        data.status = normalizeImportStatus(cellText(row, 9, formatter));
        String roleCodesText = trimToNull(cellText(row, 10, formatter));
        data.password = trimToNull(cellText(row, 11, formatter));

        if (!StringUtils.hasText(data.realName)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "姓名不能为空");
        }

        data.roleCodes = resolveRoleCodes(rawIdentityType, roleCodesText);
        if (CollectionUtils.isEmpty(data.roleCodes)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "角色编码不能为空");
        }
        for (String roleCode : data.roleCodes) {
            if (!roleCodeMap.containsKey(roleCode)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "角色编码不存在：" + roleCode);
            }
        }

        if (data.roleCodes.contains(ROLE_STUDENT)) {
            if (!StringUtils.hasText(data.studentNo)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "师范生角色必须填写学号");
            }
            if (!StringUtils.hasText(deptText) || !StringUtils.hasText(majorText) || !StringUtils.hasText(gradeText)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "师范生角色必须填写院系/专业/年级编码");
            }
        }
        if (!data.roleCodes.contains(ROLE_STUDENT)) {
            if (!StringUtils.hasText(data.teacherNo)) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "教师或管理员角色必须填写工号");
            }
        }

        data.department = resolveDepartmentByCodeOrName(deptText, deptCodeMap, deptNameGroupMap);
        data.major = resolveMajorByCodeOrName(majorText, data.department, majorCodeMap, majorNameGroupMap);
        data.grade = resolveGradeByCodeOrName(gradeText, gradeCodeMap, gradeNameGroupMap);
        if (StringUtils.hasText(deptText) && data.department == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "院系编码/名称不存在：" + deptText);
        }
        if (StringUtils.hasText(majorText) && data.major == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "专业编码/名称不存在：" + majorText);
        }
        if (StringUtils.hasText(gradeText) && data.grade == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "年级编码/名称不存在：" + gradeText);
        }
        if (data.major != null && data.department != null && !Objects.equals(data.major.getDeptId(), data.department.getId())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "专业与院系不匹配");
        }
        if (!StringUtils.hasText(data.password)) {
            data.password = DEFAULT_RESET_PASSWORD;
        }
        if (!StringUtils.hasText(data.identityType)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "身份类型不能为空");
        }
        validateCreateUserRequest(
                data.identityType,
                data.roleCodes,
                data.studentNo,
                data.teacherNo,
                data.department == null ? null : data.department.getId(),
                data.major == null ? null : data.major.getId(),
                data.grade == null ? null : data.grade.getId()
        );
        if (ROLE_STUDENT.equals(data.identityType)) {
            data.teacherNo = null;
        } else {
            data.studentNo = null;
            data.major = null;
            data.grade = null;
            if (!shouldPersistDepartment(data.identityType, data.roleCodes)) {
                data.department = null;
            }
        }
        data.accountNo = resolveImportAccountNo(data);
        return data;
    }

    private void validateImportRowUniqueness(ImportRowData data,
                                             Set<String> importAccountNos,
                                             Set<String> importStudentNos,
                                             Set<String> importTeacherNos) {
        if (importAccountNos.contains(data.accountNo)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "导入文件中存在重复登录账号：" + data.accountNo);
        }
        if (StringUtils.hasText(data.studentNo) && importStudentNos.contains(data.studentNo)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "导入文件中存在重复学号：" + data.studentNo);
        }
        if (StringUtils.hasText(data.teacherNo) && importTeacherNos.contains(data.teacherNo)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "导入文件中存在重复工号：" + data.teacherNo);
        }
    }

    private void saveImportRow(ImportRowData data,
                               Long operatorId,
                               Map<String, SysRole> roleCodeMap) {
        ensureLoginNameUnique(data.accountNo, null);
        if (StringUtils.hasText(data.studentNo)) {
            ensureStudentNoUnique(data.studentNo, null);
        }
        if (StringUtils.hasText(data.teacherNo)) {
            ensureTeacherNoUnique(data.teacherNo, null);
        }

        SysUser user = new SysUser();
        user.setLoginName(data.accountNo);
        user.setPasswordHash(passwordEncoder.encode(data.password));
        user.setRealName(data.realName);
        user.setIdentityType(data.identityType);
        user.setStudentNo(data.studentNo);
        user.setTeacherNo(data.teacherNo);
        user.setPhone(data.phone);
        user.setEmail(data.email);
        user.setDeptId(data.department == null ? null : data.department.getId());
        user.setMajorId(data.major == null ? null : data.major.getId());
        user.setGradeId(data.grade == null ? null : data.grade.getId());
        user.setStatus(data.status);
        user.setMustChangePassword(1);
        user.setCreatedBy(operatorId);
        user.setUpdatedBy(operatorId);
        user.setDeleted(0L);
        sysUserMapper.insert(user);

        for (String roleCode : data.roleCodes) {
            SysRole role = roleCodeMap.get(roleCode);
            if (role == null) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "角色编码不存在：" + roleCode);
            }
            SysUserRole relation = new SysUserRole();
            relation.setUserId(user.getId());
            relation.setRoleId(role.getId());
            relation.setCreatedBy(operatorId);
            relation.setUpdatedBy(operatorId);
            relation.setDeleted(0L);
            sysUserRoleMapper.insert(relation);
        }
    }

    private Set<String> resolveRoleCodes(String identityType, String roleCodesText) {
        Set<String> result = new HashSet<>();
        if (StringUtils.hasText(roleCodesText)) {
            String[] segments = roleCodesText.split("[,;\\uFF0C\\uFF1B\\s]+");
            for (String segment : segments) {
                String code = normalizeCode(segment);
                if (StringUtils.hasText(code)) {
                    result.add(code);
                }
            }
        }
        if (!result.isEmpty()) {
            return result;
        }
        return result;
    }

    private String normalizeImportStatus(String text) {
        String normalized = normalizeCode(text);
        if (!StringUtils.hasText(normalized)) {
            return STATUS_ENABLED;
        }
        if (STATUS_PENDING.equals(normalized) || STATUS_ENABLED.equals(normalized) || "DISABLED".equals(normalized) || "LOCKED".equals(normalized)) {
            return normalized;
        }
        throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "不支持的状态值：" + text);
    }

    private boolean isImportRowEmpty(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i <= 11; i++) {
            if (StringUtils.hasText(cellText(row, i, formatter))) {
                return false;
            }
        }
        return true;
    }

    private String cellText(Row row, int cellIndex, DataFormatter formatter) {
        if (row == null || row.getCell(cellIndex) == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(cellIndex)).trim();
    }

    private String normalizeCode(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeLookupText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim().toLowerCase(Locale.ROOT);
    }

    private BaseDepartment resolveDepartmentByCodeOrName(String text,
                                                         Map<String, BaseDepartment> deptCodeMap,
                                                         Map<String, List<BaseDepartment>> deptNameGroupMap) {
        String normalizedCode = normalizeCode(text);
        if (!StringUtils.hasText(normalizedCode)) {
            return null;
        }
        BaseDepartment byCode = deptCodeMap.get(normalizedCode);
        if (byCode != null) {
            return byCode;
        }
        return resolveUniqueByName(text, deptNameGroupMap, "院系");
    }

    private BaseMajor resolveMajorByCodeOrName(String text,
                                               BaseDepartment department,
                                               Map<String, BaseMajor> majorCodeMap,
                                               Map<String, List<BaseMajor>> majorNameGroupMap) {
        String normalizedCode = normalizeCode(text);
        if (!StringUtils.hasText(normalizedCode)) {
            return null;
        }
        BaseMajor byCode = majorCodeMap.get(normalizedCode);
        if (byCode != null) {
            return byCode;
        }
        List<BaseMajor> matches = majorNameGroupMap.get(normalizeLookupText(text));
        if (CollectionUtils.isEmpty(matches)) {
            return null;
        }
        if (department != null) {
            List<BaseMajor> departmentMatches = matches.stream()
                    .filter(item -> Objects.equals(item.getDeptId(), department.getId()))
                    .collect(Collectors.toList());
            if (departmentMatches.size() == 1) {
                return departmentMatches.get(0);
            }
            if (departmentMatches.size() > 1) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "专业名称不唯一：" + text);
            }
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "专业名称不唯一：" + text);
    }

    private BaseGrade resolveGradeByCodeOrName(String text,
                                               Map<String, BaseGrade> gradeCodeMap,
                                               Map<String, List<BaseGrade>> gradeNameGroupMap) {
        String normalizedCode = normalizeCode(text);
        if (!StringUtils.hasText(normalizedCode)) {
            return null;
        }
        BaseGrade byCode = gradeCodeMap.get(normalizedCode);
        if (byCode != null) {
            return byCode;
        }
        return resolveUniqueByName(text, gradeNameGroupMap, "年级");
    }

    private <T> T resolveUniqueByName(String text, Map<String, List<T>> nameGroupMap, String label) {
        String normalizedName = normalizeLookupText(text);
        if (!StringUtils.hasText(normalizedName)) {
            return null;
        }
        List<T> matches = nameGroupMap.get(normalizedName);
        if (CollectionUtils.isEmpty(matches)) {
            return null;
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), label + "名称不唯一：" + text);
    }

    private String normalizeIdentityType(String text) {
        String normalized = normalizeCode(text);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        if (ROLE_STUDENT.equals(normalized)) {
            return ROLE_STUDENT;
        }
        if (IDENTITY_TEACHER.equals(normalized)) {
            return IDENTITY_TEACHER;
        }
        if (IDENTITY_ADMIN.equals(normalized)) {
            return IDENTITY_ADMIN;
        }
        return null;
    }

    private String trimToNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }

    private String resolveAccountNo(SysUser user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.hasText(user.getStudentNo())) {
            return user.getStudentNo().trim();
        }
        if (StringUtils.hasText(user.getTeacherNo())) {
            return user.getTeacherNo().trim();
        }
        return StringUtils.hasText(user.getLoginName()) ? user.getLoginName().trim() : null;
    }

    private String resolveImportAccountNo(ImportRowData data) {
        if (data == null) {
            return null;
        }
        if (StringUtils.hasText(data.studentNo)) {
            return data.studentNo.trim();
        }
        if (StringUtils.hasText(data.teacherNo)) {
            return data.teacherNo.trim();
        }
        throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "请根据角色填写学号或工号");
    }

    private String rootMessage(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }
        String message = cursor.getMessage();
        if (!StringUtils.hasText(message) && throwable != null) {
            message = throwable.getMessage();
        }
        return StringUtils.hasText(message) ? message : "鏈煡閿欒";
    }

    private static class ImportRowData {
        private String accountNo;
        private String realName;
        private String identityType;
        private String studentNo;
        private String teacherNo;
        private String phone;
        private String email;
        private String status;
        private String password;
        private Set<String> roleCodes = new HashSet<>();
        private BaseDepartment department;
        private BaseMajor major;
        private BaseGrade grade;
    }

    public List<IdNameOptionVO> queryEnabledDepartmentOptions() {
        return departmentMapper.selectList(new LambdaQueryWrapper<BaseDepartment>()
                        .eq(BaseDepartment::getDeleted, 0L)
                        .eq(BaseDepartment::getStatus, "ENABLED")
                        .orderByAsc(BaseDepartment::getDeptName))
                .stream()
                .map(this::toDepartmentOption)
                .collect(Collectors.toList());
    }

    public List<IdNameOptionVO> queryEnabledMajorOptions(Long deptId) {
        LambdaQueryWrapper<BaseMajor> wrapper = new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getDeleted, 0L)
                .eq(BaseMajor::getStatus, "ENABLED")
                .orderByAsc(BaseMajor::getMajorName);
        if (deptId != null) {
            wrapper.eq(BaseMajor::getDeptId, deptId);
        }
        return majorMapper.selectList(wrapper)
                .stream()
                .map(this::toMajorOption)
                .collect(Collectors.toList());
    }

    public List<IdNameOptionVO> queryEnabledGradeOptions() {
        return gradeMapper.selectList(new LambdaQueryWrapper<BaseGrade>()
                        .eq(BaseGrade::getDeleted, 0L)
                        .eq(BaseGrade::getStatus, "ENABLED")
                        .orderByAsc(BaseGrade::getGradeCode))
                .stream()
                .map(this::toGradeOption)
                .collect(Collectors.toList());
    }

    private IdNameOptionVO toDepartmentOption(BaseDepartment entity) {
        IdNameOptionVO vo = new IdNameOptionVO();
        vo.setId(entity.getId());
        vo.setName(entity.getDeptName());
        return vo;
    }

    private IdNameOptionVO toMajorOption(BaseMajor entity) {
        IdNameOptionVO vo = new IdNameOptionVO();
        vo.setId(entity.getId());
        vo.setName(entity.getMajorName());
        return vo;
    }

    private IdNameOptionVO toGradeOption(BaseGrade entity) {
        IdNameOptionVO vo = new IdNameOptionVO();
        vo.setId(entity.getId());
        vo.setName(entity.getGradeName());
        return vo;
    }
}




