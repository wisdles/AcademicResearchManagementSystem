package com.school.research_system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.research_system.entity.Book;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
