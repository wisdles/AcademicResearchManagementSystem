// package com.school.research_system.dto;

// import lombok.Data;
// import java.time.LocalDate;

// @Data
// public class PatentDto {
//     private Long id; // 修改时必填

//     private String name;
//     private String patentNo;
//     private String type;
//     private LocalDate authDate;
//     private String fileUrl;

//     // true=提交审核, false=暂存
//     private Boolean isSubmit;
// }
package com.school.research_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PatentDto {
    private Long id;

    @JsonProperty("patent_name")
    private String name; // 映射到实体 name

    @JsonProperty("patent_number")
    private String patentNo; // 映射到实体 patentNo

    @JsonProperty("patent_type")
    private String type; // 映射到实体 type

    @JsonProperty("apply_date")
    private LocalDate applyDate;

    @JsonProperty("grant_date")
    private LocalDate grantDate; // 对应新增字段

    private String owner;
    private String inventors;
    private String status; // 前端传的"已授权"等文本，存入remark或忽略，不影响审核状态

    @JsonProperty("proof_file")
    private String proofFile;

    private String remark;
    private Boolean isSubmit;

    @JsonProperty("classification")
    private String classification;// 科学教研分类
}