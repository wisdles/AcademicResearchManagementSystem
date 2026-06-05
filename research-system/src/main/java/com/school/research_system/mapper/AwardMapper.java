package com.school.research_system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.research_system.entity.Award;

/**
 * 获奖成果 Mapper
 */
@Mapper
public interface AwardMapper extends BaseMapper<Award> {
}