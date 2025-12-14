package com.school.research_system.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class StatsDetailQueryDto {
    private Long collegeId; // 学院ID
    private String teacherName; // 教师姓名
    private String classification;// 教研类型: RESEARCH / TEACHING
    private List<String> types; // 成果类型: ["project", "paper", "patent", "software", "book"]
    private List<LocalDate> dateRange; // 时间范围 [开始日期, 结束日期]
}