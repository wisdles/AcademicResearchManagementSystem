package com.school.research_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.research_system.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}