package com.school.research_system.dto;

import lombok.Data;

@Data
public class PasswordDto {
    private String oldPassword;
    private String newPassword;
}