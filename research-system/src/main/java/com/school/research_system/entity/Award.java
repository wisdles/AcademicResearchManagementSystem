package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 获奖成果实体类
 */
@Data
@TableName("biz_award")
public class Award {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String awardName;

    private String awardLevel;

    private String awardGrade;

    private String awardUnit;

    private LocalDate awardDate;

    private String ranking;

    private String classification;

    private String proofFile;

    private String remark;

    private String tags; // 标签，多个用逗号分隔

    /**
     * 申报人姓名 (非数据库字段，用于前端展示)
     */
    @TableField(exist = false)
    private String applicantName;

    // 审核状态: 0草稿,1待秘书审核,2待院长审核,3已通过,-1秘书驳回,-2院长驳回
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

}