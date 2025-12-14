package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_resume")
public class Resume {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String titleLevel; // 职称: 教授/副教授...
    private String education; // 学历
    private String researchDirection;// 研究方向
    private String bio; // 简介
    private String resumeFileUrl; // 附件

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}