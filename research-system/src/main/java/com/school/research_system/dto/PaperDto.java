// package com.school.research_system.dto;

// import lombok.Data;
// import java.math.BigDecimal;
// import java.time.LocalDate;

// @Data
// public class PaperDto {
//     // 🔴 必须加上这个，用于修改草稿
//     private Long id;

//     private String title;
//     private String journalName;
//     private LocalDate publishDate;

//     // 核心科研指标
//     private Boolean isSci;
//     private String sciPartition;
//     private BigDecimal impactFactor;
//     private String pageRange;
//     private String authorRole;

//     private String fileUrl;

//     // true=提交审核, false=暂存
//     private Boolean isSubmit;
// }
package com.school.research_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaperDto {
    private Long id;

    private String title;

    private String journalName;

    private String issn;

    private BigDecimal impactFactor;

    // 前端: pages -> 后端: pages
    private String pages;

    private String sciPartition;

    private LocalDate publishDate;

    private String authors;

    private String correspondingAuthor;

    private String discipline;

    private String proofFile;

    private String remark;
    private Boolean isSubmit;

    private String classification;// 科学教研分类
}