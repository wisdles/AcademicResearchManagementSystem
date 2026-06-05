package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.CourseDto;
import com.school.research_system.entity.Course;

/**
 * 课程建设 Service 接口
 */
public interface ICourseService extends IService<Course> {

    void createCourse(CourseDto dto);

    void updateCourse(CourseDto dto);

    void auditCourse(AuditDto dto);

}