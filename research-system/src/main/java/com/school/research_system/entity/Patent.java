package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_patent")
public class Patent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId; // 申请人ID

    private String name; // 专利名称
    private String patentNo; // 专利号
    private String patentType; // 类型: 发明/实用新型/外观
    private LocalDate grantDate; // 授权日期
    private String fileUrl; // 证书文件路径
    // 需需增加: applyDate, grantDate, owner, inventors, proofFile, remark.
    private LocalDate applyDate;
    private String owner;
    private String inventors;
    private String proofFile;
    private String remark;
    /**
     * 申报人姓名 (非数据库字段，用于前端展示)
     */
    @TableField(exist = false)
    private String applicantName;
    // -----------
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