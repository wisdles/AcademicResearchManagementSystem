package com.school.research_system.dto;

import lombok.Data;

@Data
public class StatsQueryDto {
    private Long collegeId; // 学院ID筛选
    private String teacherName; // 教师姓名/工号模糊搜索
}