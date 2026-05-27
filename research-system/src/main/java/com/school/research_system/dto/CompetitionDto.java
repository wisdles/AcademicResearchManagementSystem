package com.school.research_system.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 竞赛获奖DTO
 */
@Data
public class CompetitionDto {
    private Long id;

    private String name;

    private String competitionLevel;

    private String awardLevel;

    private String awardGrade;

    private LocalDate awardDate;

    private String studentName;

    private String ranking;

    private String certFileUrl;

    private String classification;

    private String proofFile;

    private String remark;

    private String tags; // 标签

    private Boolean isSubmit;

}