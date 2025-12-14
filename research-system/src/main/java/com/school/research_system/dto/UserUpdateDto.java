package com.school.research_system.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    private Long id;
    private String realName;
    private String roleKey;
    private Long collegeId;
}