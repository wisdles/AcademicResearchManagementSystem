package com.school.research_system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.research_system.entity.Competition;

/**
 * 竞赛获奖 Mapper
 */
@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {
}