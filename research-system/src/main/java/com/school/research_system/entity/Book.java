package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private String publisher;
    private String isbn;
    private LocalDate publishDate;
    // 需增加: authors, editor, classification, level, proofFile, remark.
    private String authors;
    private String editor;
    private String bookType;
    private String level;
    private String proofFile;
    private String remark;
    /**
     * 申报人姓名 (非数据库字段，用于前端展示)
     */
    @TableField(exist = false)
    private String applicantName;
    // ------------
    private Integer status; // 0草稿,1提交,2秘书通过,3院长通过,-1驳回

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    private String classification;// 科学教研分类

}
