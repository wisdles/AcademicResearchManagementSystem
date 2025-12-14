package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_college")
public class College {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学院名称
     */
    private String name;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-正常, 1-删除
     */
    @TableLogic
    private Integer isDeleted;
}