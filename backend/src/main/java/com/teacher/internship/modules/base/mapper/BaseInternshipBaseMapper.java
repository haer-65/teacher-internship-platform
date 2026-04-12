package com.teacher.internship.modules.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teacher.internship.modules.base.entity.BaseInternshipBase;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BaseInternshipBaseMapper extends BaseMapper<BaseInternshipBase> {
}

