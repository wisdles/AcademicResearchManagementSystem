// package com.school.research_system.dto;

// import lombok.Data;
// import java.math.BigDecimal;
// import java.time.LocalDate;

// @Data
// public class ProjectDto {
//     private Long id;
//     private String name;
//     private String category; // TEACHING 或 RESEARCH
//     private String subType;
//     private BigDecimal funds;
//     private LocalDate startDate;
//     private LocalDate endDate;
//     private String appFileUrl;
//     // 新增
//     private String level;
//     private String closureFileUrl;

//     // true=直接提交审核，false=保存草稿
//     private Boolean isSubmit;
// }
package com.school.research_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectDto {
    private Long id;

    // 前端: project_name -> 后端: name
    @JsonProperty("project_name")
    private String name;

    @JsonProperty("project_source")
    private String projectSource;

    // 前端: project_level -> 后端: level
    @JsonProperty("project_level")
    private String level;

    @JsonProperty("project_number")
    private String projectNumber;

    // 前端: budget -> 后端: funds
    @JsonProperty("budget")
    private BigDecimal funds;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    private String leader;
    private String participants;
    private String discipline;

    // 前端: open_file -> 后端: appFileUrl
    @JsonProperty("open_file")
    private String appFileUrl;

    // 前端: close_file -> 后端: closureFileUrl
    @JsonProperty("close_file")
    private String closureFileUrl;

    private String remark;

    // 前端: category -> 后端: category (TEACHING/RESEARCH)
    private String category;

    private Boolean isSubmit; // 必须有，控制草稿还是提交
}