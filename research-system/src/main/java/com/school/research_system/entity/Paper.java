package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_paper")
public class Paper {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String journalName;
    private LocalDate publishDate;

    private Boolean isSci;
    private String sciPartition;
    private BigDecimal impactFactor;
    private String pageRange;
    private String authorRole;

    private String fileUrl;
    private String ocrContent;
    // 新增字段需增加: issn, pages, partitionInfo, authors, correspondingAuthor,
    // discipline, category, proofFile, remark.
    private String issn;
    private String pages;
    private String partitionInfo;
    private String authors;
    private String correspondingAuthor;
    private String discipline;
    private String category;
    private String proofFile;
    private String remark;
    /**
     * 申报人姓名 (非数据库字段，用于前端展示)
     */
    @TableField(exist = false)
    private String applicantName;
    // ------
    /**
     * 状态: 0-草稿, 1-待秘书审, 2-待院长审, 3-通过, -1/-2 驳回
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 🔴 建议补上这个字段，和 Project 保持一致
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    private String classification;// 科学教研分类
}