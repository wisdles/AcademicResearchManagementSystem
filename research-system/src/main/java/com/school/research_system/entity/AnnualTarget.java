package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_annual_target")
public class AnnualTarget {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long collegeId;
    private Integer year;
    private Integer projectTarget;
    private Integer paperTarget;
    private Integer patentTarget;
    private Integer softTarget;
    private Integer bookTarget;
    private Integer awardTarget;
    private Integer competitionTarget;
    private Integer courseTarget;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
