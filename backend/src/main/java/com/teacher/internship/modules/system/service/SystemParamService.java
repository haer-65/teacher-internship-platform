package com.teacher.internship.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.system.dto.SysParamSaveRequest;
import com.teacher.internship.modules.system.entity.SysParam;
import com.teacher.internship.modules.system.mapper.SysParamMapper;
import com.teacher.internship.modules.system.vo.PageResultVO;
import com.teacher.internship.modules.system.vo.SysParamItemVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SystemParamService {

    private static final String STATUS_ENABLED = "ENABLED";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String TYPE_SYSTEM = "SYSTEM";
    private static final String TYPE_BUSINESS = "BUSINESS";
    private static final String CACHE_PREFIX = "sys:param:";

    private final SysParamMapper paramMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public SystemParamService(SysParamMapper paramMapper,
                              RedisTemplate<String, Object> redisTemplate) {
        this.paramMapper = paramMapper;
        this.redisTemplate = redisTemplate;
    }

    public PageResultVO<SysParamItemVO> queryPage(long page, long size, String keyword, String paramType, String status) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);

        LambdaQueryWrapper<SysParam> wrapper = new LambdaQueryWrapper<SysParam>()
                .eq(SysParam::getDeleted, 0L)
                .orderByDesc(SysParam::getUpdatedTime)
                .orderByDesc(SysParam::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysParam::getParamCode, keyword)
                    .or().like(SysParam::getParamName, keyword)
                    .or().like(SysParam::getParamValue, keyword));
        }
        if (StringUtils.hasText(paramType)) {
            wrapper.eq(SysParam::getParamType, normalizeType(paramType));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysParam::getStatus, normalizeStatus(status));
        }

        Page<SysParam> pageResult = paramMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        PageResultVO<SysParamItemVO> result = new PageResultVO<>();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal(pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream().map(this::toItemVO).collect(Collectors.toList()));
        return result;
    }

    public SysParamItemVO getById(Long id) {
        SysParam param = requireParam(id);
        return toItemVO(param);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysParamItemVO create(Long operatorId, SysParamSaveRequest request) {
        String paramCode = normalizeCode(request.getParamCode());
        ensureParamCodeUnique(paramCode, null);

        SysParam entity = new SysParam();
        entity.setParamCode(paramCode);
        entity.setParamName(trimText(request.getParamName()));
        entity.setParamValue(trimText(request.getParamValue()));
        entity.setParamType(normalizeType(request.getParamType()));
        entity.setStatus(STATUS_ENABLED);
        entity.setRemark(trimText(request.getRemark()));
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setDeleted(0L);
        paramMapper.insert(entity);
        clearCacheByCode(paramCode);
        return toItemVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysParamItemVO update(Long operatorId, Long id, SysParamSaveRequest request) {
        SysParam entity = requireParam(id);
        String paramCode = normalizeCode(request.getParamCode());
        ensureParamCodeUnique(paramCode, id);

        String oldCode = entity.getParamCode();
        entity.setParamCode(paramCode);
        entity.setParamName(trimText(request.getParamName()));
        entity.setParamValue(trimText(request.getParamValue()));
        entity.setParamType(normalizeType(request.getParamType()));
        entity.setRemark(trimText(request.getRemark()));
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        paramMapper.updateById(entity);

        clearCacheByCode(oldCode);
        clearCacheByCode(paramCode);
        return toItemVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysParamItemVO updateStatus(Long operatorId, Long id, String status) {
        SysParam entity = requireParam(id);
        entity.setStatus(normalizeStatus(status));
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedTime(LocalDateTime.now());
        paramMapper.updateById(entity);
        clearCacheByCode(entity.getParamCode());
        return toItemVO(entity);
    }

    public String getStringValue(String code, String defaultValue) {
        String value = getEnabledValue(code);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    public int getIntValue(String code, int defaultValue) {
        String value = getEnabledValue(code);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public BigDecimal getDecimalValue(String code, BigDecimal defaultValue) {
        String value = getEnabledValue(code);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public String getEnabledValue(String code) {
        String normalizedCode = normalizeCode(code);
        if (!StringUtils.hasText(normalizedCode)) {
            return null;
        }
        String cacheKey = cacheKey(normalizedCode);
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof String) {
                return (String) cached;
            }
            if (cached != null) {
                return String.valueOf(cached);
            }
        } catch (Exception ignored) {
            // fallback to DB directly when cache is unavailable
        }

        SysParam param = paramMapper.selectOne(new LambdaQueryWrapper<SysParam>()
                .eq(SysParam::getParamCode, normalizedCode)
                .eq(SysParam::getStatus, STATUS_ENABLED)
                .eq(SysParam::getDeleted, 0L)
                .last("LIMIT 1"));
        if (param == null || !StringUtils.hasText(param.getParamValue())) {
            return null;
        }
        String value = param.getParamValue().trim();
        try {
            redisTemplate.opsForValue().set(cacheKey, value);
        } catch (Exception ignored) {
            // keep silent, DB result already available
        }
        return value;
    }

    public void clearCacheByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return;
        }
        try {
            redisTemplate.delete(cacheKey(normalizeCode(code)));
        } catch (Exception ignored) {
            // keep silent
        }
    }

    private String cacheKey(String code) {
        return CACHE_PREFIX + code;
    }

    private SysParam requireParam(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "参数标识不能为空");
        }
        SysParam entity = paramMapper.selectOne(new LambdaQueryWrapper<SysParam>()
                .eq(SysParam::getId, id)
                .eq(SysParam::getDeleted, 0L)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "参数不存在");
        }
        return entity;
    }

    private void ensureParamCodeUnique(String paramCode, Long excludeId) {
        SysParam existing = paramMapper.selectOne(new LambdaQueryWrapper<SysParam>()
                .eq(SysParam::getParamCode, paramCode)
                .eq(SysParam::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), excludeId)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "参数编码已存在");
        }
    }

    private SysParamItemVO toItemVO(SysParam entity) {
        SysParamItemVO vo = new SysParamItemVO();
        vo.setId(entity.getId());
        vo.setParamCode(entity.getParamCode());
        vo.setParamName(entity.getParamName());
        vo.setParamValue(entity.getParamValue());
        vo.setParamType(entity.getParamType());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
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

    private String normalizeType(String text) {
        String value = normalizeCode(text);
        if (TYPE_BUSINESS.equals(value)) {
            return TYPE_BUSINESS;
        }
        return TYPE_SYSTEM;
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
