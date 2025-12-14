package com.school.research_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.research_system.entity.Project;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    // 新增这个统计查询
    @Select("SELECT c.name as collegeName, COUNT(p.id) as count " +
            "FROM biz_project p " +
            "LEFT JOIN sys_college c ON p.college_id = c.id " +
            "WHERE p.status = 3 AND p.is_deleted = 0 " + // 只统计审核通过的
            "GROUP BY c.id, c.name")
    List<Map<String, Object>> selectProjectCountByCollege();
}
