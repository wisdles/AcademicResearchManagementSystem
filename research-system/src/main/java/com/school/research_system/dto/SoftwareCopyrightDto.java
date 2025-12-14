
package com.school.research_system.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SoftwareCopyrightDto {
    private Long id;

    private String name;

    private String registerNo;

    private LocalDate grantDate;

    private String authors;
    private String softwareType;
    private String organization;

    private String proofFile;

    private String remark;
    private Boolean isSubmit;

    private String classification;// 科学教研分类
}