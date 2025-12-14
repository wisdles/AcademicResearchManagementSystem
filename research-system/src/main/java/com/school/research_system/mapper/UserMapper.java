package com.school.research_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.research_system.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 告诉 Spring 这是一个 Mapper
public interface UserMapper extends BaseMapper<User> {
    // 里面什么都不用写，就已经有了 selectById, insert, delete 等方法
}