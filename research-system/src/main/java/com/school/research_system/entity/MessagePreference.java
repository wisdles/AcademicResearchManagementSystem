package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_message_preference")
public class MessagePreference {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer auditResultEnabled;  // 审核结果通知
    private Integer urgeEnabled;          // 催报通知
    private Integer noticeEnabled;        // 系统公告
    private Integer systemEnabled;        // 系统消息

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
