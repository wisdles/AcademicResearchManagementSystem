package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_software_copyright")
public class SoftwareCopyright {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name; // 软件名称
    private String registerNo; // 登记号
    private LocalDate developDate; // 开发完成日期
    private String fileUrl; // 证书文件
    // 需增加: grantDate, authors, classification, organization, proofFile, remark.
    private LocalDate grantDate;
    private String authors;
    private String softwareType;
    private String organization;
    private String proofFile;
    private String remark;
    /**
     * 申报人姓名 (非数据库字段，用于前端展示)
     */
    @TableField(exist = false)
    private String applicantName;
    /**
     * 状态: 0-草稿, 1-待秘书审, 2-待院长审, 3-通过, -1/-2 驳回
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    private String classification;// 科学教研分类
}