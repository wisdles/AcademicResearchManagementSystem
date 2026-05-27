package com.school.research_system.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 课程建设DTO
 */
@Data
public class CourseDto {
    private Long id;

    private String courseName;

    private String courseType;

    private String courseLevel;

    private LocalDate startDate;

    private String description;

    private String classification;

    private String proofFile;

    private String remark;

    private String tags; // 标签

    private Boolean isSubmit;

}