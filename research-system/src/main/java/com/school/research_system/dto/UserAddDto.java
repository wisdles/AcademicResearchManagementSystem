package com.school.research_system.dto;

import lombok.Data;

@Data
public class UserAddDto {
    private String username; // 工号
    private String realName; // 姓名
    private Long collegeId; // 学院ID

    // 角色: TEACHER, SEC_TEACHING, SEC_RESEARCH, DEAN, ADMIN
    private String roleKey;
}