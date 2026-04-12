package com.teacher.internship.modules.score.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.application.entity.BizAssignment;
import com.teacher.internship.modules.evaluation.entity.BizEvaluation;
import com.teacher.internship.modules.evaluation.mapper.BizEvaluationMapper;
import com.teacher.internship.modules.material.entity.BizMaterial;
import com.teacher.internship.modules.material.entity.BizMaterialVersion;
import com.teacher.internship.modules.material.mapper.BizMaterialMapper;
import com.teacher.internship.modules.material.mapper.BizMaterialVersionMapper;
import com.teacher.internship.modules.plan.entity.BizInternshipPlan;
import com.teacher.internship.modules.plan.entity.BizMaterialType;
import com.teacher.internship.modules.plan.mapper.BizMaterialTypeMapper;
import com.teacher.internship.modules.score.vo.ScoreDetailSnapshotVO;
import com.teacher.internship.modules.score.vo.ScoreMaterialDetailVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScoreCalculationService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private static final String ROLE_INNER_TEACHER = "INNER_TEACHER";
    private static final String ROLE_BASE_TEACHER = "BASE_TEACHER";
    private static final String TYPE_PROCESS = "PROCESS";
    private static final String TYPE_FINAL = "FINAL";
    private static final String STATUS_ENABLED = "ENABLED";

    private final BizMaterialTypeMapper materialTypeMapper;
    private final BizMaterialMapper materialMapper;
    private final BizMaterialVersionMapper materialVersionMapper;
    private final BizEvaluationMapper evaluationMapper;

    public ScoreCalculationService(BizMaterialTypeMapper materialTypeMapper,
                                   BizMaterialMapper materialMapper,
                                   BizMaterialVersionMapper materialVersionMapper,
                                   BizEvaluationMapper evaluationMapper) {
        this.materialTypeMapper = materialTypeMapper;
        this.materialMapper = materialMapper;
        this.materialVersionMapper = materialVersionMapper;
        this.evaluationMapper = evaluationMapper;
    }

    public ScoreDetailSnapshotVO calculate(BizInternshipPlan plan, BizAssignment assignment) {
        if (plan == null || assignment == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "计划和分配记录不能为空");
        }

        BigDecimal innerTeacherWeight = normalizeWeight(plan.getInnerTeacherWeight());
        BigDecimal baseTeacherWeight = normalizeWeight(plan.getBaseTeacherWeight());
        validateWeightSum(innerTeacherWeight, baseTeacherWeight, "校内导师权重与基地导师权重之和必须等于 100");

        List<BizMaterialType> materialTypes = materialTypeMapper.selectList(new LambdaQueryWrapper<BizMaterialType>()
                .eq(BizMaterialType::getPlanId, plan.getId())
                .eq(BizMaterialType::getDeleted, 0L)
                .eq(BizMaterialType::getStatus, STATUS_ENABLED)
                .orderByAsc(BizMaterialType::getDeadlineTime)
                .orderByAsc(BizMaterialType::getId));
        if (CollectionUtils.isEmpty(materialTypes)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "当前计划未配置材料类型");
        }

        BigDecimal materialWeightSum = materialTypes.stream()
                .map(item -> normalizeWeight(item.getWeight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        validateWeightSum(materialWeightSum, null, "材料权重总和必须等于 100");

        List<BizMaterial> materials = materialMapper.selectList(new LambdaQueryWrapper<BizMaterial>()
                .eq(BizMaterial::getAssignmentId, assignment.getId())
                .eq(BizMaterial::getDeleted, 0L));
        Map<Long, BizMaterial> materialByTypeMap = materials.stream()
                .collect(Collectors.toMap(BizMaterial::getMaterialTypeId, item -> item, (left, right) -> left));

        Set<Long> materialIds = materials.stream().map(BizMaterial::getId).collect(Collectors.toSet());
        Map<Long, BizMaterialVersion> currentVersionMap = queryCurrentVersionMap(materialIds);
        Map<Long, List<BizEvaluation>> processEvaluationMap = queryProcessEvaluationMap(
                currentVersionMap.values().stream().map(BizMaterialVersion::getId).collect(Collectors.toSet()),
                assignment.getId()
        );

        List<ScoreMaterialDetailVO> materialDetailList = new ArrayList<>();
        BigDecimal processScore = BigDecimal.ZERO;
        for (BizMaterialType materialType : materialTypes) {
            ScoreMaterialDetailVO detailItem = new ScoreMaterialDetailVO();
            detailItem.setMaterialTypeId(materialType.getId());
            detailItem.setMaterialTypeCode(materialType.getTypeCode());
            detailItem.setMaterialTypeName(materialType.getTypeName());
            detailItem.setMaterialWeight(normalizeWeight(materialType.getWeight()));

            BizMaterial material = materialByTypeMap.get(materialType.getId());
            BizMaterialVersion version = null;
            if (material != null) {
                detailItem.setMaterialId(material.getId());
                version = currentVersionMap.get(material.getId());
            }
            if (version != null) {
                detailItem.setMaterialVersionId(version.getId());
                detailItem.setMaterialVersionNo(version.getVersionNo());
                detailItem.setFileName(version.getFileName());
            }

            List<BizEvaluation> processEvaluations = version == null
                    ? new ArrayList<>()
                    : processEvaluationMap.getOrDefault(version.getId(), new ArrayList<>());
            BigDecimal innerProcessScore = latestRoleScore(processEvaluations, ROLE_INNER_TEACHER);
            BigDecimal baseProcessScore = latestRoleScore(processEvaluations, ROLE_BASE_TEACHER);
            detailItem.setInnerTeacherProcessScore(innerProcessScore);
            detailItem.setBaseTeacherProcessScore(baseProcessScore);

            BigDecimal materialCompositeScore = combineByTeacherWeight(
                    innerProcessScore,
                    baseProcessScore,
                    innerTeacherWeight,
                    baseTeacherWeight
            );
            detailItem.setMaterialCompositeScore(materialCompositeScore);

            BigDecimal weightedContribution = percentMul(materialCompositeScore, detailItem.getMaterialWeight());
            weightedContribution = normalizeScore(weightedContribution);
            detailItem.setWeightedContribution(weightedContribution);
            processScore = processScore.add(weightedContribution);
            materialDetailList.add(detailItem);
        }
        processScore = normalizeScore(processScore);

        List<BizEvaluation> finalEvaluations = evaluationMapper.selectList(new LambdaQueryWrapper<BizEvaluation>()
                .eq(BizEvaluation::getAssignmentId, assignment.getId())
                .eq(BizEvaluation::getEvaluationType, TYPE_FINAL)
                .eq(BizEvaluation::getDeleted, 0L)
                .orderByDesc(BizEvaluation::getEvaluatedTime)
                .orderByDesc(BizEvaluation::getId));

        BigDecimal innerFinalScore = latestRoleScore(finalEvaluations, ROLE_INNER_TEACHER);
        BigDecimal baseFinalScore = latestRoleScore(finalEvaluations, ROLE_BASE_TEACHER);
        BigDecimal finalScore = combineByTeacherWeight(innerFinalScore, baseFinalScore, innerTeacherWeight, baseTeacherWeight);
        finalScore = normalizeScore(finalScore);

        BigDecimal autoTotalScore = normalizeScore(processScore.add(finalScore).divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP));
        autoTotalScore = clampScore(autoTotalScore);

        ScoreDetailSnapshotVO snapshot = new ScoreDetailSnapshotVO();
        snapshot.setFormulaVersion("SCORE_V1");
        snapshot.setInnerTeacherWeight(innerTeacherWeight);
        snapshot.setBaseTeacherWeight(baseTeacherWeight);
        snapshot.setInnerTeacherFinalScore(innerFinalScore);
        snapshot.setBaseTeacherFinalScore(baseFinalScore);
        snapshot.setProcessScore(processScore);
        snapshot.setFinalScore(finalScore);
        snapshot.setAutoTotalScore(autoTotalScore);
        snapshot.setTotalScore(autoTotalScore);
        snapshot.setCalculatedTime(LocalDateTime.now());
        snapshot.setMaterialDetails(materialDetailList);
        return snapshot;
    }

    private Map<Long, BizMaterialVersion> queryCurrentVersionMap(Set<Long> materialIds) {
        if (CollectionUtils.isEmpty(materialIds)) {
            return new HashMap<>();
        }
        List<BizMaterialVersion> versions = materialVersionMapper.selectList(new LambdaQueryWrapper<BizMaterialVersion>()
                .in(BizMaterialVersion::getMaterialId, materialIds)
                .eq(BizMaterialVersion::getIsCurrent, 1)
                .eq(BizMaterialVersion::getDeleted, 0L));
        return versions.stream().collect(Collectors.toMap(BizMaterialVersion::getMaterialId, item -> item, (left, right) -> left));
    }

    private Map<Long, List<BizEvaluation>> queryProcessEvaluationMap(Set<Long> versionIds, Long assignmentId) {
        if (CollectionUtils.isEmpty(versionIds)) {
            return new HashMap<>();
        }
        List<BizEvaluation> processEvaluations = evaluationMapper.selectList(new LambdaQueryWrapper<BizEvaluation>()
                .in(BizEvaluation::getMaterialVersionId, versionIds)
                .eq(BizEvaluation::getAssignmentId, assignmentId)
                .eq(BizEvaluation::getEvaluationType, TYPE_PROCESS)
                .eq(BizEvaluation::getDeleted, 0L)
                .orderByDesc(BizEvaluation::getEvaluatedTime)
                .orderByDesc(BizEvaluation::getId));
        return processEvaluations.stream()
                .filter(item -> item.getMaterialVersionId() != null)
                .collect(Collectors.groupingBy(BizEvaluation::getMaterialVersionId));
    }

    private BigDecimal latestRoleScore(List<BizEvaluation> evaluations, String roleCode) {
        if (CollectionUtils.isEmpty(evaluations)) {
            return ZERO;
        }
        String normalizedRole = normalizeCode(roleCode);
        BizEvaluation latest = evaluations.stream()
                .filter(item -> normalizedRole.equals(normalizeCode(item.getEvaluatorRole())))
                .filter(item -> item.getScore() != null)
                .sorted(Comparator.comparing(BizEvaluation::getEvaluatedTime, Comparator.nullsLast(LocalDateTime::compareTo))
                        .reversed()
                        .thenComparing(BizEvaluation::getId, Comparator.nullsLast(Long::compareTo).reversed()))
                .findFirst()
                .orElse(null);
        return latest == null ? ZERO : normalizeScore(latest.getScore());
    }

    private BigDecimal combineByTeacherWeight(BigDecimal innerScore,
                                              BigDecimal baseScore,
                                              BigDecimal innerWeight,
                                              BigDecimal baseWeight) {
        BigDecimal safeInnerScore = innerScore == null ? ZERO : normalizeScore(innerScore);
        BigDecimal safeBaseScore = baseScore == null ? ZERO : normalizeScore(baseScore);
        BigDecimal innerPart = percentMul(safeInnerScore, innerWeight);
        BigDecimal basePart = percentMul(safeBaseScore, baseWeight);
        return clampScore(normalizeScore(innerPart.add(basePart)));
    }

    private BigDecimal percentMul(BigDecimal score, BigDecimal weight) {
        BigDecimal safeScore = score == null ? ZERO : score;
        BigDecimal safeWeight = weight == null ? ZERO : weight;
        return safeScore.multiply(safeWeight).divide(HUNDRED, 4, RoundingMode.HALF_UP);
    }

    private void validateWeightSum(BigDecimal left, BigDecimal right, String message) {
        BigDecimal sum;
        if (right == null) {
            sum = normalizeWeight(left);
        } else {
            sum = normalizeWeight(left).add(normalizeWeight(right)).setScale(2, RoundingMode.HALF_UP);
        }
        if (sum.compareTo(HUNDRED.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), message);
        }
    }

    private BigDecimal normalizeWeight(BigDecimal weight) {
        if (weight == null) {
            return ZERO;
        }
        return weight.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeScore(BigDecimal score) {
        if (score == null) {
            return ZERO;
        }
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal clampScore(BigDecimal score) {
        BigDecimal safe = score == null ? ZERO : score;
        if (safe.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO;
        }
        if (safe.compareTo(HUNDRED) > 0) {
            return HUNDRED.setScale(2, RoundingMode.HALF_UP);
        }
        return normalizeScore(safe);
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}
