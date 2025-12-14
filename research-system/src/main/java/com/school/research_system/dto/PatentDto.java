
package com.school.research_system.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PatentDto {
    private Long id;

    private String name; // 映射到实体 name

    private String patentNo; // 映射到实体 patentNo

    private String patentType; // 映射到实体 type

    private LocalDate applyDate;

    private LocalDate grantDate; // 对应新增字段

    private String owner;
    private String inventors;
    private String status; // 前端传的"已授权"等文本，存入remark或忽略，不影响审核状态

    private String proofFile;

    private String remark;
    private Boolean isSubmit;

    private String classification;// 科学教研分类
}