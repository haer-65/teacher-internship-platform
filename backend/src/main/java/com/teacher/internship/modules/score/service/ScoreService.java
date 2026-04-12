package com.teacher.internship.modules.score.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.application.entity.BizAssignment;
import com.teacher.internship.modules.application.mapper.BizAssignmentMapper;
import com.teacher.internship.modules.base.entity.BaseDepartment;
import com.teacher.internship.modules.base.entity.BaseGrade;
import com.teacher.internship.modules.base.entity.BaseMajor;
import com.teacher.internship.modules.base.mapper.BaseDepartmentMapper;
import com.teacher.internship.modules.base.mapper.BaseGradeMapper;
import com.teacher.internship.modules.base.mapper.BaseMajorMapper;
import com.teacher.internship.modules.evaluation.entity.BizEvaluation;
import com.teacher.internship.modules.evaluation.mapper.BizEvaluationMapper;
import com.teacher.internship.modules.notice.service.NoticeTriggerService;
import com.teacher.internship.modules.plan.entity.BizInternshipPlan;
import com.teacher.internship.modules.plan.mapper.BizInternshipPlanMapper;
import com.teacher.internship.modules.score.dto.ScoreAdjustRequest;
import com.teacher.internship.modules.score.entity.BizScoreSheet;
import com.teacher.internship.modules.score.mapper.BizScoreSheetMapper;
import com.teacher.internship.modules.score.vo.ScoreDetailSnapshotVO;
import com.teacher.internship.modules.score.vo.ScoreDetailVO;
import com.teacher.internship.modules.score.vo.ScoreListItemVO;
import com.teacher.internship.modules.score.vo.ScoreMaterialDetailVO;
import com.teacher.internship.modules.score.vo.ScorePageVO;
import com.teacher.internship.modules.score.vo.ScorePublishPreviewVO;
import com.teacher.internship.modules.score.vo.ScorePublishResultVO;
import com.teacher.internship.modules.score.vo.ScoreRecalculateResultVO;
import com.teacher.internship.modules.system.entity.SysOperationLog;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.mapper.SysOperationLogMapper;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import com.teacher.internship.modules.system.vo.IdNameOptionVO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private static final Logger log = LoggerFactory.getLogger(ScoreService.class);

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_INNER_TEACHER = "INNER_TEACHER";
    private static final String ROLE_BASE_TEACHER = "BASE_TEACHER";
    private static final String ROLE_DEPT_ADMIN = "DEPT_ADMIN";
    private static final String ROLE_ACADEMIC_ADMIN = "ACADEMIC_ADMIN";
    private static final String ROLE_SYS_ADMIN = "SYS_ADMIN";

    private static final String SCORE_STATUS_DRAFT = "DRAFT";
    private static final String SCORE_STATUS_PUBLISHED = "PUBLISHED";
    private static final String PLAN_SCORE_UNPUBLISHED = "UNPUBLISHED";
    private static final String PLAN_SCORE_PUBLISHED = "PUBLISHED";
    private static final String PLAN_STATUS_FINISHED = "FINISHED";
    private static final String PLAN_STATUS_ARCHIVED = "ARCHIVED";
    private static final String TYPE_FINAL = "FINAL";

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final List<String> CHINESE_FONT_CANDIDATES = List.of(
            "C:/Windows/Fonts/simhei.ttf",
            "C:/Windows/Fonts/msyh.ttf",
            "C:/Windows/Fonts/NotoSansSC-VF.ttf"
    );

    private final BizScoreSheetMapper scoreSheetMapper;
    private final BizInternshipPlanMapper planMapper;
    private final BizAssignmentMapper assignmentMapper;
    private final BizEvaluationMapper evaluationMapper;
    private final SysUserMapper userMapper;
    private final BaseDepartmentMapper departmentMapper;
    private final BaseMajorMapper majorMapper;
    private final BaseGradeMapper gradeMapper;
    private final SysOperationLogMapper operationLogMapper;
    private final ScoreCalculationService scoreCalculationService;
    private final ObjectMapper objectMapper;
    private final NoticeTriggerService noticeTriggerService;

    public ScoreService(BizScoreSheetMapper scoreSheetMapper,
                        BizInternshipPlanMapper planMapper,
                        BizAssignmentMapper assignmentMapper,
                        BizEvaluationMapper evaluationMapper,
                        SysUserMapper userMapper,
                        BaseDepartmentMapper departmentMapper,
                        BaseMajorMapper majorMapper,
                        BaseGradeMapper gradeMapper,
                        SysOperationLogMapper operationLogMapper,
                        ScoreCalculationService scoreCalculationService,
                        ObjectMapper objectMapper,
                        NoticeTriggerService noticeTriggerService) {
        this.scoreSheetMapper = scoreSheetMapper;
        this.planMapper = planMapper;
        this.assignmentMapper = assignmentMapper;
        this.evaluationMapper = evaluationMapper;
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.majorMapper = majorMapper;
        this.gradeMapper = gradeMapper;
        this.operationLogMapper = operationLogMapper;
        this.scoreCalculationService = scoreCalculationService;
        this.objectMapper = objectMapper;
        this.noticeTriggerService = noticeTriggerService;
    }

    public ScorePageVO queryScorePage(long page,
                                      long size,
                                      String keyword,
                                      Long planId,
                                      String studentNo,
                                      String studentName,
                                      String status,
                                      Long userId,
                                      String roleCode) {
        SysUser operator = requireUser(userId);
        String normalizedRole = normalizeCode(roleCode);
        List<BizScoreSheet> scopedSheets = queryScopedScoreSheets(
                operator,
                normalizedRole,
                planId,
                studentNo,
                studentName,
                status,
                null
        );
        return buildScorePage(scopedSheets, page, size, keyword);
    }

    public ScorePageVO queryStudentPublishedPage(long page,
                                                 long size,
                                                 String keyword,
                                                 Long planId,
                                                 Long userId,
                                                 String roleCode) {
        SysUser student = requireUser(userId);
        String normalizedRole = normalizeCode(roleCode);
        if (!ROLE_STUDENT.equals(normalizedRole)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色必须是师范生");
        }
        List<BizScoreSheet> scopedSheets = queryScopedScoreSheets(
                student,
                normalizedRole,
                planId,
                null,
                null,
                SCORE_STATUS_PUBLISHED,
                null
        );
        return buildScorePage(scopedSheets, page, size, keyword);
    }

    public List<IdNameOptionVO> queryVisiblePlanOptions(Long userId, String roleCode) {
        SysUser operator = requireUser(userId);
        String normalizedRole = normalizeCode(roleCode);
        List<BizScoreSheet> scopedSheets = queryScopedScoreSheets(
                operator,
                normalizedRole,
                null,
                null,
                null,
                null,
                null
        );
        if (CollectionUtils.isEmpty(scopedSheets)) {
            return new ArrayList<>();
        }

        Set<Long> planIds = scopedSheets.stream()
                .map(BizScoreSheet::getPlanId)
                .collect(Collectors.toSet());
        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);
        return planIds.stream()
                .map(planMap::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(BizInternshipPlan::getCreatedTime, Comparator.nullsLast(LocalDateTime::compareTo))
                        .thenComparing(BizInternshipPlan::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toIdNameOption)
                .collect(Collectors.toList());
    }

    public ScoreDetailVO getScoreDetail(Long scoreSheetId,
                                        Long userId,
                                        String roleCode) {
        BizScoreSheet scoreSheet = requireScoreSheet(scoreSheetId);
        SysUser operator = requireUser(userId);
        BizInternshipPlan plan = requirePlan(scoreSheet.getPlanId());
        ensureScoreViewAccess(scoreSheet, plan, operator, normalizeCode(roleCode));
        return buildScoreDetail(scoreSheet, plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public ScoreRecalculateResultVO recalculateByPlan(Long planId,
                                                      Long userId,
                                                      String roleCode) {
        SysUser operator = requireUser(userId);
        BizInternshipPlan plan = requirePlan(planId);
        ensureScoreManageAccess(plan, operator, normalizeCode(roleCode));
        ensurePlanCanCalculate(plan);
        ensureFinalEvaluationReadyForPublish(plan);
        ensurePlanCanRecalculate(plan);
        return doRecalculate(plan, operator.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public long backfillArchivedPlanDrafts() {
        List<BizInternshipPlan> archivedPlans = planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getDeleted, 0L)
                .eq(BizInternshipPlan::getPlanStatus, PLAN_STATUS_ARCHIVED)
                .orderByAsc(BizInternshipPlan::getId));

        if (CollectionUtils.isEmpty(archivedPlans)) {
            return 0L;
        }

        Long operatorId = resolveBackfillOperatorId();
        long generatedSheets = 0L;
        for (BizInternshipPlan plan : archivedPlans) {
            long existingCount = scoreSheetMapper.selectCount(new LambdaQueryWrapper<BizScoreSheet>()
                    .eq(BizScoreSheet::getPlanId, plan.getId())
                    .eq(BizScoreSheet::getDeleted, 0L));
            if (existingCount > 0) {
                continue;
            }

            try {
                ScoreRecalculateResultVO result = doRecalculate(plan, operatorId);
                generatedSheets += result.getGeneratedSheets();
            } catch (Exception ex) {
                log.warn("Archived plan score backfill skipped, planId={}, planCode={}", plan.getId(), plan.getPlanCode(), ex);
            }
        }
        return generatedSheets;
    }

    public long backfillFinishedPlanDrafts() {
        List<BizInternshipPlan> finishedPlans = planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getDeleted, 0L)
                .eq(BizInternshipPlan::getPlanStatus, PLAN_STATUS_FINISHED)
                .orderByAsc(BizInternshipPlan::getId));

        if (CollectionUtils.isEmpty(finishedPlans)) {
            return 0L;
        }

        Long operatorId = resolveBackfillOperatorId();
        long generatedSheets = 0L;
        for (BizInternshipPlan plan : finishedPlans) {
            long existingCount = scoreSheetMapper.selectCount(new LambdaQueryWrapper<BizScoreSheet>()
                    .eq(BizScoreSheet::getPlanId, plan.getId())
                    .eq(BizScoreSheet::getDeleted, 0L));
            if (existingCount > 0) {
                continue;
            }
            if (!checkFinalEvaluationReady(plan).canPublish) {
                continue;
            }

            try {
                ScoreRecalculateResultVO result = doRecalculate(plan, operatorId);
                generatedSheets += result.getGeneratedSheets();
            } catch (Exception ex) {
                log.warn("Finished plan score backfill skipped, planId={}, planCode={}", plan.getId(), plan.getPlanCode(), ex);
            }
        }
        return generatedSheets;
    }

    public ScorePublishPreviewVO previewPublish(Long planId,
                                                Integer recalculate,
                                                Long userId,
                                                String roleCode) {
        SysUser operator = requireUser(userId);
        BizInternshipPlan plan = requirePlan(planId);
        ensureScoreManageAccess(plan, operator, normalizeCode(roleCode));
        ensurePlanCanCalculate(plan);
        PublishReadiness readiness = checkFinalEvaluationReady(plan);

        if (readiness.canPublish && (recalculate == null || recalculate == 1)) {
            ensurePlanCanRecalculate(plan);
            doRecalculate(plan, operator.getId());
        }

        List<BizScoreSheet> draftSheets = scoreSheetMapper.selectList(new LambdaQueryWrapper<BizScoreSheet>()
                .eq(BizScoreSheet::getPlanId, planId)
                .eq(BizScoreSheet::getDeleted, 0L)
                .eq(BizScoreSheet::getStatus, SCORE_STATUS_DRAFT)
                .orderByDesc(BizScoreSheet::getTotalScore)
                .orderByDesc(BizScoreSheet::getId));

        ScorePublishPreviewVO preview = new ScorePublishPreviewVO();
        preview.setPlanId(plan.getId());
        preview.setPlanCode(plan.getPlanCode());
        preview.setPlanName(plan.getPlanName());
        preview.setPlanStatus(plan.getPlanStatus());
        preview.setScorePublishStatus(plan.getScorePublishStatus());
        preview.setCanPublish(readiness.canPublish);
        preview.setPublishBlockedReason(readiness.blockedReason);
        preview.setTotal(draftSheets.size());
        preview.setRecords(buildScoreListItems(draftSheets));
        return preview;
    }

    @Transactional(rollbackFor = Exception.class)
    public ScorePublishResultVO publishByPlan(Long planId,
                                              Long userId,
                                              String roleCode) {
        SysUser operator = requireUser(userId);
        BizInternshipPlan plan = requirePlan(planId);
        ensureScoreManageAccess(plan, operator, normalizeCode(roleCode));
        ensurePlanCanCalculate(plan);
        ensureFinalEvaluationReadyForPublish(plan);

        List<BizScoreSheet> draftSheets = scoreSheetMapper.selectList(new LambdaQueryWrapper<BizScoreSheet>()
                .eq(BizScoreSheet::getPlanId, planId)
                .eq(BizScoreSheet::getDeleted, 0L)
                .eq(BizScoreSheet::getStatus, SCORE_STATUS_DRAFT));
        if (CollectionUtils.isEmpty(draftSheets)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "没有可发布的草稿成绩单");
        }

        LocalDateTime now = LocalDateTime.now();
        for (BizScoreSheet sheet : draftSheets) {
            sheet.setStatus(SCORE_STATUS_PUBLISHED);
            sheet.setPublishedBy(operator.getId());
            sheet.setPublishedTime(now);
            sheet.setUpdatedBy(operator.getId());
            sheet.setUpdatedTime(now);
            scoreSheetMapper.updateById(sheet);
        }

        plan.setScorePublishStatus(PLAN_SCORE_PUBLISHED);
        plan.setUpdatedBy(operator.getId());
        plan.setUpdatedTime(now);
        planMapper.updateById(plan);
        noticeTriggerService.notifyScorePublished(plan, draftSheets, operator.getId());

        ScorePublishResultVO result = new ScorePublishResultVO();
        result.setPlanId(plan.getId());
        result.setPlanCode(plan.getPlanCode());
        result.setPlanName(plan.getPlanName());
        result.setPublishedCount(draftSheets.size());
        result.setPublishedTime(now);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public ScoreDetailVO adjustScore(Long scoreSheetId,
                                     ScoreAdjustRequest request,
                                     Long userId,
                                     String roleCode) {
        BizScoreSheet scoreSheet = requireScoreSheet(scoreSheetId);
        SysUser operator = requireUser(userId);
        BizInternshipPlan plan = requirePlan(scoreSheet.getPlanId());
        ensureScoreManageAccess(plan, operator, normalizeCode(roleCode));
        ensureAdjustAllowed(scoreSheet);

        BigDecimal oldScore = normalizeScore(scoreSheet.getTotalScore());
        BigDecimal newScore = validateAndNormalizeScore(request.getNewTotalScore());
        String reason = trimToNull(request.getAdjustReason());
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "调整原因不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        scoreSheet.setTotalScore(newScore);
        scoreSheet.setAdjustedFlag(1);
        scoreSheet.setAdjustReason(reason);
        scoreSheet.setAdjustBy(operator.getId());
        scoreSheet.setAdjustTime(now);
        scoreSheet.setAdjustTimes(scoreSheet.getAdjustTimes() == null ? 1 : scoreSheet.getAdjustTimes() + 1);
        scoreSheet.setUpdatedBy(operator.getId());
        scoreSheet.setUpdatedTime(now);
        scoreSheetMapper.updateById(scoreSheet);

        writeAdjustAuditLog(scoreSheet, operator, oldScore, newScore, reason, now);
        noticeTriggerService.notifyScoreAdjusted(scoreSheet, plan, operator.getId(), oldScore, newScore, reason);
        return buildScoreDetail(requireScoreSheet(scoreSheet.getId()), plan);
    }

    private void ensureAdjustAllowed(BizScoreSheet scoreSheet) {
        if (scoreSheet == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "成绩单不存在");
        }
        boolean published = SCORE_STATUS_PUBLISHED.equals(scoreSheet.getStatus());
        boolean alreadyAdjustedAfterPublish = scoreSheet.getAdjustedFlag() != null && scoreSheet.getAdjustedFlag() >= 1;
        if (published && alreadyAdjustedAfterPublish) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "成绩已发布后仅允许调整一次");
        }
    }

    public byte[] exportExcel(Long planId,
                              String status,
                              Long userId,
                              String roleCode) {
        SysUser operator = requireUser(userId);
        List<BizScoreSheet> sheets = queryScopedScoreSheets(
                operator,
                normalizeCode(roleCode),
                planId,
                null,
                null,
                status,
                null
        );
        List<ScoreListItemVO> records = buildScoreListItems(sheets);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("成绩单");
            String[] headers = new String[]{
                    "计划编码",
                    "计划名称",
                    "学号",
                    "学生姓名",
                    "学院",
                    "专业",
                    "年级",
                    "过程分",
                    "综合分",
                    "自动总分",
                    "总分",
                    "状态",
                    "已调整",
                    "调整次数",
                    "调整原因",
                    "发布时间"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (ScoreListItemVO record : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(defaultText(record.getPlanCode()));
                row.createCell(1).setCellValue(defaultText(record.getPlanName()));
                row.createCell(2).setCellValue(defaultText(record.getStudentNo()));
                row.createCell(3).setCellValue(defaultText(record.getStudentName()));
                row.createCell(4).setCellValue(defaultText(record.getDeptName()));
                row.createCell(5).setCellValue(defaultText(record.getMajorName()));
                row.createCell(6).setCellValue(defaultText(record.getGradeName()));
                row.createCell(7).setCellValue(toDouble(record.getProcessScore()));
                row.createCell(8).setCellValue(toDouble(record.getFinalScore()));
                row.createCell(9).setCellValue(toDouble(record.getAutoTotalScore()));
                row.createCell(10).setCellValue(toDouble(record.getTotalScore()));
                row.createCell(11).setCellValue(defaultText(record.getStatus()));
                row.createCell(12).setCellValue(Objects.equals(record.getAdjustedFlag(), 1) ? "是" : "否");
                row.createCell(13).setCellValue(record.getAdjustTimes() == null ? 0 : record.getAdjustTimes());
                row.createCell(14).setCellValue(defaultText(record.getAdjustReason()));
                row.createCell(15).setCellValue(record.getPublishedTime() == null ? "" : record.getPublishedTime().toString());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "导出成绩表失败");
        }
    }

    public byte[] exportPdf(Long scoreSheetId,
                            Long userId,
                            String roleCode) {
        ScoreDetailVO detail = getScoreDetail(scoreSheetId, userId, roleCode);

        List<String> lines = new ArrayList<>();
        lines.add("师范生教育实习成绩单");
        lines.add("");
        lines.add("计划：" + defaultText(detail.getPlanName()) + "（" + defaultText(detail.getPlanCode()) + "）");
        lines.add("学生：" + defaultText(detail.getStudentName()) + "（" + defaultText(detail.getStudentNo()) + "）");
        lines.add("学院：" + defaultText(detail.getDeptName()) + "   专业：" + defaultText(detail.getMajorName()));
        lines.add("年级：" + defaultText(detail.getGradeName()));
        lines.add("");
        lines.add("过程分：" + toPlain(detail.getProcessScore()));
        lines.add("综合分：" + toPlain(detail.getFinalScore()));
        lines.add("自动总分：" + toPlain(detail.getAutoTotalScore()));
        lines.add("总分：" + toPlain(detail.getTotalScore()));
        lines.add("状态：" + defaultText(detail.getStatus()));
        lines.add("是否调整：" + (Objects.equals(detail.getAdjustedFlag(), 1) ? "是" : "否"));
        lines.add("调整原因：" + defaultText(detail.getAdjustReason()));
        lines.add("");
        lines.add("材料明细：");
        for (ScoreMaterialDetailVO material : detail.getMaterialDetails()) {
            lines.add("- " + defaultText(material.getMaterialTypeName())
                    + " | 权重=" + toPlain(material.getMaterialWeight())
                    + " | 校内分=" + toPlain(material.getInnerTeacherProcessScore())
                    + " | 基地分=" + toPlain(material.getBaseTeacherProcessScore())
                    + " | 合成分=" + toPlain(material.getMaterialCompositeScore())
                    + " | 折算贡献=" + toPlain(material.getWeightedContribution()));
        }
        lines.add("");
        lines.add("计算公式：");
        lines.add(defaultText(detail.getFormulaText()));

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writePdfLines(document, lines);
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "导出成绩单失败");
        }
    }
    private void writePdfLines(PDDocument document, List<String> lines) throws IOException {
        final float margin = 40f;
        final float lineHeight = 15f;
        final float pageHeight = PDRectangle.A4.getHeight();
        final float pageWidth = PDRectangle.A4.getWidth();
        final float maxLineWidth = pageWidth - margin * 2;
        final float startY = pageHeight - margin;
        final float minY = margin;
        final float fontSize = 11f;
        final PDFont font = loadChineseFont(document);

        List<String> renderLines = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                renderLines.add("");
                continue;
            }
            renderLines.addAll(wrapPdfText(line, font, fontSize, maxLineWidth));
        }

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(document, page);
        stream.setFont(font, fontSize);
        stream.beginText();
        stream.newLineAtOffset(margin, startY);
        float currentY = startY;

        for (String line : renderLines) {
            if (currentY <= minY) {
                stream.endText();
                stream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                stream = new PDPageContentStream(document, page);
                stream.setFont(font, fontSize);
                stream.beginText();
                stream.newLineAtOffset(margin, startY);
                currentY = startY;
            }
            stream.showText(line);
            stream.newLineAtOffset(0, -lineHeight);
            currentY -= lineHeight;
        }

        stream.endText();
        stream.close();
    }

    private PDFont loadChineseFont(PDDocument document) throws IOException {
        for (String candidate : CHINESE_FONT_CANDIDATES) {
            File fontFile = new File(candidate);
            if (fontFile.isFile()) {
                return PDType0Font.load(document, fontFile);
            }
        }
        return PDType0Font.load(document, new File(CHINESE_FONT_CANDIDATES.get(0)));
    }

    private List<String> wrapPdfText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> wrapped = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            String next = current.toString() + ch;
            float width = font.getStringWidth(next) / 1000f * fontSize;
            if (width > maxWidth && current.length() > 0) {
                wrapped.add(current.toString());
                current.setLength(0);
            }
            current.append(ch);
        }
        if (current.length() > 0) {
            wrapped.add(current.toString());
        }
        if (wrapped.isEmpty()) {
            wrapped.add("");
        }
        return wrapped;
    }
    private ScoreRecalculateResultVO doRecalculate(BizInternshipPlan plan, Long operatorId) {
        List<BizAssignment> assignments = assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getPlanId, plan.getId())
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L));

        List<BizScoreSheet> existingSheets = scoreSheetMapper.selectList(new LambdaQueryWrapper<BizScoreSheet>()
                .eq(BizScoreSheet::getPlanId, plan.getId())
                .eq(BizScoreSheet::getDeleted, 0L));
        Map<Long, BizScoreSheet> existingByStudent = existingSheets.stream()
                .collect(Collectors.toMap(BizScoreSheet::getStudentId, item -> item, (left, right) -> left));

        LocalDateTime now = LocalDateTime.now();
        long generatedCount = 0;
        Set<Long> currentStudentIds = new HashSet<>();

        for (BizAssignment assignment : assignments) {
            currentStudentIds.add(assignment.getStudentId());
            ScoreDetailSnapshotVO snapshot = scoreCalculationService.calculate(plan, assignment);
            String detailJson = serializeSnapshot(snapshot);

            BizScoreSheet sheet = existingByStudent.get(assignment.getStudentId());
            if (sheet == null) {
                sheet = new BizScoreSheet();
                sheet.setPlanId(plan.getId());
                sheet.setStudentId(assignment.getStudentId());
                sheet.setCreatedBy(operatorId);
                sheet.setDeleted(0L);
                sheet.setAdjustTimes(0);
            }

            sheet.setAssignmentId(assignment.getId());
            sheet.setProcessScore(normalizeScore(snapshot.getProcessScore()));
            sheet.setFinalScore(normalizeScore(snapshot.getFinalScore()));
            sheet.setAutoTotalScore(normalizeScore(snapshot.getAutoTotalScore()));
            sheet.setTotalScore(normalizeScore(snapshot.getAutoTotalScore()));
            sheet.setDetailJson(detailJson);
            sheet.setLastCalcTime(now);
            sheet.setStatus(SCORE_STATUS_DRAFT);
            sheet.setAdjustedFlag(0);
            sheet.setAdjustReason(null);
            sheet.setAdjustBy(null);
            sheet.setAdjustTime(null);
            sheet.setAdjustTimes(0);
            sheet.setPublishedBy(null);
            sheet.setPublishedTime(null);
            sheet.setUpdatedBy(operatorId);
            sheet.setUpdatedTime(now);

            if (sheet.getId() == null) {
                scoreSheetMapper.insert(sheet);
            } else {
                scoreSheetMapper.updateById(sheet);
            }
            generatedCount++;
        }

        long deleteMark = System.currentTimeMillis();
        for (BizScoreSheet stale : existingSheets) {
            if (currentStudentIds.contains(stale.getStudentId())) {
                continue;
            }
            stale.setDeleted(deleteMark);
            stale.setUpdatedBy(operatorId);
            stale.setUpdatedTime(now);
            scoreSheetMapper.updateById(stale);
        }

        plan.setScorePublishStatus(PLAN_SCORE_UNPUBLISHED);
        plan.setUpdatedBy(operatorId);
        plan.setUpdatedTime(now);
        planMapper.updateById(plan);

        ScoreRecalculateResultVO result = new ScoreRecalculateResultVO();
        result.setPlanId(plan.getId());
        result.setPlanCode(plan.getPlanCode());
        result.setPlanName(plan.getPlanName());
        result.setTotalAssignments(assignments.size());
        result.setGeneratedSheets(generatedCount);
        return result;
    }

    private List<BizScoreSheet> queryScopedScoreSheets(SysUser operator,
                                                       String normalizedRole,
                                                       Long planId,
                                                       String studentNo,
                                                       String studentName,
                                                       String status,
                                                       String forcedStatus) {
        LambdaQueryWrapper<BizScoreSheet> wrapper = new LambdaQueryWrapper<BizScoreSheet>()
                .eq(BizScoreSheet::getDeleted, 0L)
                .orderByDesc(BizScoreSheet::getUpdatedTime)
                .orderByDesc(BizScoreSheet::getId);

        Set<Long> scopePlanIds = null;
        boolean teacherScopeOnlyPublished = false;
        if (ROLE_STUDENT.equals(normalizedRole)) {
            wrapper.eq(BizScoreSheet::getStudentId, operator.getId());
            wrapper.eq(BizScoreSheet::getStatus, SCORE_STATUS_PUBLISHED);
        } else if (ROLE_INNER_TEACHER.equals(normalizedRole) || ROLE_BASE_TEACHER.equals(normalizedRole)) {
            List<BizAssignment> assignmentList = queryCurrentAssignmentsByTeacher(operator.getId(), normalizedRole);
            if (CollectionUtils.isEmpty(assignmentList)) {
                return new ArrayList<>();
            }
            Set<Long> assignmentIds = assignmentList.stream().map(BizAssignment::getId).collect(Collectors.toSet());
            wrapper.in(BizScoreSheet::getAssignmentId, assignmentIds);
            teacherScopeOnlyPublished = true;
        } else if (ROLE_DEPT_ADMIN.equals(normalizedRole)) {
            scopePlanIds = queryPlanIdsByDept(operator.getDeptId());
            if (CollectionUtils.isEmpty(scopePlanIds)) {
                return new ArrayList<>();
            }
            wrapper.in(BizScoreSheet::getPlanId, scopePlanIds);
        } else if (ROLE_ACADEMIC_ADMIN.equals(normalizedRole)) {
            teacherScopeOnlyPublished = true;
        } else if (ROLE_SYS_ADMIN.equals(normalizedRole)) {
            // system level can view all
        } else {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看成绩数据");
        }

        if (teacherScopeOnlyPublished) {
            wrapper.eq(BizScoreSheet::getStatus, SCORE_STATUS_PUBLISHED);
        }

        if (planId != null) {
            if (scopePlanIds != null && !scopePlanIds.contains(planId)) {
                return new ArrayList<>();
            }
            wrapper.eq(BizScoreSheet::getPlanId, planId);
        }

        String normalizedStatus = StringUtils.hasText(forcedStatus) ? normalizeCode(forcedStatus) : normalizeCode(status);
        if (StringUtils.hasText(normalizedStatus)) {
            wrapper.eq(BizScoreSheet::getStatus, normalizedStatus);
        }

        if (StringUtils.hasText(studentNo)) {
            SysUser student = findStudentByStudentNo(studentNo.trim());
            if (student == null) {
                return new ArrayList<>();
            }
            wrapper.eq(BizScoreSheet::getStudentId, student.getId());
        }

        if (StringUtils.hasText(studentName)) {
            Set<Long> studentIds = queryStudentIdsByName(studentName.trim());
            if (CollectionUtils.isEmpty(studentIds)) {
                return new ArrayList<>();
            }
            wrapper.in(BizScoreSheet::getStudentId, studentIds);
        }

        return scoreSheetMapper.selectList(wrapper);
    }

    private ScorePageVO buildScorePage(List<BizScoreSheet> source,
                                       long page,
                                       long size,
                                       String keyword) {
        List<ScoreListItemVO> allRecords = buildScoreListItems(source);
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim().toLowerCase(Locale.ROOT);
            allRecords = allRecords.stream()
                    .filter(item -> contains(item.getPlanCode(), value)
                            || contains(item.getPlanName(), value)
                            || contains(item.getStudentNo(), value)
                            || contains(item.getStudentName(), value)
                            || contains(item.getDeptName(), value)
                            || contains(item.getMajorName(), value)
                            || contains(item.getGradeName(), value))
                    .collect(Collectors.toList());
        }

        allRecords.sort(Comparator.comparing(ScoreListItemVO::getTotalScore, Comparator.nullsLast(BigDecimal::compareTo))
                .reversed()
                .thenComparing(ScoreListItemVO::getId, Comparator.nullsLast(Long::compareTo).reversed()));

        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        ScorePageVO result = new ScorePageVO();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal(allRecords.size());
        result.setRecords(slice(allRecords, safePage, safeSize));
        return result;
    }

    private IdNameOptionVO toIdNameOption(BizInternshipPlan plan) {
        IdNameOptionVO option = new IdNameOptionVO();
        option.setId(plan.getId());
        option.setName(plan.getPlanName() + " (" + plan.getPlanCode() + ")");
        return option;
    }

    private List<ScoreListItemVO> buildScoreListItems(List<BizScoreSheet> sheets) {
        if (CollectionUtils.isEmpty(sheets)) {
            return new ArrayList<>();
        }

        Set<Long> planIds = sheets.stream().map(BizScoreSheet::getPlanId).collect(Collectors.toSet());
        Set<Long> studentIds = sheets.stream().map(BizScoreSheet::getStudentId).collect(Collectors.toSet());
        Set<Long> operatorIds = sheets.stream()
                .flatMap(item -> {
                    List<Long> ids = new ArrayList<>();
                    if (item.getAdjustBy() != null) {
                        ids.add(item.getAdjustBy());
                    }
                    if (item.getPublishedBy() != null) {
                        ids.add(item.getPublishedBy());
                    }
                    return ids.stream();
                })
                .collect(Collectors.toSet());

        Map<Long, BizInternshipPlan> planMap = queryPlanMap(planIds);
        Map<Long, SysUser> studentMap = queryUserMap(studentIds);
        Map<Long, SysUser> operatorMap = queryUserMap(operatorIds);

        Set<Long> deptIds = studentMap.values().stream().map(SysUser::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> majorIds = studentMap.values().stream().map(SysUser::getMajorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> gradeIds = studentMap.values().stream().map(SysUser::getGradeId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, BaseDepartment> deptMap = queryDeptMap(deptIds);
        Map<Long, BaseMajor> majorMap = queryMajorMap(majorIds);
        Map<Long, BaseGrade> gradeMap = queryGradeMap(gradeIds);

        List<ScoreListItemVO> result = new ArrayList<>();
        for (BizScoreSheet sheet : sheets) {
            BizInternshipPlan plan = planMap.get(sheet.getPlanId());
            SysUser student = studentMap.get(sheet.getStudentId());
            if (plan == null || student == null) {
                continue;
            }
            ScoreListItemVO item = new ScoreListItemVO();
            item.setId(sheet.getId());
            item.setPlanId(sheet.getPlanId());
            item.setPlanCode(plan.getPlanCode());
            item.setPlanName(plan.getPlanName());
            item.setAssignmentId(sheet.getAssignmentId());

            item.setStudentId(student.getId());
            item.setStudentNo(student.getStudentNo());
            item.setStudentName(student.getRealName());
            item.setDeptName(resolveDeptName(student.getDeptId(), deptMap));
            item.setMajorName(resolveMajorName(student.getMajorId(), majorMap));
            item.setGradeName(resolveGradeName(student.getGradeId(), gradeMap));

            item.setProcessScore(normalizeScore(sheet.getProcessScore()));
            item.setFinalScore(normalizeScore(sheet.getFinalScore()));
            item.setAutoTotalScore(normalizeScore(sheet.getAutoTotalScore()));
            item.setTotalScore(normalizeScore(sheet.getTotalScore()));
            item.setStatus(sheet.getStatus());
            item.setAdjustedFlag(sheet.getAdjustedFlag());
            item.setAdjustTimes(sheet.getAdjustTimes());
            item.setAdjustReason(sheet.getAdjustReason());
            item.setAdjustTime(sheet.getAdjustTime());
            item.setPublishedTime(sheet.getPublishedTime());
            item.setLastCalcTime(sheet.getLastCalcTime());

            SysUser adjustByUser = operatorMap.get(sheet.getAdjustBy());
            if (adjustByUser != null) {
                item.setAdjustByName(adjustByUser.getRealName());
            }
            result.add(item);
        }
        return result;
    }

    private ScoreDetailVO buildScoreDetail(BizScoreSheet scoreSheet, BizInternshipPlan plan) {
        SysUser student = requireUser(scoreSheet.getStudentId());
        BizAssignment assignment = requireAssignment(scoreSheet.getAssignmentId());

        ScoreDetailSnapshotVO snapshot = parseSnapshot(scoreSheet.getDetailJson());
        if (snapshot == null) {
            snapshot = scoreCalculationService.calculate(plan, assignment);
        }

        ScoreDetailVO detail = new ScoreDetailVO();
        detail.setId(scoreSheet.getId());
        detail.setPlanId(plan.getId());
        detail.setPlanCode(plan.getPlanCode());
        detail.setPlanName(plan.getPlanName());
        detail.setAssignmentId(scoreSheet.getAssignmentId());
        detail.setStudentId(student.getId());
        detail.setStudentNo(student.getStudentNo());
        detail.setStudentName(student.getRealName());

        Map<Long, BaseDepartment> deptMap = queryDeptMap(singletonSet(student.getDeptId()));
        Map<Long, BaseMajor> majorMap = queryMajorMap(singletonSet(student.getMajorId()));
        Map<Long, BaseGrade> gradeMap = queryGradeMap(singletonSet(student.getGradeId()));
        detail.setDeptName(resolveDeptName(student.getDeptId(), deptMap));
        detail.setMajorName(resolveMajorName(student.getMajorId(), majorMap));
        detail.setGradeName(resolveGradeName(student.getGradeId(), gradeMap));

        detail.setInnerTeacherWeight(normalizeScore(snapshot.getInnerTeacherWeight()));
        detail.setBaseTeacherWeight(normalizeScore(snapshot.getBaseTeacherWeight()));
        detail.setInnerTeacherFinalScore(normalizeScore(snapshot.getInnerTeacherFinalScore()));
        detail.setBaseTeacherFinalScore(normalizeScore(snapshot.getBaseTeacherFinalScore()));
        detail.setProcessScore(normalizeScore(scoreSheet.getProcessScore()));
        detail.setFinalScore(normalizeScore(scoreSheet.getFinalScore()));
        detail.setAutoTotalScore(normalizeScore(scoreSheet.getAutoTotalScore()));
        detail.setTotalScore(normalizeScore(scoreSheet.getTotalScore()));
        detail.setStatus(scoreSheet.getStatus());
        detail.setAdjustedFlag(scoreSheet.getAdjustedFlag());
        detail.setAdjustTimes(scoreSheet.getAdjustTimes());
        detail.setAdjustReason(scoreSheet.getAdjustReason());
        detail.setAdjustBy(scoreSheet.getAdjustBy());
        detail.setAdjustTime(scoreSheet.getAdjustTime());
        detail.setPublishedBy(scoreSheet.getPublishedBy());
        detail.setPublishedTime(scoreSheet.getPublishedTime());
        detail.setLastCalcTime(scoreSheet.getLastCalcTime());
        detail.setMaterialDetails(snapshot.getMaterialDetails() == null
                ? new ArrayList<>()
                : snapshot.getMaterialDetails());
        detail.setFormulaText("总分 = (过程分 + 综合分) / 2；"
                + "过程分 = Σ(材料合成分 × 材料权重%)；"
                + "材料合成分 = 校内过程分 × 校内权重% + 基地过程分 × 基地权重%；"
                + "综合分 = 校内综合分 × 校内权重% + 基地综合分 × 基地权重%");

        Map<Long, SysUser> operators = queryUserMap(multiSet(scoreSheet.getAdjustBy(), scoreSheet.getPublishedBy()));
        SysUser adjustBy = operators.get(scoreSheet.getAdjustBy());
        SysUser publishedBy = operators.get(scoreSheet.getPublishedBy());
        if (adjustBy != null) {
            detail.setAdjustByName(adjustBy.getRealName());
        }
        if (publishedBy != null) {
            detail.setPublishedByName(publishedBy.getRealName());
        }
        return detail;
    }

    private void writeAdjustAuditLog(BizScoreSheet scoreSheet,
                                     SysUser operator,
                                     BigDecimal oldScore,
                                     BigDecimal newScore,
                                     String reason,
                                     LocalDateTime operateTime) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scoreSheetId", scoreSheet.getId());
        payload.put("planId", scoreSheet.getPlanId());
        payload.put("studentId", scoreSheet.getStudentId());
        payload.put("oldScore", toPlain(oldScore));
        payload.put("newScore", toPlain(newScore));
        payload.put("adjustReason", reason);
        payload.put("adjustTimes", scoreSheet.getAdjustTimes());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            payloadJson = "{\"error\":\"序列化失败\"}";
        }

        SysOperationLog log = new SysOperationLog();
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getRealName());
        log.setModuleCode("SCORE");
        log.setActionCode("ADJUST");
        log.setBusinessType("SCORE_SHEET");
        log.setBusinessId(scoreSheet.getId());
        log.setRequestMethod("POST");
        log.setRequestUri("/api/v1/score/adjust/" + scoreSheet.getId());
        log.setRequestParams(payloadJson);
        log.setResponseData(payloadJson);
        log.setOperationStatus("SUCCESS");
        log.setOperateTime(operateTime);
        log.setCreatedBy(operator.getId());
        log.setUpdatedBy(operator.getId());
        log.setDeleted(0L);
        operationLogMapper.insert(log);
    }

    private void ensurePlanCanCalculate(BizInternshipPlan plan) {
        String status = normalizeCode(plan.getPlanStatus());
        if (!PLAN_STATUS_FINISHED.equals(status) && !PLAN_STATUS_ARCHIVED.equals(status)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "只有计划结束或归档后才可以计算成绩");
        }
    }

    private void ensureFinalEvaluationReadyForPublish(BizInternshipPlan plan) {
        PublishReadiness readiness = checkFinalEvaluationReady(plan);
        if (!readiness.canPublish) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), readiness.blockedReason);
        }
    }

    private void ensurePlanCanRecalculate(BizInternshipPlan plan) {
        if (PLAN_SCORE_PUBLISHED.equals(normalizeCode(plan.getScorePublishStatus()))) {
            throw new BusinessException(
                    ApiCode.BAD_REQUEST.getCode(),
                    "该计划的成绩已发布，为避免覆盖已发布结果，暂不允许重新计算"
            );
        }
    }

    private void ensureScoreManageAccess(BizInternshipPlan plan, SysUser operator, String normalizedRole) {
        if (ROLE_SYS_ADMIN.equals(normalizedRole)) {
            return;
        }
        if (ROLE_DEPT_ADMIN.equals(normalizedRole) && Objects.equals(plan.getDeptId(), operator.getDeptId())) {
            return;
        }
        throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权管理该计划的成绩");
    }

    private PublishReadiness checkFinalEvaluationReady(BizInternshipPlan plan) {
        PublishReadiness readiness = new PublishReadiness();
        if (plan == null) {
            readiness.canPublish = false;
            readiness.blockedReason = "计划不存在";
            return readiness;
        }

        String status = normalizeCode(plan.getPlanStatus());
        if (!PLAN_STATUS_FINISHED.equals(status) && !PLAN_STATUS_ARCHIVED.equals(status)) {
            readiness.canPublish = false;
            readiness.blockedReason = "只有计划结束或归档后才可以发布成绩";
            return readiness;
        }

        List<BizAssignment> assignments = assignmentMapper.selectList(new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getPlanId, plan.getId())
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L));
        if (CollectionUtils.isEmpty(assignments)) {
            readiness.canPublish = false;
            readiness.blockedReason = "当前计划尚无分配记录，暂不能发布成绩";
            return readiness;
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
            boolean innerDone = roles != null && roles.contains(ROLE_INNER_TEACHER);
            boolean baseDone = roles != null && roles.contains(ROLE_BASE_TEACHER);
            if (!innerDone || !baseDone) {
                readiness.canPublish = false;
                readiness.blockedReason = "当前计划还有未完成的综合评价，暂不能发布成绩";
                return readiness;
            }
        }

        readiness.canPublish = true;
        readiness.blockedReason = null;
        return readiness;
    }

    private void ensureScoreViewAccess(BizScoreSheet scoreSheet,
                                       BizInternshipPlan plan,
                                       SysUser operator,
                                       String normalizedRole) {
        if (ROLE_SYS_ADMIN.equals(normalizedRole)) {
            return;
        }
        if (ROLE_DEPT_ADMIN.equals(normalizedRole)) {
            if (Objects.equals(plan.getDeptId(), operator.getDeptId())) {
                return;
            }
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看该成绩单");
        }
        if (ROLE_ACADEMIC_ADMIN.equals(normalizedRole)) {
            if (SCORE_STATUS_PUBLISHED.equals(normalizeCode(scoreSheet.getStatus()))) {
                return;
            }
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看未发布成绩单");
        }
        if (ROLE_STUDENT.equals(normalizedRole)) {
            if (Objects.equals(scoreSheet.getStudentId(), operator.getId())
                    && SCORE_STATUS_PUBLISHED.equals(normalizeCode(scoreSheet.getStatus()))) {
                return;
            }
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看该成绩单");
        }
        if (ROLE_INNER_TEACHER.equals(normalizedRole) || ROLE_BASE_TEACHER.equals(normalizedRole)) {
            if (!SCORE_STATUS_PUBLISHED.equals(normalizeCode(scoreSheet.getStatus()))) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看未发布成绩单");
            }
            BizAssignment assignment = requireAssignment(scoreSheet.getAssignmentId());
            if (ROLE_INNER_TEACHER.equals(normalizedRole) && Objects.equals(assignment.getInnerTeacherId(), operator.getId())) {
                return;
            }
            if (ROLE_BASE_TEACHER.equals(normalizedRole) && Objects.equals(assignment.getBaseTeacherId(), operator.getId())) {
                return;
            }
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看该成绩单");
        }
        throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "无权查看成绩数据");
    }

    private BizScoreSheet requireScoreSheet(Long scoreSheetId) {
        if (scoreSheetId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "成绩单标识不能为空");
        }
        BizScoreSheet sheet = scoreSheetMapper.selectOne(new LambdaQueryWrapper<BizScoreSheet>()
                .eq(BizScoreSheet::getId, scoreSheetId)
                .eq(BizScoreSheet::getDeleted, 0L)
                .last("LIMIT 1"));
        if (sheet == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "成绩单不存在");
        }
        return sheet;
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

    private Long resolveBackfillOperatorId() {
        SysUser admin = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getLoginName, "A0001")
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        if (admin != null) {
            return admin.getId();
        }

        SysUser anyUser = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        return anyUser == null ? 0L : anyUser.getId();
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

    private Map<Long, BaseDepartment> queryDeptMap(Set<Long> ids) {
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

    private List<BizAssignment> queryCurrentAssignmentsByTeacher(Long teacherId, String roleCode) {
        LambdaQueryWrapper<BizAssignment> wrapper = new LambdaQueryWrapper<BizAssignment>()
                .eq(BizAssignment::getIsCurrent, 1)
                .eq(BizAssignment::getDeleted, 0L);
        if (ROLE_INNER_TEACHER.equals(roleCode)) {
            wrapper.eq(BizAssignment::getInnerTeacherId, teacherId);
        } else if (ROLE_BASE_TEACHER.equals(roleCode)) {
            wrapper.eq(BizAssignment::getBaseTeacherId, teacherId);
        } else {
            return new ArrayList<>();
        }
        return assignmentMapper.selectList(wrapper);
    }

    private static class PublishReadiness {
        private boolean canPublish;
        private String blockedReason;
    }

    private Set<Long> queryPlanIdsByDept(Long deptId) {
        if (deptId == null) {
            return new HashSet<>();
        }
        return planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                        .eq(BizInternshipPlan::getDeptId, deptId)
                        .eq(BizInternshipPlan::getDeleted, 0L))
                .stream()
                .map(BizInternshipPlan::getId)
                .collect(Collectors.toSet());
    }

    private SysUser findStudentByStudentNo(String studentNo) {
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStudentNo, studentNo)
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
    }

    private Set<Long> queryStudentIdsByName(String studentName) {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0L)
                        .isNotNull(SysUser::getStudentNo)
                        .like(SysUser::getRealName, studentName))
                .stream()
                .map(SysUser::getId)
                .collect(Collectors.toSet());
    }

    private ScoreDetailSnapshotVO parseSnapshot(String detailJson) {
        if (!StringUtils.hasText(detailJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(detailJson, new TypeReference<ScoreDetailSnapshotVO>() {
            });
        } catch (Exception ex) {
            return null;
        }
    }

    private String serializeSnapshot(ScoreDetailSnapshotVO snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception ex) {
            throw new BusinessException(ApiCode.INTERNAL_ERROR.getCode(), "成绩明细快照序列化失败");
        }
    }

    private String resolveDeptName(Long deptId, Map<Long, BaseDepartment> map) {
        if (deptId == null) {
            return null;
        }
        BaseDepartment dept = map.get(deptId);
        return dept == null ? null : dept.getDeptName();
    }

    private String resolveMajorName(Long majorId, Map<Long, BaseMajor> map) {
        if (majorId == null) {
            return null;
        }
        BaseMajor major = map.get(majorId);
        return major == null ? null : major.getMajorName();
    }

    private String resolveGradeName(Long gradeId, Map<Long, BaseGrade> map) {
        if (gradeId == null) {
            return null;
        }
        BaseGrade grade = map.get(gradeId);
        return grade == null ? null : grade.getGradeName();
    }

    private boolean contains(String text, String keywordLowerCase) {
        return StringUtils.hasText(text) && text.toLowerCase(Locale.ROOT).contains(keywordLowerCase);
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

    private long normalizePage(long page) {
        return page <= 0 ? 1 : page;
    }

    private long normalizeSize(long size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 200);
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

    private BigDecimal validateAndNormalizeScore(BigDecimal score) {
        if (score == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "评分不能为空");
        }
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(HUNDRED) > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "评分必须在 0 到 100 之间");
        }
        return normalizeScore(score);
    }

    private BigDecimal normalizeScore(BigDecimal score) {
        if (score == null) {
            return null;
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private Set<Long> singletonSet(Long id) {
        Set<Long> result = new HashSet<>();
        if (id != null) {
            result.add(id);
        }
        return result;
    }

    private Set<Long> multiSet(Long left, Long right) {
        Set<Long> result = new HashSet<>();
        if (left != null) {
            result.add(left);
        }
        if (right != null) {
            result.add(right);
        }
        return result;
    }

    private String defaultText(String text) {
        return text == null ? "" : text;
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0D : value.doubleValue();
    }

    private String toPlain(BigDecimal value) {
        return value == null ? "0.00" : normalizeScore(value).toPlainString();
    }
}

