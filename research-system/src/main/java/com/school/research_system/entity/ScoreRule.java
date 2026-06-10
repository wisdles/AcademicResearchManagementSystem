package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审核评分规则配置（管理员配置）
 */
@Data
@TableName("sys_score_rule")
public class ScoreRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer year;          // 年份，如2026
    private String ruleType;      // 类型：project/paper/patent...
    private String ruleKey;        // 规则键：如 SCI_Q1, NATIONAL
    private String ruleLabel;      // 显示名：如 SCI 一区
    private Integer score;         // 分值
    private String description;    // 说明
    private Integer enabled;       // 是否启用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
