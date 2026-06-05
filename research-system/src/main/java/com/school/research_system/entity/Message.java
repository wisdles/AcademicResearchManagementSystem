package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@TableName("sys_message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId;

    private Long receiverId;

    private String title;

    private String content;

    private String type;

    private Long relatedId;

    private String relatedType;

    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;

}