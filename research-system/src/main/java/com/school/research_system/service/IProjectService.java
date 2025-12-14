package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.entity.Project;
import com.school.research_system.dto.ProjectDto;

public interface IProjectService extends IService<Project> {
    void createProject(ProjectDto dto);

    // 添加这个方法定义
    void auditProject(com.school.research_system.dto.AuditDto dto);

    void updateProject(ProjectDto dto);
}
