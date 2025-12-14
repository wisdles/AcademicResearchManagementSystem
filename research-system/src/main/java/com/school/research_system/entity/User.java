
package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data // Lombok 自动生成 Getter/Setter/ToString
@TableName("sys_user") // 对应数据库表名
public class User {

    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;

    private String username;

    // 密码字段通常不返回给前端，但这里先写上
    private String password;

    private String realName;

    private Long collegeId;

    private String roleKey;

    private Boolean isFirstLogin;

    @TableField(fill = FieldFill.INSERT) // 自动填充（稍后配置，暂时手动或者忽略）
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 自动填充（稍后配置，暂时手动或者忽略）
    private LocalDateTime updateTime;
    @TableLogic // 核心：逻辑删除注解！
    private Integer isDeleted;
}