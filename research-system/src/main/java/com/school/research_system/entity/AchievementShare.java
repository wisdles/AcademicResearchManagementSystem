package com.school.research_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 成果共享关系：用于多人共同成果机制
 * 一条记录 = 某成果共享给某用户
 * 主作者(owner_user_id) 与 共同作者(shared_user_id) 通过本表关联
 */
@Data
@TableName("biz_achievement_share")
public class AchievementShare {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 成果类型: project/paper/patent/software/book/award/competition/course */
    private String achievementType;

    /** 成果ID（对应 biz_xxx.id） */
    private Long achievementId;

    /** 成果主拥有者（首录入者） */
    private Long ownerUserId;

    /** 被共享的用户（共同作者） */
    private Long sharedUserId;

    /** 共同角色: CO_AUTHOR(共同作者)/CO_LEADER(共同负责人)/PARTICIPANT(参与人) */
    private String roleType;

    /** 贡献比例 0-100，便于统计加权 */
    private Integer contributionPercent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}
