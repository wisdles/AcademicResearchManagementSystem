// package com.school.research_system.entity;

// import com.baomidou.mybatisplus.annotation.*;
// import lombok.Data;
// import java.time.LocalDateTime;

// @Data
// @TableName("sys_audit_log")
// public class AuditNewLog {
// @TableId(type = IdType.AUTO)
// private Long id;

// private Long targetId;
// private String targetType;

// private Long operatorId;
// private String operatorName;

// private String action; // PASS_SEC, REJECT_SEC, PASS_DEAN, REJECT_DEAN
// private String comment;

// @TableField(fill = FieldFill.INSERT)
// private LocalDateTime createTime;

// @TableLogic
// private Integer isDeleted;
// }
