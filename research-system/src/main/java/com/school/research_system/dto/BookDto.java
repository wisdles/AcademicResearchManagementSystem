package com.school.research_system.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookDto {
    private Long id;

    private String name;

    private String isbn;
    private String publisher;

    private LocalDate publishDate;

    private String authors;
    private String editor;
    private String bookType;
    private String level;

    private String proofFile;

    private String remark;
    private String tags; // 标签
    private Boolean isSubmit;

    private String classification;// 科学教研分类
}