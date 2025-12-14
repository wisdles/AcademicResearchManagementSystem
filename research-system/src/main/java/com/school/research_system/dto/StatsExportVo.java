package com.school.research_system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StatsExportVo {
    private String type; // 成果类型 (项目/论文...)
    private String name; // 名称 (项目名/论文标题...)
    private String applicantName; // 申报人
    private String collegeName; // 所属学院
    private String classification; // 科研/教学
    private String status; // 状态 (固定为"已通过")
    private LocalDateTime createTime; // 提交时间
}