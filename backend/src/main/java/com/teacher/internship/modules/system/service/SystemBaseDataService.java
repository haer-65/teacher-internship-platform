package com.teacher.internship.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.base.entity.BaseDepartment;
import com.teacher.internship.modules.base.entity.BaseGrade;
import com.teacher.internship.modules.base.entity.BaseInternshipBase;
import com.teacher.internship.modules.base.entity.BaseMajor;
import com.teacher.internship.modules.base.mapper.BaseDepartmentMapper;
import com.teacher.internship.modules.base.mapper.BaseGradeMapper;
import com.teacher.internship.modules.base.mapper.BaseInternshipBaseMapper;
import com.teacher.internship.modules.base.mapper.BaseMajorMapper;
import com.teacher.internship.modules.application.entity.BizApplicationPreference;
import com.teacher.internship.modules.application.entity.BizAssignment;
import com.teacher.internship.modules.application.mapper.BizApplicationPreferenceMapper;
import com.teacher.internship.modules.application.mapper.BizAssignmentMapper;
import com.teacher.internship.modules.plan.entity.BizInternshipPlan;
import com.teacher.internship.modules.plan.entity.BizPlanBase;
import com.teacher.internship.modules.plan.mapper.BizInternshipPlanMapper;
import com.teacher.internship.modules.plan.mapper.BizPlanBaseMapper;
import com.teacher.internship.modules.system.dto.BaseDepartmentSaveRequest;
import com.teacher.internship.modules.system.dto.BaseGradeSaveRequest;
import com.teacher.internship.modules.system.dto.BaseInternshipBaseSaveRequest;
import com.teacher.internship.modules.system.dto.BaseMajorSaveRequest;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import com.teacher.internship.modules.system.vo.BaseDepartmentItemVO;
import com.teacher.internship.modules.system.vo.BaseGradeItemVO;
import com.teacher.internship.modules.system.vo.BaseInternshipBaseItemVO;
import com.teacher.internship.modules.system.vo.BaseMajorItemVO;
import com.teacher.internship.modules.system.vo.IdNameOptionVO;
import com.teacher.internship.modules.system.vo.PageResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SystemBaseDataService {

    private static final String STATUS_ENABLED = "ENABLED";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String DEPARTMENT_CODE_PREFIX = "DEPT";
    private static final String MAJOR_CODE_PREFIX = "MAJOR";
    private static final String GRADE_CODE_PREFIX = "G";
    private static final String BASE_CODE_PREFIX = "BASE";

    private final BaseDepartmentMapper departmentMapper;
    private final BaseMajorMapper majorMapper;
    private final BaseGradeMapper gradeMapper;
    private final BaseInternshipBaseMapper internshipBaseMapper;
    private final SysUserMapper sysUserMapper;
    private final BizInternshipPlanMapper internshipPlanMapper;
    private final BizPlanBaseMapper planBaseMapper;
    private final BizAssignmentMapper assignmentMapper;
    private final BizApplicationPreferenceMapper applicationPreferenceMapper;

    public SystemBaseDataService(BaseDepartmentMapper departmentMapper,
                                 BaseMajorMapper majorMapper,
                                 BaseGradeMapper gradeMapper,
                                 BaseInternshipBaseMapper internshipBaseMapper,
                                 SysUserMapper sysUserMapper,
                                 BizInternshipPlanMapper internshipPlanMapper,
                                 BizPlanBaseMapper planBaseMapper,
                                 BizAssignmentMapper assignmentMapper,
                                 BizApplicationPreferenceMapper applicationPreferenceMapper) {
        this.departmentMapper = departmentMapper;
        this.majorMapper = majorMapper;
        this.gradeMapper = gradeMapper;
        this.internshipBaseMapper = internshipBaseMapper;
        this.sysUserMapper = sysUserMapper;
        this.internshipPlanMapper = internshipPlanMapper;
        this.planBaseMapper = planBaseMapper;
        this.assignmentMapper = assignmentMapper;
        this.applicationPreferenceMapper = applicationPreferenceMapper;
    }

    public PageResultVO<BaseDepartmentItemVO> queryDepartmentPage(long page, long size, String keyword, String status) {
        LambdaQueryWrapper<BaseDepartment> wrapper = new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getDeleted, 0L)
                .orderByAsc(BaseDepartment::getDeptCode)
                .orderByDesc(BaseDepartment::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BaseDepartment::getDeptCode, keyword)
                    .or().like(BaseDepartment::getDeptName, keyword)
                    .or().like(BaseDepartment::getLeaderName, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(BaseDepartment::getStatus, normalizeStatus(status));
        }
        Page<BaseDepartment> pageResult = departmentMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)), wrapper);
        PageResultVO<BaseDepartmentItemVO> result = new PageResultVO<>();
        result.setPage(normalizePage(page));
        result.setSize(normalizeSize(size));
        result.setTotal(pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream().map(this::toDepartmentVO).collect(Collectors.toList()));
        return result;
    }

    public PageResultVO<BaseMajorItemVO> queryMajorPage(long page,
                                                        long size,
                                                        String keyword,
                                                        String status,
                                                        Long deptId) {
        LambdaQueryWrapper<BaseMajor> wrapper = new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getDeleted, 0L)
                .orderByAsc(BaseMajor::getMajorCode)
                .orderByDesc(BaseMajor::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BaseMajor::getMajorCode, keyword)
                    .or().like(BaseMajor::getMajorName, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(BaseMajor::getStatus, normalizeStatus(status));
        }
        if (deptId != null) {
            wrapper.eq(BaseMajor::getDeptId, deptId);
        }
        Page<BaseMajor> pageResult = majorMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)), wrapper);
        Set<Long> deptIds = pageResult.getRecords().stream().map(BaseMajor::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, BaseDepartment> deptMap = queryDepartmentMap(deptIds);

        PageResultVO<BaseMajorItemVO> result = new PageResultVO<>();
        result.setPage(normalizePage(page));
        result.setSize(normalizeSize(size));
        result.setTotal(pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream()
                .map(item -> toMajorVO(item, deptMap.get(item.getDeptId())))
                .collect(Collectors.toList()));
        return result;
    }

    public PageResultVO<BaseGradeItemVO> queryGradePage(long page, long size, String keyword, String status) {
        LambdaQueryWrapper<BaseGrade> wrapper = new LambdaQueryWrapper<BaseGrade>()
                .eq(BaseGrade::getDeleted, 0L)
                .orderByAsc(BaseGrade::getGradeCode)
                .orderByDesc(BaseGrade::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BaseGrade::getGradeCode, keyword)
                    .or().like(BaseGrade::getGradeName, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(BaseGrade::getStatus, normalizeStatus(status));
        }
        Page<BaseGrade> pageResult = gradeMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)), wrapper);
        PageResultVO<BaseGradeItemVO> result = new PageResultVO<>();
        result.setPage(normalizePage(page));
        result.setSize(normalizeSize(size));
        result.setTotal(pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream().map(this::toGradeVO).collect(Collectors.toList()));
        return result;
    }

    public PageResultVO<BaseInternshipBaseItemVO> queryInternshipBasePage(long page, long size, String keyword, String status) {
        LambdaQueryWrapper<BaseInternshipBase> wrapper = new LambdaQueryWrapper<BaseInternshipBase>()
                .eq(BaseInternshipBase::getDeleted, 0L)
                .orderByAsc(BaseInternshipBase::getBaseCode)
                .orderByDesc(BaseInternshipBase::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BaseInternshipBase::getBaseCode, keyword)
                    .or().like(BaseInternshipBase::getBaseName, keyword)
                    .or().like(BaseInternshipBase::getProvince, keyword)
                    .or().like(BaseInternshipBase::getCity, keyword)
                    .or().like(BaseInternshipBase::getDistrict, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(BaseInternshipBase::getStatus, normalizeStatus(status));
        }
        Page<BaseInternshipBase> pageResult = internshipBaseMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)), wrapper);
        PageResultVO<BaseInternshipBaseItemVO> result = new PageResultVO<>();
        result.setPage(normalizePage(page));
        result.setSize(normalizeSize(size));
        result.setTotal(pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream().map(this::toInternshipBaseVO).collect(Collectors.toList()));
        return result;
    }

    public List<IdNameOptionVO> queryDepartmentOptions() {
        return departmentMapper.selectList(new LambdaQueryWrapper<BaseDepartment>()
                        .eq(BaseDepartment::getDeleted, 0L)
                        .orderByAsc(BaseDepartment::getDeptName))
                .stream()
                .map(this::toDepartmentOption)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseDepartmentItemVO createDepartment(BaseDepartmentSaveRequest request, Long operatorId) {
        String code = resolveDepartmentCode(request.getDeptCode());
        ensureDepartmentCodeUnique(code, null);
        String deptName = trimText(request.getDeptName());
        ensureDepartmentNameUnique(deptName, null);
        BaseDepartment entity = new BaseDepartment();
        entity.setDeptCode(code);
        entity.setDeptName(deptName);
        entity.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        entity.setLeaderName(trimText(request.getLeaderName()));
        entity.setContactPhone(trimText(request.getContactPhone()));
        entity.setStatus(STATUS_ENABLED);
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setDeleted(0L);
        departmentMapper.insert(entity);
        return toDepartmentVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseDepartmentItemVO updateDepartment(Long id, BaseDepartmentSaveRequest request, Long operatorId) {
        BaseDepartment entity = requireDepartment(id);
        String code = resolveDepartmentCodeForUpdate(request.getDeptCode(), entity.getDeptCode());
        ensureDepartmentCodeUnique(code, id);
        String deptName = trimText(request.getDeptName());
        ensureDepartmentNameUnique(deptName, id);
        entity.setDeptCode(code);
        entity.setDeptName(deptName);
        entity.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        entity.setLeaderName(trimText(request.getLeaderName()));
        entity.setContactPhone(trimText(request.getContactPhone()));
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        departmentMapper.updateById(entity);
        return toDepartmentVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseDepartmentItemVO updateDepartmentStatus(Long id, String status, Long operatorId) {
        BaseDepartment entity = requireDepartment(id);
        entity.setStatus(normalizeStatus(status));
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        departmentMapper.updateById(entity);
        return toDepartmentVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long id, Long operatorId) {
        BaseDepartment entity = requireDepartmentForDelete(id);
        if (entity.getDeleted() != null && entity.getDeleted() != 0L) {
            return;
        }
        ensureDepartmentDeletable(entity.getId());
        entity.setStatus(STATUS_DISABLED);
        entity.setDeleted(entity.getId());
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        departmentMapper.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseMajorItemVO createMajor(BaseMajorSaveRequest request, Long operatorId) {
        String code = resolveMajorCode(request.getMajorCode());
        ensureMajorCodeUnique(code, null);
        String majorName = trimText(request.getMajorName());
        ensureMajorNameUnique(majorName, null);
        BaseDepartment department = requireDepartment(request.getDeptId());
        BaseMajor entity = new BaseMajor();
        entity.setDeptId(department.getId());
        entity.setMajorCode(code);
        entity.setMajorName(majorName);
        entity.setStatus(STATUS_ENABLED);
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setDeleted(0L);
        majorMapper.insert(entity);
        return toMajorVO(entity, department);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseMajorItemVO updateMajor(Long id, BaseMajorSaveRequest request, Long operatorId) {
        BaseMajor entity = requireMajor(id);
        String code = resolveMajorCodeForUpdate(request.getMajorCode(), entity.getMajorCode());
        ensureMajorCodeUnique(code, id);
        String majorName = trimText(request.getMajorName());
        ensureMajorNameUnique(majorName, id);
        BaseDepartment department = requireDepartment(request.getDeptId());
        entity.setDeptId(department.getId());
        entity.setMajorCode(code);
        entity.setMajorName(majorName);
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        majorMapper.updateById(entity);
        return toMajorVO(entity, department);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseMajorItemVO updateMajorStatus(Long id, String status, Long operatorId) {
        BaseMajor entity = requireMajor(id);
        entity.setStatus(normalizeStatus(status));
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        majorMapper.updateById(entity);
        BaseDepartment department = queryDepartment(entity.getDeptId());
        return toMajorVO(entity, department);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMajor(Long id, Long operatorId) {
        BaseMajor entity = requireMajorForDelete(id);
        if (entity.getDeleted() != null && entity.getDeleted() != 0L) {
            return;
        }
        ensureMajorDeletable(entity.getId());
        entity.setStatus(STATUS_DISABLED);
        entity.setDeleted(entity.getId());
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        majorMapper.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseGradeItemVO createGrade(BaseGradeSaveRequest request, Long operatorId) {
        String code = resolveGradeCode(request.getGradeCode(), request.getGradeName());
        ensureGradeCodeUnique(code, null);
        String gradeName = trimText(request.getGradeName());
        ensureGradeNameUnique(gradeName, null);
        BaseGrade entity = new BaseGrade();
        entity.setGradeCode(code);
        entity.setGradeName(gradeName);
        entity.setStatus(STATUS_ENABLED);
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setDeleted(0L);
        gradeMapper.insert(entity);
        return toGradeVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseGradeItemVO updateGrade(Long id, BaseGradeSaveRequest request, Long operatorId) {
        BaseGrade entity = requireGrade(id);
        String code = resolveGradeCodeForUpdate(request.getGradeCode(), request.getGradeName(), entity.getGradeCode());
        ensureGradeCodeUnique(code, id);
        String gradeName = trimText(request.getGradeName());
        ensureGradeNameUnique(gradeName, id);
        entity.setGradeCode(code);
        entity.setGradeName(gradeName);
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        gradeMapper.updateById(entity);
        return toGradeVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseGradeItemVO updateGradeStatus(Long id, String status, Long operatorId) {
        BaseGrade entity = requireGrade(id);
        entity.setStatus(normalizeStatus(status));
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        gradeMapper.updateById(entity);
        return toGradeVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteGrade(Long id, Long operatorId) {
        BaseGrade entity = requireGradeForDelete(id);
        if (entity.getDeleted() != null && entity.getDeleted() != 0L) {
            return;
        }
        ensureGradeDeletable(entity.getId());
        entity.setStatus(STATUS_DISABLED);
        entity.setDeleted(entity.getId());
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        gradeMapper.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseInternshipBaseItemVO createInternshipBase(BaseInternshipBaseSaveRequest request, Long operatorId) {
        String code = resolveInternshipBaseCode(request.getBaseCode());
        ensureInternshipBaseCodeUnique(code, null);
        String baseName = trimText(request.getBaseName());
        ensureInternshipBaseNameUnique(baseName, null);
        BaseInternshipBase entity = new BaseInternshipBase();
        entity.setBaseCode(code);
        entity.setBaseName(baseName);
        entity.setProvince(trimText(request.getProvince()));
        entity.setCity(trimText(request.getCity()));
        entity.setDistrict(trimText(request.getDistrict()));
        entity.setAddress(trimText(request.getAddress()));
        entity.setContactPerson(trimText(request.getContactPerson()));
        entity.setContactPhone(trimText(request.getContactPhone()));
        entity.setStatus(STATUS_ENABLED);
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setDeleted(0L);
        internshipBaseMapper.insert(entity);
        return toInternshipBaseVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseInternshipBaseItemVO updateInternshipBase(Long id, BaseInternshipBaseSaveRequest request, Long operatorId) {
        BaseInternshipBase entity = requireInternshipBase(id);
        String code = resolveInternshipBaseCodeForUpdate(request.getBaseCode(), entity.getBaseCode());
        ensureInternshipBaseCodeUnique(code, id);
        String baseName = trimText(request.getBaseName());
        ensureInternshipBaseNameUnique(baseName, id);
        entity.setBaseCode(code);
        entity.setBaseName(baseName);
        entity.setProvince(trimText(request.getProvince()));
        entity.setCity(trimText(request.getCity()));
        entity.setDistrict(trimText(request.getDistrict()));
        entity.setAddress(trimText(request.getAddress()));
        entity.setContactPerson(trimText(request.getContactPerson()));
        entity.setContactPhone(trimText(request.getContactPhone()));
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        internshipBaseMapper.updateById(entity);
        return toInternshipBaseVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseInternshipBaseItemVO updateInternshipBaseStatus(Long id, String status, Long operatorId) {
        BaseInternshipBase entity = requireInternshipBase(id);
        entity.setStatus(normalizeStatus(status));
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        internshipBaseMapper.updateById(entity);
        return toInternshipBaseVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteInternshipBase(Long id, Long operatorId) {
        BaseInternshipBase entity = requireInternshipBaseForDelete(id);
        if (entity.getDeleted() != null && entity.getDeleted() != 0L) {
            return;
        }
        long planBaseCount = planBaseMapper.selectCount(new LambdaQueryWrapper<BizPlanBase>()
                .eq(BizPlanBase::getBaseId, entity.getId())
                .eq(BizPlanBase::getDeleted, 0L));
        long preferenceCount = applicationPreferenceMapper.selectCount(new LambdaQueryWrapper<BizApplicationPreference>()
                .eq(BizApplicationPreference::getBaseId, entity.getId())
                .eq(BizApplicationPreference::getDeleted, 0L));
        if (planBaseCount > 0 || preferenceCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该实习基地已被计划配置或申请志愿引用，不能删除");
        }
        ensureInternshipBaseDeletable(entity.getId());
        entity.setStatus(STATUS_DISABLED);
        entity.setDeleted(entity.getId());
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        internshipBaseMapper.updateById(entity);
    }

    private void ensureDepartmentDeletable(Long id) {
        long childDepartmentCount = departmentMapper.selectCount(new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getParentId, id)
                .eq(BaseDepartment::getDeleted, 0L));
        long majorCount = majorMapper.selectCount(new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getDeptId, id)
                .eq(BaseMajor::getDeleted, 0L));
        long userCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeptId, id)
                .eq(SysUser::getDeleted, 0L));
        long planCount = internshipPlanMapper.selectCount(new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getDeptId, id)
                .eq(BizInternshipPlan::getDeleted, 0L));
        if (childDepartmentCount > 0 || majorCount > 0 || userCount > 0 || planCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该院系已被下级院系、专业、用户或计划引用，不能删除");
        }
    }

    private void ensureMajorDeletable(Long id) {
        long userCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getMajorId, id)
                .eq(SysUser::getDeleted, 0L));
        if (userCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该专业已被用户引用，不能删除");
        }
    }

    private void ensureGradeDeletable(Long id) {
        long userCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getGradeId, id)
                .eq(SysUser::getDeleted, 0L));
        if (userCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该年级已被用户引用，不能删除");
        }
    }

    private void ensureInternshipBaseDeletable(Long id) {
        long assignmentCount = assignmentMapper.selectCount(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getBaseId, id)
                .eq(BizAssignment::getDeleted, 0L));
        if (assignmentCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "该实习基地已被分配记录引用，不能删除");
        }
    }

    private Map<Long, BaseDepartment> queryDepartmentMap(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        List<BaseDepartment> list = departmentMapper.selectList(new LambdaQueryWrapper<BaseDepartment>()
                .in(BaseDepartment::getId, ids)
                .eq(BaseDepartment::getDeleted, 0L));
        return list.stream().collect(Collectors.toMap(BaseDepartment::getId, item -> item, (left, right) -> left));
    }

    private BaseDepartment queryDepartment(Long id) {
        if (id == null) {
            return null;
        }
        return departmentMapper.selectOne(new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getId, id)
                .eq(BaseDepartment::getDeleted, 0L)
                .last("LIMIT 1"));
    }

    private BaseDepartment requireDepartment(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "院系标识不能为空");
        }
        BaseDepartment entity = queryDepartment(id);
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "院系不存在");
        }
        return entity;
    }

    private BaseMajor requireMajor(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "专业标识不能为空");
        }
        BaseMajor entity = majorMapper.selectOne(new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getId, id)
                .eq(BaseMajor::getDeleted, 0L)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "专业不存在");
        }
        return entity;
    }

    private BaseGrade requireGrade(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "年级标识不能为空");
        }
        BaseGrade entity = gradeMapper.selectOne(new LambdaQueryWrapper<BaseGrade>()
                .eq(BaseGrade::getId, id)
                .eq(BaseGrade::getDeleted, 0L)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "年级不存在");
        }
        return entity;
    }

    private BaseInternshipBase requireInternshipBase(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "实习基地标识不能为空");
        }
        BaseInternshipBase entity = internshipBaseMapper.selectOne(new LambdaQueryWrapper<BaseInternshipBase>()
                .eq(BaseInternshipBase::getId, id)
                .eq(BaseInternshipBase::getDeleted, 0L)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "实习基地不存在");
        }
        return entity;
    }

    private BaseDepartment requireDepartmentForDelete(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "院系标识不能为空");
        }
        BaseDepartment entity = departmentMapper.selectOne(new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getId, id)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "院系不存在");
        }
        return entity;
    }

    private BaseMajor requireMajorForDelete(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "专业标识不能为空");
        }
        BaseMajor entity = majorMapper.selectOne(new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getId, id)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "专业不存在");
        }
        return entity;
    }

    private BaseGrade requireGradeForDelete(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "年级标识不能为空");
        }
        BaseGrade entity = gradeMapper.selectOne(new LambdaQueryWrapper<BaseGrade>()
                .eq(BaseGrade::getId, id)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "年级不存在");
        }
        return entity;
    }

    private BaseInternshipBase requireInternshipBaseForDelete(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "实习基地标识不能为空");
        }
        BaseInternshipBase entity = internshipBaseMapper.selectOne(new LambdaQueryWrapper<BaseInternshipBase>()
                .eq(BaseInternshipBase::getId, id)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "实习基地不存在");
        }
        return entity;
    }

    private void ensureDepartmentCodeUnique(String code, Long excludeId) {
        BaseDepartment existing = departmentMapper.selectOne(new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getDeptCode, code)
                .eq(BaseDepartment::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "院系编码已存在");
        }
    }

    private void ensureDepartmentNameUnique(String deptName, Long excludeId) {
        BaseDepartment existing = departmentMapper.selectOne(new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getDeptName, deptName)
                .eq(BaseDepartment::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "院系名称已存在");
        }
    }

    private void ensureMajorCodeUnique(String code, Long excludeId) {
        BaseMajor existing = majorMapper.selectOne(new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getMajorCode, code)
                .eq(BaseMajor::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "专业编码已存在");
        }
    }

    private void ensureMajorNameUnique(String majorName, Long excludeId) {
        BaseMajor existing = majorMapper.selectOne(new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getMajorName, majorName)
                .eq(BaseMajor::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "专业名称已存在");
        }
    }

    private void ensureGradeCodeUnique(String code, Long excludeId) {
        BaseGrade existing = gradeMapper.selectOne(new LambdaQueryWrapper<BaseGrade>()
                .eq(BaseGrade::getGradeCode, code)
                .eq(BaseGrade::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "年级编码已存在");
        }
    }

    private void ensureGradeNameUnique(String gradeName, Long excludeId) {
        BaseGrade existing = gradeMapper.selectOne(new LambdaQueryWrapper<BaseGrade>()
                .eq(BaseGrade::getGradeName, gradeName)
                .eq(BaseGrade::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "年级名称已存在");
        }
    }

    private void ensureInternshipBaseCodeUnique(String code, Long excludeId) {
        BaseInternshipBase existing = internshipBaseMapper.selectOne(new LambdaQueryWrapper<BaseInternshipBase>()
                .eq(BaseInternshipBase::getBaseCode, code)
                .eq(BaseInternshipBase::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "实习基地编码已存在");
        }
    }

    private void ensureInternshipBaseNameUnique(String baseName, Long excludeId) {
        BaseInternshipBase existing = internshipBaseMapper.selectOne(new LambdaQueryWrapper<BaseInternshipBase>()
                .eq(BaseInternshipBase::getBaseName, baseName)
                .eq(BaseInternshipBase::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "实习基地名称已存在");
        }
    }

    private String resolveDepartmentCode(String requestedCode) {
        String code = normalizeCode(requestedCode);
        if (StringUtils.hasText(code)) {
            return code;
        }
        return generateNextCode(DEPARTMENT_CODE_PREFIX, departmentMapper.selectList(new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getDeleted, 0L)
                .select(BaseDepartment::getDeptCode))
                .stream()
                .map(BaseDepartment::getDeptCode)
                .collect(Collectors.toList()));
    }

    private String resolveDepartmentCodeForUpdate(String requestedCode, String currentCode) {
        String code = normalizeCode(requestedCode);
        return StringUtils.hasText(code) ? code : currentCode;
    }

    private String resolveMajorCode(String requestedCode) {
        String code = normalizeCode(requestedCode);
        if (StringUtils.hasText(code)) {
            return code;
        }
        return generateNextCode(MAJOR_CODE_PREFIX, majorMapper.selectList(new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getDeleted, 0L)
                .select(BaseMajor::getMajorCode))
                .stream()
                .map(BaseMajor::getMajorCode)
                .collect(Collectors.toList()));
    }

    private String resolveMajorCodeForUpdate(String requestedCode, String currentCode) {
        String code = normalizeCode(requestedCode);
        return StringUtils.hasText(code) ? code : currentCode;
    }

    private String resolveGradeCode(String requestedCode, String gradeName) {
        String code = normalizeCode(requestedCode);
        if (StringUtils.hasText(code)) {
            return code;
        }
        String yearCode = extractGradeYearCode(gradeName);
        if (StringUtils.hasText(yearCode) && isGradeCodeAvailable(yearCode, null)) {
            return yearCode;
        }
        return generateNextCode(StringUtils.hasText(yearCode) ? yearCode + "_" : GRADE_CODE_PREFIX,
                gradeMapper.selectList(new LambdaQueryWrapper<BaseGrade>()
                        .eq(BaseGrade::getDeleted, 0L)
                        .select(BaseGrade::getGradeCode))
                        .stream()
                        .map(BaseGrade::getGradeCode)
                        .collect(Collectors.toList()));
    }

    private String resolveGradeCodeForUpdate(String requestedCode, String gradeName, String currentCode) {
        String code = normalizeCode(requestedCode);
        if (StringUtils.hasText(code)) {
            return code;
        }
        return StringUtils.hasText(currentCode) ? currentCode : resolveGradeCode(null, gradeName);
    }

    private String resolveInternshipBaseCode(String requestedCode) {
        String code = normalizeCode(requestedCode);
        if (StringUtils.hasText(code)) {
            return code;
        }
        return generateNextCode(BASE_CODE_PREFIX, internshipBaseMapper.selectList(new LambdaQueryWrapper<BaseInternshipBase>()
                .eq(BaseInternshipBase::getDeleted, 0L)
                .select(BaseInternshipBase::getBaseCode))
                .stream()
                .map(BaseInternshipBase::getBaseCode)
                .collect(Collectors.toList()));
    }

    private String resolveInternshipBaseCodeForUpdate(String requestedCode, String currentCode) {
        String code = normalizeCode(requestedCode);
        return StringUtils.hasText(code) ? code : currentCode;
    }

    private boolean isGradeCodeAvailable(String code, Long excludeId) {
        BaseGrade existing = gradeMapper.selectOne(new LambdaQueryWrapper<BaseGrade>()
                .eq(BaseGrade::getGradeCode, code)
                .eq(BaseGrade::getDeleted, 0L)
                .last("LIMIT 1"));
        return existing == null || Objects.equals(existing.getId(), excludeId);
    }

    private String extractGradeYearCode(String gradeName) {
        String normalizedName = trimText(gradeName);
        if (!StringUtils.hasText(normalizedName)) {
            return null;
        }
        Matcher matcher = Pattern.compile("(20\\d{2})").matcher(normalizedName);
        if (matcher.find()) {
            return GRADE_CODE_PREFIX + matcher.group(1);
        }
        return null;
    }

    private String generateNextCode(String prefix, List<String> existingCodes) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(prefix) + "(\\d+)$");
        int maxNumber = 0;
        for (String existingCode : existingCodes) {
            String normalizedCode = normalizeCode(existingCode);
            if (!StringUtils.hasText(normalizedCode)) {
                continue;
            }
            Matcher matcher = pattern.matcher(normalizedCode);
            if (matcher.matches()) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(matcher.group(1)));
            }
        }
        return prefix + String.format(Locale.ROOT, "%03d", maxNumber + 1);
    }

    private BaseDepartmentItemVO toDepartmentVO(BaseDepartment entity) {
        BaseDepartmentItemVO vo = new BaseDepartmentItemVO();
        vo.setId(entity.getId());
        vo.setDeptCode(entity.getDeptCode());
        vo.setDeptName(entity.getDeptName());
        vo.setParentId(entity.getParentId());
        vo.setLeaderName(entity.getLeaderName());
        vo.setContactPhone(entity.getContactPhone());
        vo.setStatus(entity.getStatus());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
    }

    private IdNameOptionVO toDepartmentOption(BaseDepartment entity) {
        IdNameOptionVO vo = new IdNameOptionVO();
        vo.setId(entity.getId());
        vo.setName(entity.getDeptName());
        return vo;
    }

    private BaseMajorItemVO toMajorVO(BaseMajor entity, BaseDepartment dept) {
        BaseMajorItemVO vo = new BaseMajorItemVO();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setDeptName(dept == null ? null : dept.getDeptName());
        vo.setMajorCode(entity.getMajorCode());
        vo.setMajorName(entity.getMajorName());
        vo.setStatus(entity.getStatus());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
    }

    private BaseGradeItemVO toGradeVO(BaseGrade entity) {
        BaseGradeItemVO vo = new BaseGradeItemVO();
        vo.setId(entity.getId());
        vo.setGradeCode(entity.getGradeCode());
        vo.setGradeName(entity.getGradeName());
        vo.setStatus(entity.getStatus());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
    }

    private BaseInternshipBaseItemVO toInternshipBaseVO(BaseInternshipBase entity) {
        BaseInternshipBaseItemVO vo = new BaseInternshipBaseItemVO();
        vo.setId(entity.getId());
        vo.setBaseCode(entity.getBaseCode());
        vo.setBaseName(entity.getBaseName());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setAddress(entity.getAddress());
        vo.setContactPerson(entity.getContactPerson());
        vo.setContactPhone(entity.getContactPhone());
        vo.setStatus(entity.getStatus());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
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

    private String normalizeCode(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeStatus(String text) {
        String value = normalizeCode(text);
        if (STATUS_DISABLED.equals(value)) {
            return STATUS_DISABLED;
        }
        return STATUS_ENABLED;
    }

    private String trimText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }
}
