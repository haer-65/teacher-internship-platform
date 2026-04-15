package com.teacher.internship.modules.stats.mapper.provider;

import com.teacher.internship.modules.stats.mapper.model.StatsQueryParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsSqlProvider {

    private static final String DIMENSION_DEPARTMENT = "DEPARTMENT";
    private static final String DIMENSION_SUMMARY = "SUMMARY";
    private static final String DIMENSION_MAJOR = "MAJOR";
    private static final String DIMENSION_GRADE = "GRADE";
    private static final String DIMENSION_BASE = "BASE";
    private static final String DIMENSION_TEACHER = "TEACHER";
    private static final String DIMENSION_PLAN = "PLAN";

    public String selectOverview(Map<String, Object> params) {
        StatsQueryParam p = queryParam(params);
        StringBuilder sql = new StringBuilder();
        sql.append("WITH assignment_scope AS (");
        sql.append(buildOverviewScopeSql(p));
        sql.append("), scope_assignment AS (");
        sql.append(" SELECT DISTINCT assignment_id FROM assignment_scope ");
        sql.append("), material_agg AS (");
        sql.append(" SELECT m.assignment_id AS assignment_id,");
        sql.append("        COUNT(1) AS material_total_count,");
        sql.append("        SUM(CASE WHEN m.material_status = 'SUBMITTED' THEN 1 ELSE 0 END) AS material_submitted_count,");
        sql.append("        SUM(CASE WHEN t.deadline_time IS NOT NULL");
        sql.append("                  AND #{p.now} > t.deadline_time");
        sql.append("                  AND COALESCE(m.latest_version_no, 0) <= 0");
        sql.append("                  AND NOT (COALESCE(c.open_flag, 0) = 1 AND (c.open_until IS NULL OR #{p.now} <= c.open_until))");
        sql.append("             THEN 1 ELSE 0 END) AS material_overdue_count");
        sql.append("   FROM biz_material m");
        sql.append("   JOIN biz_material_type t ON t.id = m.material_type_id AND t.deleted = 0");
        sql.append("   LEFT JOIN biz_material_resubmit_control c ON c.material_id = m.id AND c.deleted = 0");
        sql.append("   JOIN scope_assignment sa ON sa.assignment_id = m.assignment_id");
        sql.append("  WHERE m.deleted = 0");
        sql.append("  GROUP BY m.assignment_id");
        sql.append("), evaluation_agg AS (");
        sql.append(" SELECT s.assignment_id AS assignment_id,");
        sql.append("        CASE");
        sql.append("          WHEN EXISTS (");
        sql.append("            SELECT 1 FROM biz_evaluation e");
        sql.append("             WHERE e.assignment_id = s.assignment_id");
        sql.append("               AND e.evaluation_type = 'FINAL'");
        sql.append("               AND e.evaluator_id = s.inner_teacher_id");
        sql.append("               AND e.deleted = 0");
        sql.append("          )");
        sql.append("          AND EXISTS (");
        sql.append("            SELECT 1 FROM biz_evaluation e");
        sql.append("             WHERE e.assignment_id = s.assignment_id");
        sql.append("               AND e.evaluation_type = 'FINAL'");
        sql.append("               AND e.evaluator_id = s.base_teacher_id");
        sql.append("               AND e.deleted = 0");
        sql.append("          ) THEN 1 ELSE 0");
        sql.append("        END AS evaluation_completed_flag");
        sql.append("   FROM assignment_scope s");
        sql.append("), score_agg AS (");
        sql.append(" SELECT ss.assignment_id AS assignment_id, ss.total_score AS total_score");
        sql.append("   FROM biz_score_sheet ss");
        sql.append("   JOIN scope_assignment sa ON sa.assignment_id = ss.assignment_id");
        sql.append("  WHERE ss.deleted = 0");
        sql.append("    AND ss.status = 'PUBLISHED'");
        sql.append(")");
        sql.append(" SELECT");
        sql.append("   COUNT(DISTINCT s.student_id) AS internship_student_count,");
        sql.append("   COALESCE(SUM(COALESCE(ma.material_total_count, 0)), 0) AS material_total_count,");
        sql.append("   COALESCE(SUM(COALESCE(ma.material_submitted_count, 0)), 0) AS material_submitted_count,");
        sql.append("   COALESCE(SUM(COALESCE(ma.material_overdue_count, 0)), 0) AS material_overdue_count,");
        sql.append("   COUNT(DISTINCT s.assignment_id) AS assignment_total_count,");
        sql.append("   COALESCE(SUM(COALESCE(ea.evaluation_completed_flag, 0)), 0) AS evaluation_completed_count,");
        sql.append("   COUNT(sa.total_score) AS score_student_count,");
        sql.append("   AVG(sa.total_score) AS average_score,");
        sql.append("   COALESCE(SUM(CASE WHEN sa.total_score >= 90 THEN 1 ELSE 0 END), 0) AS excellent_count,");
        sql.append("   COALESCE(SUM(CASE WHEN sa.total_score >= 60 THEN 1 ELSE 0 END), 0) AS pass_count");
        sql.append(" FROM assignment_scope s");
        sql.append(" LEFT JOIN material_agg ma ON ma.assignment_id = s.assignment_id");
        sql.append(" LEFT JOIN evaluation_agg ea ON ea.assignment_id = s.assignment_id");
        sql.append(" LEFT JOIN score_agg sa ON sa.assignment_id = s.assignment_id");
        return sql.toString();
    }

    public String selectScoreDistribution(Map<String, Object> params) {
        StatsQueryParam p = queryParam(params);
        StringBuilder sql = new StringBuilder();
        sql.append("WITH assignment_scope AS (");
        sql.append(buildOverviewScopeSql(p));
        sql.append("), scope_assignment AS (");
        sql.append(" SELECT DISTINCT assignment_id FROM assignment_scope ");
        sql.append("), score_agg AS (");
        sql.append(" SELECT ss.assignment_id AS assignment_id, ss.total_score AS total_score");
        sql.append("   FROM biz_score_sheet ss");
        sql.append("   JOIN scope_assignment sa ON sa.assignment_id = ss.assignment_id");
        sql.append("  WHERE ss.deleted = 0");
        sql.append("    AND ss.status = 'PUBLISHED'");
        sql.append(")");
        sql.append(" SELECT 'S90_100' AS bucket_key, '90-100' AS bucket_label,");
        sql.append("        COALESCE(SUM(CASE WHEN total_score >= 90 THEN 1 ELSE 0 END), 0) AS score_count");
        sql.append("   FROM score_agg");
        sql.append(" UNION ALL");
        sql.append(" SELECT 'S80_89' AS bucket_key, '80-89' AS bucket_label,");
        sql.append("        COALESCE(SUM(CASE WHEN total_score >= 80 AND total_score < 90 THEN 1 ELSE 0 END), 0) AS score_count");
        sql.append("   FROM score_agg");
        sql.append(" UNION ALL");
        sql.append(" SELECT 'S70_79' AS bucket_key, '70-79' AS bucket_label,");
        sql.append("        COALESCE(SUM(CASE WHEN total_score >= 70 AND total_score < 80 THEN 1 ELSE 0 END), 0) AS score_count");
        sql.append("   FROM score_agg");
        sql.append(" UNION ALL");
        sql.append(" SELECT 'S60_69' AS bucket_key, '60-69' AS bucket_label,");
        sql.append("        COALESCE(SUM(CASE WHEN total_score >= 60 AND total_score < 70 THEN 1 ELSE 0 END), 0) AS score_count");
        sql.append("   FROM score_agg");
        sql.append(" UNION ALL");
        sql.append(" SELECT 'S0_59' AS bucket_key, '0-59' AS bucket_label,");
        sql.append("        COALESCE(SUM(CASE WHEN total_score < 60 THEN 1 ELSE 0 END), 0) AS score_count");
        sql.append("   FROM score_agg");
        return sql.toString();
    }

    public String selectDimensionStats(Map<String, Object> params) {
        StatsQueryParam p = queryParam(params);
        StringBuilder sql = new StringBuilder();
        sql.append(buildDimensionStatsBaseSql(p));
        sql.append(" SELECT");
        sql.append("   s.dimension_id AS dimension_id,");
        sql.append("   s.dimension_name AS dimension_name,");
        sql.append("   COUNT(DISTINCT s.student_id) AS internship_student_count,");
        sql.append("   COALESCE(SUM(COALESCE(ma.material_total_count, 0)), 0) AS material_total_count,");
        sql.append("   COALESCE(SUM(COALESCE(ma.material_submitted_count, 0)), 0) AS material_submitted_count,");
        sql.append("   COALESCE(SUM(COALESCE(ma.material_overdue_count, 0)), 0) AS material_overdue_count,");
        sql.append("   COUNT(DISTINCT s.assignment_id) AS assignment_total_count,");
        sql.append("   COALESCE(SUM(COALESCE(ea.evaluation_completed_flag, 0)), 0) AS evaluation_completed_count,");
        sql.append("   COUNT(sa.total_score) AS score_student_count,");
        sql.append("   AVG(sa.total_score) AS average_score,");
        sql.append("   COALESCE(SUM(CASE WHEN sa.total_score >= 90 THEN 1 ELSE 0 END), 0) AS excellent_count,");
        sql.append("   COALESCE(SUM(CASE WHEN sa.total_score >= 60 THEN 1 ELSE 0 END), 0) AS pass_count");
        sql.append(" FROM assignment_scope s");
        sql.append(" LEFT JOIN material_agg ma ON ma.assignment_id = s.assignment_id");
        sql.append(" LEFT JOIN evaluation_agg ea ON ea.assignment_id = s.assignment_id AND (ea.dimension_id <=> s.dimension_id)");
        sql.append(" LEFT JOIN score_agg sa ON sa.assignment_id = s.assignment_id");
        sql.append(" GROUP BY s.dimension_id, s.dimension_name");
        sql.append(" ORDER BY internship_student_count DESC, s.dimension_name ASC");
        if (p.getLimit() != null && p.getLimit() > 0) {
            sql.append(" LIMIT #{p.limit}");
            if (p.getOffset() != null && p.getOffset() >= 0) {
                sql.append(" OFFSET #{p.offset}");
            }
        }
        return sql.toString();
    }

    public String countDimensionGroups(Map<String, Object> params) {
        StatsQueryParam p = queryParam(params);
        StringBuilder sql = new StringBuilder();
        sql.append("WITH assignment_scope AS (");
        sql.append(buildDimensionScopeSql(p));
        sql.append(")");
        sql.append(" SELECT COUNT(1)");
        sql.append("   FROM (");
        sql.append("         SELECT s.dimension_id, s.dimension_name");
        sql.append("           FROM assignment_scope s");
        sql.append("          GROUP BY s.dimension_id, s.dimension_name");
        sql.append("        ) t");
        return sql.toString();
    }

    private String buildDimensionStatsBaseSql(StatsQueryParam p) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH assignment_scope AS (");
        sql.append(buildDimensionScopeSql(p));
        sql.append("), scope_assignment AS (");
        sql.append(" SELECT DISTINCT assignment_id FROM assignment_scope ");
        sql.append("), material_agg AS (");
        sql.append(" SELECT m.assignment_id AS assignment_id,");
        sql.append("        COUNT(1) AS material_total_count,");
        sql.append("        SUM(CASE WHEN m.material_status = 'SUBMITTED' THEN 1 ELSE 0 END) AS material_submitted_count,");
        sql.append("        SUM(CASE WHEN t.deadline_time IS NOT NULL");
        sql.append("                  AND #{p.now} > t.deadline_time");
        sql.append("                  AND COALESCE(m.latest_version_no, 0) <= 0");
        sql.append("                  AND NOT (COALESCE(c.open_flag, 0) = 1 AND (c.open_until IS NULL OR #{p.now} <= c.open_until))");
        sql.append("             THEN 1 ELSE 0 END) AS material_overdue_count");
        sql.append("   FROM biz_material m");
        sql.append("   JOIN biz_material_type t ON t.id = m.material_type_id AND t.deleted = 0");
        sql.append("   LEFT JOIN biz_material_resubmit_control c ON c.material_id = m.id AND c.deleted = 0");
        sql.append("   JOIN scope_assignment sa ON sa.assignment_id = m.assignment_id");
        sql.append("  WHERE m.deleted = 0");
        sql.append("  GROUP BY m.assignment_id");
        sql.append("), evaluation_agg AS (");
        sql.append(" SELECT s.assignment_id AS assignment_id,");
        sql.append("        s.dimension_id AS dimension_id,");
        if (isTeacherDimension(p)) {
            sql.append("        CASE");
            sql.append("          WHEN EXISTS (");
            sql.append("            SELECT 1 FROM biz_evaluation e");
            sql.append("             WHERE e.assignment_id = s.assignment_id");
            sql.append("               AND e.evaluation_type = 'FINAL'");
            sql.append("               AND e.evaluator_id = s.dimension_id");
            sql.append("               AND e.deleted = 0");
            sql.append("          ) THEN 1 ELSE 0");
            sql.append("        END AS evaluation_completed_flag");
        } else {
            sql.append("        CASE");
            sql.append("          WHEN EXISTS (");
            sql.append("            SELECT 1 FROM biz_evaluation e");
            sql.append("             WHERE e.assignment_id = s.assignment_id");
            sql.append("               AND e.evaluation_type = 'FINAL'");
            sql.append("               AND e.evaluator_id = s.inner_teacher_id");
            sql.append("               AND e.deleted = 0");
            sql.append("          )");
            sql.append("          AND EXISTS (");
            sql.append("            SELECT 1 FROM biz_evaluation e");
            sql.append("             WHERE e.assignment_id = s.assignment_id");
            sql.append("               AND e.evaluation_type = 'FINAL'");
            sql.append("               AND e.evaluator_id = s.base_teacher_id");
            sql.append("               AND e.deleted = 0");
            sql.append("          ) THEN 1 ELSE 0");
            sql.append("        END AS evaluation_completed_flag");
        }
        sql.append("   FROM assignment_scope s");
        sql.append("), score_agg AS (");
        sql.append(" SELECT ss.assignment_id AS assignment_id, ss.total_score AS total_score");
        sql.append("   FROM biz_score_sheet ss");
        sql.append("   JOIN scope_assignment sa ON sa.assignment_id = ss.assignment_id");
        sql.append("  WHERE ss.deleted = 0");
        sql.append("    AND ss.status = 'PUBLISHED'");
        sql.append(")");
        return sql.toString();
    }

    private String buildOverviewScopeSql(StatsQueryParam p) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT");
        sql.append("   a.id AS assignment_id,");
        sql.append("   a.plan_id AS plan_id,");
        sql.append("   a.student_id AS student_id,");
        sql.append("   a.inner_teacher_id AS inner_teacher_id,");
        sql.append("   a.base_teacher_id AS base_teacher_id");
        sql.append("   FROM biz_assignment a");
        sql.append("   JOIN biz_internship_plan p ON p.id = a.plan_id AND p.deleted = 0");
        sql.append("   JOIN sys_user stu ON stu.id = a.student_id AND stu.deleted = 0");
        sql.append("  WHERE a.deleted = 0 AND a.is_current = 1");
        appendScopeFilters(sql, p, false);
        return sql.toString();
    }

    private String buildDimensionScopeSql(StatsQueryParam p) {
        StringBuilder sql = new StringBuilder();
        if (isTeacherDimension(p)) {
            sql.append(" SELECT");
            sql.append("   a.id AS assignment_id,");
            sql.append("   a.plan_id AS plan_id,");
            sql.append("   a.student_id AS student_id,");
            sql.append("   a.inner_teacher_id AS inner_teacher_id,");
            sql.append("   a.base_teacher_id AS base_teacher_id,");
            sql.append("   tr.teacher_id AS dimension_id,");
            sql.append("   COALESCE(t.real_name, CONCAT('Teacher#', tr.teacher_id)) AS dimension_name");
            sql.append("   FROM biz_assignment a");
            sql.append("   JOIN biz_internship_plan p ON p.id = a.plan_id AND p.deleted = 0");
            sql.append("   JOIN sys_user stu ON stu.id = a.student_id AND stu.deleted = 0");
            sql.append("   JOIN (");
            sql.append("         SELECT a1.id AS assignment_id, a1.inner_teacher_id AS teacher_id");
            sql.append("           FROM biz_assignment a1");
            sql.append("          WHERE a1.deleted = 0 AND a1.is_current = 1 AND a1.inner_teacher_id IS NOT NULL");
            sql.append("         UNION");
            sql.append("         SELECT a2.id AS assignment_id, a2.base_teacher_id AS teacher_id");
            sql.append("           FROM biz_assignment a2");
            sql.append("          WHERE a2.deleted = 0 AND a2.is_current = 1 AND a2.base_teacher_id IS NOT NULL");
            sql.append("        ) tr ON tr.assignment_id = a.id");
            sql.append("   LEFT JOIN sys_user t ON t.id = tr.teacher_id AND t.deleted = 0");
            sql.append("  WHERE a.deleted = 0 AND a.is_current = 1");
            appendScopeFilters(sql, p, true);
            return sql.toString();
        }

        sql.append(" SELECT");
        sql.append("   a.id AS assignment_id,");
        sql.append("   a.plan_id AS plan_id,");
        sql.append("   a.student_id AS student_id,");
        sql.append("   a.inner_teacher_id AS inner_teacher_id,");
        sql.append("   a.base_teacher_id AS base_teacher_id,");
        sql.append("   ").append(resolveDimensionIdExpr(p)).append(" AS dimension_id,");
        sql.append("   ").append(resolveDimensionNameExpr(p)).append(" AS dimension_name");
        sql.append("   FROM biz_assignment a");
        sql.append("   JOIN biz_internship_plan p ON p.id = a.plan_id AND p.deleted = 0");
        sql.append("   JOIN sys_user stu ON stu.id = a.student_id AND stu.deleted = 0");
        sql.append("   LEFT JOIN base_department d ON d.id = p.dept_id AND d.deleted = 0");
        sql.append("   LEFT JOIN base_major m ON m.id = stu.major_id AND m.deleted = 0");
        sql.append("   LEFT JOIN base_grade g ON g.id = stu.grade_id AND g.deleted = 0");
        sql.append("   LEFT JOIN base_internship_base b ON b.id = a.base_id AND b.deleted = 0");
        sql.append("  WHERE a.deleted = 0 AND a.is_current = 1");
        appendScopeFilters(sql, p, false);
        return sql.toString();
    }

    private void appendScopeFilters(StringBuilder sql, StatsQueryParam p, boolean teacherDimension) {
        if (p.getDeptId() != null) {
            sql.append(" AND p.dept_id = #{p.deptId}");
        }
        if (p.getMajorId() != null) {
            sql.append(" AND stu.major_id = #{p.majorId}");
        }
        if (p.getGradeId() != null) {
            sql.append(" AND stu.grade_id = #{p.gradeId}");
        }
        if (p.getBaseId() != null) {
            sql.append(" AND a.base_id = #{p.baseId}");
        }
        if (p.getTeacherId() != null) {
            if (teacherDimension) {
                sql.append(" AND tr.teacher_id = #{p.teacherId}");
            } else {
                sql.append(" AND (a.inner_teacher_id = #{p.teacherId} OR a.base_teacher_id = #{p.teacherId})");
            }
        }
        if (p.getPlanId() != null) {
            sql.append(" AND a.plan_id = #{p.planId}");
        }
        if (p.getPlanStatus() != null && !p.getPlanStatus().trim().isEmpty()) {
            sql.append(" AND p.plan_status = #{p.planStatus}");
        }
    }

    private String resolveDimensionIdExpr(StatsQueryParam p) {
        String dimension = normalizeDimension(p.getDimension());
        if (DIMENSION_SUMMARY.equals(dimension)) {
            return "0";
        }
        if (DIMENSION_MAJOR.equals(dimension)) {
            return "stu.major_id";
        }
        if (DIMENSION_GRADE.equals(dimension)) {
            return "stu.grade_id";
        }
        if (DIMENSION_BASE.equals(dimension)) {
            return "a.base_id";
        }
        if (DIMENSION_PLAN.equals(dimension)) {
            return "p.id";
        }
        return "p.dept_id";
    }

    private String resolveDimensionNameExpr(StatsQueryParam p) {
        String dimension = normalizeDimension(p.getDimension());
        if (DIMENSION_SUMMARY.equals(dimension)) {
            return resolveSummaryDimensionNameExpr(p);
        }
        if (DIMENSION_MAJOR.equals(dimension)) {
            return "COALESCE(m.major_name, '未分配')";
        }
        if (DIMENSION_GRADE.equals(dimension)) {
            return "COALESCE(g.grade_name, '未分配')";
        }
        if (DIMENSION_BASE.equals(dimension)) {
            return "COALESCE(b.base_name, '未分配')";
        }
        if (DIMENSION_PLAN.equals(dimension)) {
            return "CONCAT(COALESCE(p.plan_name, '-'), ' (', COALESCE(p.plan_code, '-'), ')')";
        }
        return "COALESCE(d.dept_name, '未分配')";
    }

    private String resolveSummaryDimensionNameExpr(StatsQueryParam p) {
        boolean deptAdminScope = isDeptAdminScope(p);
        if (deptAdminScope) {
            List<String> parts = new ArrayList<>();
            if (p.getMajorId() != null) {
                parts.add("COALESCE(m.major_name, 'æœªåˆ†é…')");
            }
            if (p.getGradeId() != null) {
                parts.add("COALESCE(g.grade_name, 'æœªåˆ†é…')");
            }
            if (p.getBaseId() != null) {
                parts.add("COALESCE(b.base_name, 'æœªåˆ†é…')");
            }
            if (p.getTeacherId() != null) {
                parts.add(resolveTeacherNameExpr());
            }
            if (p.getPlanId() != null) {
                parts.add("CONCAT(COALESCE(p.plan_name, '-'), ' (', COALESCE(p.plan_code, '-'), ')')");
            }
            if (p.getPlanStatus() != null && !p.getPlanStatus().trim().isEmpty()) {
                parts.add(resolvePlanStatusLabelExpr(p.getPlanStatus().trim().toUpperCase(Locale.ROOT)));
            }
            if (parts.isEmpty()) {
                return "COALESCE(d.dept_name, 'æœªåˆ†é…')";
            }
            return "CONCAT(COALESCE(d.dept_name, 'æœªåˆ†é…'), ' / ', CONCAT_WS(' / ', " + String.join(", ", parts) + "))";
        }
        List<String> parts = new ArrayList<>();
        if (p.getDeptId() != null) {
            parts.add("COALESCE(d.dept_name, '未分配')");
        }
        if (p.getMajorId() != null) {
            parts.add("COALESCE(m.major_name, '未分配')");
        }
        if (p.getGradeId() != null) {
            parts.add("COALESCE(g.grade_name, '未分配')");
        }
        if (p.getBaseId() != null) {
            parts.add("COALESCE(b.base_name, '未分配')");
        }
        if (p.getTeacherId() != null) {
            parts.add(resolveTeacherNameExpr());
        }
        if (p.getPlanId() != null) {
            parts.add("CONCAT(COALESCE(p.plan_name, '-'), ' (', COALESCE(p.plan_code, '-'), ')')");
        }
        if (p.getPlanStatus() != null && !p.getPlanStatus().trim().isEmpty()) {
            parts.add(resolvePlanStatusLabelExpr(p.getPlanStatus().trim().toUpperCase(Locale.ROOT)));
        }
        if (parts.isEmpty()) {
            return "'全校范围'";
        }
        return "CONCAT('全校范围 / ', CONCAT_WS(' / ', " + String.join(", ", parts) + "))";
    }

    private String resolveTeacherNameExpr() {
        return "COALESCE(("
                + "SELECT NULLIF(CONCAT_WS(' ', COALESCE(u.real_name, ''), "
                + "CASE WHEN COALESCE(u.teacher_no, '') = '' THEN '' ELSE CONCAT('(', u.teacher_no, ')') END), '') "
                + "FROM sys_user u WHERE u.id = #{p.teacherId} AND u.deleted = 0 LIMIT 1"
                + "), CONCAT('Teacher#', #{p.teacherId}))";
    }

    private String resolvePlanStatusLabelExpr(String planStatus) {
        if ("DRAFT".equals(planStatus)) {
            return "'草稿'";
        }
        if ("PUBLISHED".equals(planStatus)) {
            return "'已发布'";
        }
        if ("FINISHED".equals(planStatus)) {
            return "'已结束'";
        }
        if ("ARCHIVED".equals(planStatus)) {
            return "'已归档'";
        }
        return "COALESCE(p.plan_status, '未分配')";
    }

    private boolean isTeacherDimension(StatsQueryParam p) {
        return DIMENSION_TEACHER.equals(normalizeDimension(p.getDimension()));
    }

    private boolean isDeptAdminScope(StatsQueryParam p) {
        return Boolean.TRUE.equals(p.getDeptAdminScope());
    }

    private String normalizeDimension(String dimension) {
        if (dimension == null) {
            return DIMENSION_DEPARTMENT;
        }
        String value = dimension.trim().toUpperCase(Locale.ROOT);
        if (DIMENSION_SUMMARY.equals(value)
                || DIMENSION_MAJOR.equals(value)
                || DIMENSION_GRADE.equals(value)
                || DIMENSION_BASE.equals(value)
                || DIMENSION_TEACHER.equals(value)
                || DIMENSION_PLAN.equals(value)) {
            return value;
        }
        return DIMENSION_DEPARTMENT;
    }

    private StatsQueryParam queryParam(Map<String, Object> params) {
        Object value = params.get("p");
        if (value instanceof StatsQueryParam) {
            return (StatsQueryParam) value;
        }
        return new StatsQueryParam();
    }
}
