package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_notice")
public class Notice {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String content; // 公告正文

    // 类型: TEACHING(教学), RESEARCH(科研), ALL(全体)
    private String category;

    private Long publisherId; // 发布人ID
    private String publisherName; // 发布人姓名 (建议冗余存入，或者查询时关联)

    private String attachmentUrl; // 附件地址
    private String attachmentName; // 附件名
    private Long targetCollegeId; // 新增字段，用于指定目标学院
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}