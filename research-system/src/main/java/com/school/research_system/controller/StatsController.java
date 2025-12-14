package com.school.research_system.controller;

import com.school.research_system.common.Result;
import com.school.research_system.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 统计各学院的项目数量 (仅统计已通过的项目 status=3)
     * 返回格式示例: [{"collegeName": "计算机学院", "count": 5}, ...]
     */
    @GetMapping("/college-project")
    public Result<List<Map<String, Object>>> statsByCollege() {
        // 使用 MyBatis-Plus 的 QueryWrapper 自定义查询比较麻烦
        // 这里为了简单直观，我们用 XML 或者 @Select 注解
        // 但为了不增加文件，我们直接用 Wrapper 查出所有数据再在内存处理?
        // 不，那样性能太差。我们去 Mapper 加上自定义 SQL。

        return Result.success(projectMapper.selectProjectCountByCollege());
    }
}