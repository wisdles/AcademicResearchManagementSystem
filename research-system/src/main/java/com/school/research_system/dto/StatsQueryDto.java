package com.school.research_system.dto;

import lombok.Data;

@Data
public class StatsQueryDto {
    private Long collegeId;
    private String teacherName;
    private Integer year; // 年份筛选，null 表示全部
}