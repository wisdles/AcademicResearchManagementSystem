package com.school.research_system.dto;

import lombok.Data;

@Data
public class AuditDto {
    private Long projectId; // 要审核的项目ID
    private Boolean isPass; // true:通过, false:驳回
    private String comment; // 审核意见
}