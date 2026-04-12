package com.teacher.internship.modules.stats.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.teacher.internship.modules.plan.entity.BizInternshipPlan;
import com.teacher.internship.modules.plan.mapper.BizInternshipPlanMapper;
import com.teacher.internship.modules.stats.dto.StatsQueryRequest;
import com.teacher.internship.modules.stats.enums.StatsDimensionType;
import com.teacher.internship.modules.stats.mapper.StatsQueryMapper;
import com.teacher.internship.modules.stats.mapper.model.StatsDimensionRow;
import com.teacher.internship.modules.stats.mapper.model.StatsOverviewRow;
import com.teacher.internship.modules.stats.mapper.model.StatsQueryParam;
import com.teacher.internship.modules.stats.mapper.model.StatsScoreDistributionRow;
import com.teacher.internship.modules.stats.vo.StatsDashboardVO;
import com.teacher.internship.modules.stats.vo.StatsDimensionItemVO;
import com.teacher.internship.modules.stats.vo.StatsDimensionPageVO;
import com.teacher.internship.modules.stats.vo.StatsFilterOptionsVO;
import com.teacher.internship.modules.stats.vo.StatsOptionItemVO;
import com.teacher.internship.modules.stats.vo.StatsOverviewVO;
import com.teacher.internship.modules.stats.vo.StatsScoreDistributionItemVO;
import com.teacher.internship.modules.stats.vo.StatsScoreSummaryVO;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private static final String ROLE_DEPT_ADMIN = "DEPT_ADMIN";
    private static final String ROLE_ACADEMIC_ADMIN = "ACADEMIC_ADMIN";
    private static final String ROLE_SYS_ADMIN = "SYS_ADMIN";
    private static final String STATUS_ENABLED = "ENABLED";
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final StatsQueryMapper statsQueryMapper;
    private final SysUserMapper userMapper;
    private final BaseDepartmentMapper departmentMapper;
    private final BaseMajorMapper majorMapper;
    private final BaseGradeMapper gradeMapper;
    private final BaseInternshipBaseMapper internshipBaseMapper;
    private final BizInternshipPlanMapper planMapper;

    public StatsService(StatsQueryMapper statsQueryMapper,
                        SysUserMapper userMapper,
                        BaseDepartmentMapper departmentMapper,
                        BaseMajorMapper majorMapper,
                        BaseGradeMapper gradeMapper,
                        BaseInternshipBaseMapper internshipBaseMapper,
                        BizInternshipPlanMapper planMapper) {
        this.statsQueryMapper = statsQueryMapper;
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.majorMapper = majorMapper;
        this.gradeMapper = gradeMapper;
        this.internshipBaseMapper = internshipBaseMapper;
        this.planMapper = planMapper;
    }

    public StatsDashboardVO queryDashboard(StatsQueryRequest request,
                                           Long userId,
                                           String roleCode) {
        ScopedQuery scopedQuery = scopeQuery(request, userId, roleCode);
        StatsQueryParam param = toParam(scopedQuery.request, null, null);

        StatsOverviewRow row = statsQueryMapper.selectOverview(param);
        if (row == null) {
            row = new StatsOverviewRow();
        }
        List<StatsScoreDistributionRow> distributionRows = statsQueryMapper.selectScoreDistribution(param);

        StatsDashboardVO result = new StatsDashboardVO();
        result.setOverview(toOverview(row));
        result.setScoreSummary(toScoreSummary(row));
        result.setScoreDistribution(toScoreDistribution(distributionRows));
        return result;
    }

    public List<StatsDimensionItemVO> queryDimensionList(StatsQueryRequest request,
                                                         Integer top,
                                                         Long userId,
                                                         String roleCode) {
        ScopedQuery scopedQuery = scopeQuery(request, userId, roleCode);
        int safeTop = normalizeTop(top);
        StatsQueryParam param = toParam(scopedQuery.request, (long) safeTop, 0L);
        List<StatsDimensionRow> rows = statsQueryMapper.selectDimensionStats(param);
        if (CollectionUtils.isEmpty(rows)) {
            return new ArrayList<>();
        }
        return rows.stream()
                .map(item -> toDimensionItem(item, scopedQuery.request.getDimension()))
                .collect(Collectors.toList());
    }

    public StatsDimensionPageVO queryDimensionPage(StatsQueryRequest request,
                                                   long page,
                                                   long size,
                                                   Long userId,
                                                   String roleCode) {
        ScopedQuery scopedQuery = scopeQuery(request, userId, roleCode);

        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        long offset = (safePage - 1) * safeSize;

        StatsQueryParam countParam = toParam(scopedQuery.request, null, null);
        Long total = statsQueryMapper.countDimensionGroups(countParam);
        if (total == null) {
            total = 0L;
        }

        StatsQueryParam queryParam = toParam(scopedQuery.request, safeSize, offset);
        List<StatsDimensionRow> rows = statsQueryMapper.selectDimensionStats(queryParam);

        StatsDimensionPageVO result = new StatsDimensionPageVO();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal(total);
        if (CollectionUtils.isEmpty(rows)) {
            return result;
        }
        result.setRecords(rows.stream()
                .map(item -> toDimensionItem(item, scopedQuery.request.getDimension()))
                .collect(Collectors.toList()));
        return result;
    }

    public StatsFilterOptionsVO queryFilterOptions(StatsQueryRequest request,
                                                   Long userId,
                                                   String roleCode) {
        ScopedQuery scopedQuery = scopeQuery(request, userId, roleCode);
        StatsQueryRequest scopedRequest = scopedQuery.request;

        StatsFilterOptionsVO result = new StatsFilterOptionsVO();

        LambdaQueryWrapper<BaseDepartment> deptWrapper = new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getDeleted, 0L)
                .eq(BaseDepartment::getStatus, STATUS_ENABLED)
                .orderByAsc(BaseDepartment::getDeptName);
        if (scopedRequest.getDeptId() != null) {
            deptWrapper.eq(BaseDepartment::getId, scopedRequest.getDeptId());
        }
        result.setDepartments(departmentMapper.selectList(deptWrapper).stream()
                .map(this::toDepartmentOption)
                .collect(Collectors.toList()));

        LambdaQueryWrapper<BaseMajor> majorWrapper = new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getDeleted, 0L)
                .eq(BaseMajor::getStatus, STATUS_ENABLED)
                .orderByAsc(BaseMajor::getMajorName);
        if (scopedRequest.getDeptId() != null) {
            majorWrapper.eq(BaseMajor::getDeptId, scopedRequest.getDeptId());
        }
        result.setMajors(majorMapper.selectList(majorWrapper).stream()
                .map(this::toMajorOption)
                .collect(Collectors.toList()));

        result.setGrades(gradeMapper.selectList(new LambdaQueryWrapper<BaseGrade>()
                        .eq(BaseGrade::getDeleted, 0L)
                        .eq(BaseGrade::getStatus, STATUS_ENABLED)
                        .orderByAsc(BaseGrade::getGradeName))
                .stream()
                .map(this::toGradeOption)
                .collect(Collectors.toList()));

        result.setBases(internshipBaseMapper.selectList(new LambdaQueryWrapper<BaseInternshipBase>()
                        .eq(BaseInternshipBase::getDeleted, 0L)
                        .eq(BaseInternshipBase::getStatus, STATUS_ENABLED)
                        .orderByAsc(BaseInternshipBase::getBaseName))
                .stream()
                .map(this::toBaseOption)
                .collect(Collectors.toList()));

        LambdaQueryWrapper<SysUser> teacherWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0L)
                .eq(SysUser::getStatus, STATUS_ENABLED)
                .isNotNull(SysUser::getTeacherNo)
                .orderByAsc(SysUser::getRealName);
        if (scopedRequest.getDeptId() != null) {
            teacherWrapper.eq(SysUser::getDeptId, scopedRequest.getDeptId());
        }
        result.setTeachers(userMapper.selectList(teacherWrapper).stream()
                .map(this::toTeacherOption)
                .collect(Collectors.toList()));

        LambdaQueryWrapper<BizInternshipPlan> planWrapper = new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getDeleted, 0L)
                .orderByDesc(BizInternshipPlan::getCreatedTime)
                .orderByDesc(BizInternshipPlan::getId);
        if (scopedRequest.getDeptId() != null) {
            planWrapper.eq(BizInternshipPlan::getDeptId, scopedRequest.getDeptId());
        }
        result.setPlans(planMapper.selectList(planWrapper).stream()
                .map(this::toPlanOption)
                .collect(Collectors.toList()));

        return result;
    }

    public byte[] exportDimensionStats(StatsQueryRequest request,
                                       Long userId,
                                       String roleCode) {
        ScopedQuery scopedQuery = scopeQuery(request, userId, roleCode);
        StatsQueryParam param = toParam(scopedQuery.request, 5000L, 0L);
        List<StatsDimensionRow> rows = statsQueryMapper.selectDimensionStats(param);
        List<StatsDimensionItemVO> records;
        if (CollectionUtils.isEmpty(rows)) {
            records = Collections.emptyList();
        } else {
            records = rows.stream()
                    .map(item -> toDimensionItem(item, scopedQuery.request.getDimension()))
                    .collect(Collectors.toList());
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("统计分析");
            String[] headers = new String[]{
                    "维度名称",
                    "实习学生数",
                    "材料总数",
                    "已提交材料数",
                    "材料提交率(%)",
                    "逾期材料数",
                    "材料逾期率(%)",
                    "分配总数",
                    "已完成评价数",
                    "评价完成率(%)",
                    "成绩人数",
                    "平均成绩",
                    "优秀率(%)",
                    "及格率(%)"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (StatsDimensionItemVO item : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(defaultText(item.getDimensionName()));
                row.createCell(1).setCellValue(item.getInternshipStudentCount());
                row.createCell(2).setCellValue(item.getMaterialTotalCount());
                row.createCell(3).setCellValue(item.getMaterialSubmittedCount());
                row.createCell(4).setCellValue(toDouble(item.getMaterialSubmitRate()));
                row.createCell(5).setCellValue(item.getMaterialOverdueCount());
                row.createCell(6).setCellValue(toDouble(item.getMaterialOverdueRate()));
                row.createCell(7).setCellValue(item.getAssignmentTotalCount());
                row.createCell(8).setCellValue(item.getEvaluationCompletedCount());
                row.createCell(9).setCellValue(toDouble(item.getEvaluationCompletionRate()));
                row.createCell(10).setCellValue(item.getScoreStudentCount());
                row.createCell(11).setCellValue(toDouble(item.getAverageScore()));
                row.createCell(12).setCellValue(toDouble(item.getExcellentRate()));
                row.createCell(13).setCellValue(toDouble(item.getPassRate()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "导出统计数据失败");
        }
    }

    private ScopedQuery scopeQuery(StatsQueryRequest request, Long userId, String roleCode) {
        SysUser operator = requireUser(userId);
        String normalizedRole = normalizeCode(roleCode);
        if (!ROLE_DEPT_ADMIN.equals(normalizedRole)
                && !ROLE_ACADEMIC_ADMIN.equals(normalizedRole)
                && !ROLE_SYS_ADMIN.equals(normalizedRole)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看统计数据");
        }

        StatsQueryRequest source = request == null ? new StatsQueryRequest() : request;
        StatsQueryRequest scoped = new StatsQueryRequest();
        scoped.setDimension(StatsDimensionType.fromCode(source.getDimension()).getCode());
        scoped.setDeptId(source.getDeptId());
        scoped.setMajorId(source.getMajorId());
        scoped.setGradeId(source.getGradeId());
        scoped.setBaseId(source.getBaseId());
        scoped.setTeacherId(source.getTeacherId());
        scoped.setPlanId(source.getPlanId());
        scoped.setPlanStatus(normalizeStatus(source.getPlanStatus()));

        scoped.setDeptAdminScope(ROLE_DEPT_ADMIN.equals(normalizedRole));

        if (ROLE_DEPT_ADMIN.equals(normalizedRole)) {
            if (operator.getDeptId() == null) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前账号缺少院系范围");
            }
            if (scoped.getDeptId() != null && !Objects.equals(scoped.getDeptId(), operator.getDeptId())) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权访问其他院系的数据");
            }
            scoped.setDeptId(operator.getDeptId());
        }

        ScopedQuery result = new ScopedQuery();
        result.request = scoped;
        result.operator = operator;
        return result;
    }

    private StatsQueryParam toParam(StatsQueryRequest request, Long limit, Long offset) {
        StatsQueryParam param = new StatsQueryParam();
        param.setDimension(StatsDimensionType.fromCode(request.getDimension()).getCode());
        param.setDeptId(request.getDeptId());
        param.setMajorId(request.getMajorId());
        param.setGradeId(request.getGradeId());
        param.setBaseId(request.getBaseId());
        param.setTeacherId(request.getTeacherId());
        param.setPlanId(request.getPlanId());
        param.setPlanStatus(request.getPlanStatus());
        param.setDeptAdminScope(request.getDeptAdminScope());
        param.setLimit(limit);
        param.setOffset(offset);
        return param;
    }

    private StatsOverviewVO toOverview(StatsOverviewRow row) {
        long materialTotal = value(row.getMaterialTotalCount());
        long materialSubmitted = value(row.getMaterialSubmittedCount());
        long materialOverdue = value(row.getMaterialOverdueCount());
        long assignmentTotal = value(row.getAssignmentTotalCount());
        long evaluationCompleted = value(row.getEvaluationCompletedCount());

        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setInternshipStudentCount(value(row.getInternshipStudentCount()));
        vo.setMaterialTotalCount(materialTotal);
        vo.setMaterialSubmittedCount(materialSubmitted);
        vo.setMaterialOverdueCount(materialOverdue);
        vo.setMaterialSubmitRate(rate(materialSubmitted, materialTotal));
        vo.setMaterialOverdueRate(rate(materialOverdue, materialTotal));
        vo.setAssignmentTotalCount(assignmentTotal);
        vo.setEvaluationCompletedCount(evaluationCompleted);
        vo.setEvaluationCompletionRate(rate(evaluationCompleted, assignmentTotal));
        return vo;
    }

    private StatsScoreSummaryVO toScoreSummary(StatsOverviewRow row) {
        long scoreCount = value(row.getScoreStudentCount());
        StatsScoreSummaryVO vo = new StatsScoreSummaryVO();
        vo.setScoreStudentCount(scoreCount);
        vo.setAverageScore(scale(normalizeScore(row.getAverageScore())));
        vo.setExcellentRate(rate(value(row.getExcellentCount()), scoreCount));
        vo.setPassRate(rate(value(row.getPassCount()), scoreCount));
        return vo;
    }

    private List<StatsScoreDistributionItemVO> toScoreDistribution(List<StatsScoreDistributionRow> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return new ArrayList<>();
        }
        List<StatsScoreDistributionItemVO> result = new ArrayList<>();
        for (StatsScoreDistributionRow row : rows) {
            StatsScoreDistributionItemVO item = new StatsScoreDistributionItemVO();
            item.setBucketKey(defaultText(row.getBucketKey()));
            item.setBucketLabel(defaultText(row.getBucketLabel()));
            item.setScoreCount(value(row.getScoreCount()));
            result.add(item);
        }
        return result;
    }

    private StatsDimensionItemVO toDimensionItem(StatsDimensionRow row, String dimensionCode) {
        long materialTotal = value(row.getMaterialTotalCount());
        long materialSubmitted = value(row.getMaterialSubmittedCount());
        long materialOverdue = value(row.getMaterialOverdueCount());
        long assignmentTotal = value(row.getAssignmentTotalCount());
        long evaluationCompleted = value(row.getEvaluationCompletedCount());
        long scoreCount = value(row.getScoreStudentCount());
        long excellentCount = value(row.getExcellentCount());
        long passCount = value(row.getPassCount());

        StatsDimensionItemVO item = new StatsDimensionItemVO();
        item.setDimensionCode(StatsDimensionType.fromCode(dimensionCode).getCode());
        item.setDimensionId(row.getDimensionId());
        item.setDimensionName(StringUtils.hasText(row.getDimensionName()) ? row.getDimensionName() : "未分配");
        item.setInternshipStudentCount(value(row.getInternshipStudentCount()));
        item.setMaterialTotalCount(materialTotal);
        item.setMaterialSubmittedCount(materialSubmitted);
        item.setMaterialOverdueCount(materialOverdue);
        item.setMaterialSubmitRate(rate(materialSubmitted, materialTotal));
        item.setMaterialOverdueRate(rate(materialOverdue, materialTotal));
        item.setAssignmentTotalCount(assignmentTotal);
        item.setEvaluationCompletedCount(evaluationCompleted);
        item.setEvaluationCompletionRate(rate(evaluationCompleted, assignmentTotal));
        item.setScoreStudentCount(scoreCount);
        item.setAverageScore(scale(normalizeScore(row.getAverageScore())));
        item.setExcellentRate(rate(excellentCount, scoreCount));
        item.setPassRate(rate(passCount, scoreCount));
        return item;
    }

    private StatsOptionItemVO toDepartmentOption(BaseDepartment entity) {
        StatsOptionItemVO item = new StatsOptionItemVO();
        item.setId(entity.getId());
        item.setName(entity.getDeptName());
        return item;
    }

    private StatsOptionItemVO toMajorOption(BaseMajor entity) {
        StatsOptionItemVO item = new StatsOptionItemVO();
        item.setId(entity.getId());
        item.setName(entity.getMajorName());
        return item;
    }

    private StatsOptionItemVO toGradeOption(BaseGrade entity) {
        StatsOptionItemVO item = new StatsOptionItemVO();
        item.setId(entity.getId());
        item.setName(entity.getGradeName());
        return item;
    }

    private StatsOptionItemVO toBaseOption(BaseInternshipBase entity) {
        StatsOptionItemVO item = new StatsOptionItemVO();
        item.setId(entity.getId());
        item.setName(entity.getBaseName());
        return item;
    }

    private StatsOptionItemVO toTeacherOption(SysUser entity) {
        StatsOptionItemVO item = new StatsOptionItemVO();
        item.setId(entity.getId());
        String teacherNo = StringUtils.hasText(entity.getTeacherNo()) ? entity.getTeacherNo() : "-";
        String name = StringUtils.hasText(entity.getRealName())
                ? entity.getRealName()
                : (StringUtils.hasText(entity.getTeacherNo()) ? entity.getTeacherNo() : entity.getLoginName());
        item.setName(name + " (" + teacherNo + ")");
        return item;
    }

    private StatsOptionItemVO toPlanOption(BizInternshipPlan entity) {
        StatsOptionItemVO item = new StatsOptionItemVO();
        item.setId(entity.getId());
        item.setName(defaultText(entity.getPlanName()) + " (" + defaultText(entity.getPlanCode()) + ")");
        return item;
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

    private String normalizeCode(String text) {
        return text == null ? "" : text.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeStatus(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim().toUpperCase(Locale.ROOT);
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

    private int normalizeTop(Integer top) {
        if (top == null || top <= 0) {
            return 20;
        }
        return Math.min(top, 200);
    }

    private long value(Long number) {
        return number == null ? 0L : number;
    }

    private BigDecimal normalizeScore(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(long numerator, long denominator) {
        if (denominator <= 0 || numerator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(HUNDRED)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0D : value.doubleValue();
    }

    private String defaultText(String text) {
        return text == null ? "" : text;
    }

    private static class ScopedQuery {
        private StatsQueryRequest request;
        private SysUser operator;
    }
}
