package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.common.Result;
import com.school.research_system.dto.DashboardVo;
import com.school.research_system.dto.StatsQueryDto;
import com.school.research_system.entity.*;
import com.school.research_system.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
public class StatisticsController {

    @Autowired
    private IProjectService projectService;
    @Autowired
    private IPaperService paperService;
    @Autowired
    private IPatentService patentService;
    @Autowired
    private ISoftwareCopyrightService softService;
    @Autowired
    private IBookService bookService;
    @Autowired
    private IUserService userService;

    @PostMapping("/dashboard")
    public Result<DashboardVo> getDashboardData(@RequestBody StatsQueryDto query) {
        DashboardVo vo = new DashboardVo();

        // 1. 获取符合条件的用户列表
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getRoleKey, "TEACHER");

        if (query.getCollegeId() != null) {
            userQuery.eq(User::getCollegeId, query.getCollegeId());
        }
        if (query.getTeacherName() != null && !query.getTeacherName().isEmpty()) {
            userQuery.and(w -> w.like(User::getRealName, query.getTeacherName())
                    .or().like(User::getUsername, query.getTeacherName()));
        }

        List<User> users = userService.list(userQuery);
        if (users.isEmpty()) {
            return Result.success(vo);
        }

        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());

        // 2. 统计五大表的数据量
        long projCount = projectService.count(new LambdaQueryWrapper<Project>().in(Project::getUserId, userIds));
        long paperCount = paperService.count(new LambdaQueryWrapper<Paper>().in(Paper::getUserId, userIds));
        long patentCount = patentService.count(new LambdaQueryWrapper<Patent>().in(Patent::getUserId, userIds));
        long softCount = softService
                .count(new LambdaQueryWrapper<SoftwareCopyright>().in(SoftwareCopyright::getUserId, userIds));
        long bookCount = bookService.count(new LambdaQueryWrapper<Book>().in(Book::getUserId, userIds));

        vo.setTotalAchievements((int) (projCount + paperCount + patentCount + softCount + bookCount));
        vo.setActiveTeachers(users.size());
        // ================= 🟢 新增：统计本月新增数 =================
        // 获取本月第一天的 00:00:00
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();

        // 统计各表在本月的新增量 (ge = greater or equal >=)
        long newProj = projectService.count(new LambdaQueryWrapper<Project>()
                .in(Project::getUserId, userIds)
                .ge(Project::getCreateTime, startOfMonth));

        long newPaper = paperService.count(new LambdaQueryWrapper<Paper>()
                .in(Paper::getUserId, userIds)
                .ge(Paper::getCreateTime, startOfMonth));

        long newPatent = patentService.count(new LambdaQueryWrapper<Patent>()
                .in(Patent::getUserId, userIds)
                .ge(Patent::getCreateTime, startOfMonth));

        long newSoft = softService.count(new LambdaQueryWrapper<SoftwareCopyright>()
                .in(SoftwareCopyright::getUserId, userIds)
                .ge(SoftwareCopyright::getCreateTime, startOfMonth));

        long newBook = bookService.count(new LambdaQueryWrapper<Book>()
                .in(Book::getUserId, userIds)
                .ge(Book::getCreateTime, startOfMonth));

        vo.setCurrentMonthNew((int) (newProj + newPaper + newPatent + newSoft + newBook));
        // ========================================================
        // 3. 统计经费
        List<Project> projects = projectService.list(new LambdaQueryWrapper<Project>()
                .in(Project::getUserId, userIds)
                .select(Project::getFunds));

        BigDecimal totalFunds = projects.stream()
                .map(Project::getFunds)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalFunds(totalFunds);

        // 4. 构建饼图数据
        Map<String, Integer> pieData = new LinkedHashMap<>();
        pieData.put("项目", (int) projCount);
        pieData.put("论文", (int) paperCount);
        pieData.put("专利", (int) patentCount);
        pieData.put("软著", (int) softCount);
        pieData.put("专著", (int) bookCount);
        vo.setCategoryDistribution(pieData);

        // 5. 计算 Top 10 教师
        Map<Long, Integer> teacherScoreMap = new HashMap<>();
        for (Long uid : userIds)
            teacherScoreMap.put(uid, 0);

        // 这里调用泛型方法，Java会自动推断 T 的类型
        countByGroup(teacherScoreMap, projectService, userIds);
        countByGroup(teacherScoreMap, paperService, userIds);
        countByGroup(teacherScoreMap, patentService, userIds);
        countByGroup(teacherScoreMap, softService, userIds);
        countByGroup(teacherScoreMap, bookService, userIds);

        List<Map<String, Object>> topList = teacherScoreMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    String realName = users.stream()
                            .filter(u -> u.getId().equals(entry.getKey()))
                            .findFirst()
                            .map(User::getRealName)
                            .orElse("未知");
                    map.put("name", realName);
                    map.put("count", entry.getValue());
                    return map;
                })
                .filter(m -> (int) m.get("count") > 0)
                .collect(Collectors.toList());

        vo.setTopTeachers(topList);

        // 6. 趋势图 (补全方法调用)
        vo.setTrendData(calculateTrend(userIds));

        return Result.success(vo);
    }

    /**
     * 🟢 修复后的泛型方法
     * 1. 方法前面加了 <T>
     * 2. 参数使用 IService<T>
     * 3. 内部使用 QueryWrapper<T>
     */
    private <T> void countByGroup(Map<Long, Integer> scoreMap, IService<T> service, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty())
            return;

        // 使用 T 类型的 Wrapper
        QueryWrapper<T> wrapper = new QueryWrapper<>();

        wrapper.select("user_id as userId", "count(*) as c")
                .in("user_id", userIds)
                .groupBy("user_id");

        // 此时 service.listMaps 和 wrapper 类型就匹配了
        List<Map<String, Object>> maps = service.listMaps(wrapper);

        for (Map<String, Object> map : maps) {
            if (map.get("userId") != null && map.get("c") != null) {
                Long uid = Long.valueOf(map.get("userId").toString());
                Integer count = Integer.valueOf(map.get("c").toString());
                scoreMap.merge(uid, count, Integer::sum);
            }
        }
    }

    /**
     * 🟢 补全缺失的 calculateTrend 方法
     * 这里先返回空列表，防止编译报错。后续如果需要真实趋势图可再扩展。
     */
    private List<Map<String, Object>> calculateTrend(List<Long> userIds) {
        // 暂时返回空，前端如果收到空数组，就不显示趋势图或者显示默认状态
        return new ArrayList<>();
    }
}