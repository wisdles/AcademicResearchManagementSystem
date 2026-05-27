
package com.school.research_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectDto {
    private Long id;

    private String name;

    private String projectSource;

    private String level;

    private String projectNumber;

    private BigDecimal funds;

    private LocalDate startDate;

    private LocalDate endDate;

    private String leader;
    private String participants;
    private String discipline;

    private String openFileUrl;

    private String closeFileUrl;

    private String remark;

    // 前端: classification -> 后端: classification (TEACHING/RESEARCH)
    private String classification;

    private String tags; // 标签

    private Boolean isSubmit; // 必须有，控制草稿还是提交
}