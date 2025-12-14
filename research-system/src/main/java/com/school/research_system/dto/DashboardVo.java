package com.school.research_system.dto; // 偷懒放dto包

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVo {
    // 核心指标
    private Integer totalAchievements; // 总成果数
    private BigDecimal totalFunds; // 总经费
    private Integer activeTeachers; // 活跃教师数

    private Integer currentMonthNew; // 本月新增数

    // 图表数据
    private Map<String, Integer> categoryDistribution; // 饼图：各类成果占比
    private List<Map<String, Object>> trendData; // 折线图：近期趋势
    private List<Map<String, Object>> topTeachers; // 柱状图：教师Top榜
}