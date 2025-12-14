
package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long targetId; // 项目ID或论文ID
    private String targetType; // 类型: PROJECT, PAPER

    private Long operatorId; // 操作人ID
    private String operatorName;// 操作人姓名

    private String action; // 动作: PASS_SEC, REJECT_SEC...
    private String comment; // 审核意见

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}