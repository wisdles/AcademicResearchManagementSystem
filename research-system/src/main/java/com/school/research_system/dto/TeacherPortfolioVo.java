package com.school.research_system.dto;

import com.school.research_system.entity.Paper;
import com.school.research_system.entity.Project;
import lombok.Data;
import java.util.List;

@Data
public class TeacherPortfolioVo {
    // 老师基本信息
    private Long userId;
    private String realName;
    private String username; // 工号
    private String collegeName;

    // 老师的成果
    private List<Project> projectList; // 项目列表
    private List<Paper> paperList; // 论文列表

    // 后面还可以加 List<Patent> patentList 等
}