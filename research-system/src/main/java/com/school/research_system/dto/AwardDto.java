package com.school.research_system.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 获奖成果DTO
 */
@Data
public class AwardDto {
    private Long id;

    private String awardName;

    private String awardLevel;

    private String awardGrade;

    private String awardUnit;

    private LocalDate awardDate;

    private String ranking;

    private String classification;

    private String proofFile;

    private String remark;

    private String tags; // 标签

    private Boolean isSubmit;

}