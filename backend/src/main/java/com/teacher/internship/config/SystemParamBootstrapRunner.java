package com.teacher.internship.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.internship.modules.system.entity.SysParam;
import com.teacher.internship.modules.system.mapper.SysParamMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SystemParamBootstrapRunner implements ApplicationRunner {

    private static final String PARAM_LOG_RETENTION_DAYS = "LOG_RETENTION_DAYS";

    private final SysParamMapper paramMapper;

    public SystemParamBootstrapRunner(SysParamMapper paramMapper) {
        this.paramMapper = paramMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        SysParam existing = paramMapper.selectOne(new LambdaQueryWrapper<SysParam>()
                .eq(SysParam::getParamCode, PARAM_LOG_RETENTION_DAYS)
                .eq(SysParam::getDeleted, 0L)
                .last("LIMIT 1"));
        if (existing != null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        SysParam entity = new SysParam();
        entity.setParamCode(PARAM_LOG_RETENTION_DAYS);
        entity.setParamName("日志保留天数");
        entity.setParamValue("30");
        entity.setParamType("SYSTEM");
        entity.setStatus("ENABLED");
        entity.setRemark("自动清理日志保留周期");
        entity.setCreatedBy(0L);
        entity.setUpdatedBy(0L);
        entity.setCreatedTime(now);
        entity.setUpdatedTime(now);
        entity.setDeleted(0L);
        paramMapper.insert(entity);
    }
}
