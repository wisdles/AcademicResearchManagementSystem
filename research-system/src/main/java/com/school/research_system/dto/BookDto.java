// package com.school.research_system.dto;

// import lombok.Data;
// import java.util.Date;

// @Data
// public class BookDto {
//     private Long id; // 草稿修改时需要传ID
//     private String name; // 书名
//     private String publisher; // 出版社
//     private String isbn; // ISBN号
//     private Date publishDate; // 出版日期
//     // 🔴 必须补全这个字段，控制是“存草稿”还是“提交”
//     private Boolean isSubmit;
// }
package com.school.research_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BookDto {
    private Long id;

    @JsonProperty("book_title")
    private String name;

    private String isbn;
    private String publisher;

    @JsonProperty("publish_date")
    private LocalDate publishDate;

    private String authors;
    private String editor;
    private String category;
    private String level;

    @JsonProperty("proof_file")
    private String proofFile;

    private String remark;
    private Boolean isSubmit;
    @JsonProperty("classification")
    private String classification;// 科学教研分类
}