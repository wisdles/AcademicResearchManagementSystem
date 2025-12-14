// package com.school.research_system.dto;

// import lombok.Data;
// import java.time.LocalDate;

// @Data
// public class SoftwareCopyrightDto {
//     private Long id;

//     private String name;
//     private String registerNo;
//     private LocalDate developDate;
//     private String fileUrl;

//     private Boolean isSubmit;
// }
package com.school.research_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SoftwareCopyrightDto {
    private Long id;

    @JsonProperty("software_name")
    private String name;

    @JsonProperty("register_number")
    private String registerNo;

    @JsonProperty("grant_date")
    private LocalDate grantDate;

    private String authors;
    private String category;
    private String organization;

    @JsonProperty("proof_file")
    private String proofFile;

    private String remark;
    private Boolean isSubmit;
    @JsonProperty("classification")
    private String classification;// 科学教研分类
}