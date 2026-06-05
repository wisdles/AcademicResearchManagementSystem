package com.school.research_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.research_system.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息 Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}