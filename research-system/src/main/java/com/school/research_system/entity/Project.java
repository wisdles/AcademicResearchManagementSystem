package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId; // 申报人ID
    private Long collegeId; // 归属学院ID

    private String name; // 项目名称
    /**
     * 申报人姓名 (非数据库字段，用于前端展示)
     */
    @TableField(exist = false)
    private String applicantName;
    // 核心分类: TEACHING(教学), RESEARCH(科研)
    private String category;

    // 子类型: 纵向/横向/校级/教改
    private String subType;
    // =========== 🔴 新增字段 ===========
    /**
     * 项目等级: NATIONAL, PROVINCIAL, CITY, SCHOOL
     */
    private String level;

    /**
     * 结项证明附件
     */
    private String closureFileUrl;
    // =================================
    private BigDecimal funds; // 经费

    private LocalDate startDate;
    private LocalDate endDate;

    private String appFileUrl; // 申报书路径
    private String contractFileUrl; // 合同路径
    // === 新增字段，对应数据库 ===
    private String projectSource;
    private String projectNumber;
    private String leader;
    private String participants;
    private String discipline;
    private String remark;
    /**
     * 状态:
     * 0-暂存(草稿)
     * 1-待秘书审核
     * 2-待院长审核
     * 3-已通过
     * -1-秘书驳回
     * -2-院长驳回
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}