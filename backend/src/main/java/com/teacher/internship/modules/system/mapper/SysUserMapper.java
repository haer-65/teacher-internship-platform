package com.teacher.internship.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teacher.internship.modules.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}

