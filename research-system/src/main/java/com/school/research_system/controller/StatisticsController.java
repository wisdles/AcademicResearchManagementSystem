package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.common.Result;
import com.school.research_system.dto.DashboardVo;
import com.school.research_system.dto.StatsDetailQueryDto;
import com.school.research_system.dto.StatsExportVo;
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
        vo.setClassificationDistribution(pieData);

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

    // 新增方法来获取详细统计列表，支持多条件查询+导出准备
    // StatisticsController.java

    @Autowired
    private ICollegeService collegeService; // 需注入

    // // 获取详细统计列表 (支持多条件查询 + 导出准备)
    // @PostMapping("/detail-list")
    // public Result<List<StatsExportVo>> getDetailList(@RequestBody
    // StatsDetailQueryDto query) {
    // List<StatsExportVo> result = new ArrayList<>();

    // // 1. 预处理：获取用户ID列表 (如果选了学院或老师)
    // List<Long> userIds = null;
    // if (query.getCollegeId() != null || (query.getTeacherName() != null &&
    // !query.getTeacherName().isEmpty())) {
    // LambdaQueryWrapper<User> userQ = new LambdaQueryWrapper<>();
    // if (query.getCollegeId() != null)
    // userQ.eq(User::getCollegeId, query.getCollegeId());
    // if (query.getTeacherName() != null)
    // userQ.like(User::getRealName, query.getTeacherName());
    // List<User> users = userService.list(userQ);
    // if (users.isEmpty())
    // return Result.success(new ArrayList<>()); // 没查到人，直接返回空
    // userIds = users.stream().map(User::getId).collect(Collectors.toList());
    // }

    // // 2. 辅助 Map：User ID -> Info (姓名, 学院名)
    // // 查全量用户缓存一下，方便后面拼装
    // List<User> allUsers = userService.list();
    // List<College> allColleges = collegeService.list();
    // Map<Long, String> userMap =
    // allUsers.stream().collect(Collectors.toMap(User::getId, User::getRealName));
    // Map<Long, Long> userCollegeMap = allUsers.stream()
    // .collect(Collectors.toMap(User::getId, u -> u.getCollegeId() != null ?
    // u.getCollegeId() : 0L));
    // Map<Long, String> collegeMap =
    // allColleges.stream().collect(Collectors.toMap(College::getId,
    // College::getName));

    // // 3. 通用查询条件构建器
    // // 状态必须是 3 (已通过)

    // // --- 查项目 ---
    // if (query.getTypes() == null || query.getTypes().contains("project")) {
    // LambdaQueryWrapper<Project> q = new LambdaQueryWrapper<>();
    // q.eq(Project::getStatus, 3);
    // if (userIds != null)
    // q.in(Project::getUserId, userIds);
    // if (query.getClassification() != null)
    // q.eq(Project::getClassification, query.getClassification());
    // if (query.getDateRange() != null && query.getDateRange().size() == 2) {
    // q.between(Project::getCreateTime, query.getDateRange().get(0),
    // query.getDateRange().get(1));
    // }
    // List<Project> list = projectService.list(q);
    // for (Project item : list) {
    // result.add(buildVo("项目", item.getName(), item.getUserId(),
    // item.getClassification(),
    // item.getCreateTime(), userMap, userCollegeMap, collegeMap));
    // }
    // }

    // // --- 查论文 ---
    // if (query.getTypes() == null || query.getTypes().contains("paper")) {
    // LambdaQueryWrapper<Paper> q = new LambdaQueryWrapper<>();
    // q.eq(Paper::getStatus, 3);
    // if (userIds != null)
    // q.in(Paper::getUserId, userIds);
    // if (query.getClassification() != null)
    // q.eq(Paper::getClassification, query.getClassification());
    // if (query.getDateRange() != null && query.getDateRange().size() == 2) {
    // q.between(Paper::getCreateTime, query.getDateRange().get(0),
    // query.getDateRange().get(1));
    // }
    // List<Paper> list = paperService.list(q);
    // for (Paper item : list) {
    // // 论文用 title
    // result.add(buildVo("论文", item.getTitle(), item.getUserId(),
    // item.getClassification(),
    // item.getCreateTime(), userMap, userCollegeMap, collegeMap));
    // }
    // }

    // // --- 查专利 (Patent), 软著 (SoftwareCopyright), 专著 (Book) ---
    // // 逻辑同上，代码略 (主要是换 Service 和 换 name/title 字段)
    // // 专利用 name, 软著用 name, 专著用 name
    // if (query.getTypes() == null || query.getTypes().contains("patent")) {
    // LambdaQueryWrapper<Patent> q = new LambdaQueryWrapper<>();
    // q.eq(Patent::getStatus, 3);
    // if (userIds != null)
    // q.in(Patent::getUserId, userIds);
    // if (query.getClassification() != null)
    // q.eq(Patent::getClassification, query.getClassification());
    // if (query.getDateRange() != null && query.getDateRange().size() == 2)
    // q.between(Patent::getCreateTime, query.getDateRange().get(0),
    // query.getDateRange().get(1));

    // List<Patent> list = patentService.list(q);
    // for (Patent item : list) {
    // result.add(buildVo("专利", item.getName(), item.getUserId(),
    // item.getClassification(), item.getCreateTime(),
    // userMap, userCollegeMap, collegeMap));
    // }
    // }
    // if (query.getTypes() == null || query.getTypes().contains("software")) {
    // LambdaQueryWrapper<SoftwareCopyright> q = new LambdaQueryWrapper<>();
    // q.eq(SoftwareCopyright::getStatus, 3);
    // if (userIds != null)
    // q.in(SoftwareCopyright::getUserId, userIds);
    // if (query.getClassification() != null)
    // q.eq(SoftwareCopyright::getClassification, query.getClassification());
    // if (query.getDateRange() != null && query.getDateRange().size() == 2)
    // q.between(SoftwareCopyright::getCreateTime, query.getDateRange().get(0),
    // query.getDateRange().get(1));

    // List<SoftwareCopyright> list = softService.list(q);
    // for (SoftwareCopyright item : list) {
    // result.add(buildVo("软著", item.getName(), item.getUserId(),
    // item.getClassification(), item.getCreateTime(),
    // userMap, userCollegeMap, collegeMap));
    // }
    // }
    // if (query.getTypes() == null || query.getTypes().contains("book")) {
    // LambdaQueryWrapper<Book> q = new LambdaQueryWrapper<>();
    // q.eq(Book::getStatus, 3);
    // if (userIds != null)
    // q.in(Book::getUserId, userIds);
    // if (query.getClassification() != null)
    // q.eq(Book::getClassification, query.getClassification());
    // if (query.getDateRange() != null && query.getDateRange().size() == 2)
    // q.between(Book::getCreateTime, query.getDateRange().get(0),
    // query.getDateRange().get(1));

    // List<Book> list = bookService.list(q);
    // for (Book item : list) {
    // result.add(buildVo("专著", item.getName(), item.getUserId(),
    // item.getClassification(), item.getCreateTime(),
    // userMap, userCollegeMap, collegeMap));
    // }
    // }

    // // 4. 按时间倒序排序
    // result.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

    // return Result.success(result);
    // }
    @PostMapping("/detail-list")
    public Result<List<StatsExportVo>> getDetailList(@RequestBody StatsDetailQueryDto query) {
        List<StatsExportVo> result = new ArrayList<>();

        // 1. 预处理：获取用户ID列表 (如果选了学院或老师)
        List<Long> userIds = null;
        if (query.getCollegeId() != null || (query.getTeacherName() != null && !query.getTeacherName().isEmpty())) {
            LambdaQueryWrapper<User> userQ = new LambdaQueryWrapper<>();
            if (query.getCollegeId() != null)
                userQ.eq(User::getCollegeId, query.getCollegeId());
            if (query.getTeacherName() != null)
                userQ.like(User::getRealName, query.getTeacherName());
            List<User> users = userService.list(userQ);

            // 如果按名字查不到人，直接返回空列表，避免查出不相关的数据
            if (users.isEmpty())
                return Result.success(new ArrayList<>());

            userIds = users.stream().map(User::getId).collect(Collectors.toList());
        }

        // 2. 辅助 Map (缓存用户和学院信息，避免循环查库)
        List<User> allUsers = userService.list();
        List<College> allColleges = collegeService.list();
        Map<Long, String> userMap = allUsers.stream().collect(Collectors.toMap(User::getId, User::getRealName));
        Map<Long, Long> userCollegeMap = allUsers.stream()
                .collect(Collectors.toMap(User::getId, u -> u.getCollegeId() != null ? u.getCollegeId() : 0L));
        Map<Long, String> collegeMap = allColleges.stream().collect(Collectors.toMap(College::getId, College::getName));

        // 🟢 辅助判断：是否需要查询某种类型
        // 如果 types 为 null 或 空数组，默认查所有类型
        boolean queryAll = query.getTypes() == null || query.getTypes().isEmpty();

        // 3. 分表查询并组装
        // 状态必须是 3 (已通过)

        // --- 查项目 ---
        if (queryAll || query.getTypes().contains("project")) {
            LambdaQueryWrapper<Project> q = new LambdaQueryWrapper<>();
            q.eq(Project::getStatus, 3);
            if (userIds != null)
                q.in(Project::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) {
                q.eq(Project::getClassification, query.getClassification());
            }
            if (query.getDateRange() != null && query.getDateRange().size() == 2) {
                q.between(Project::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            }
            projectService.list(q).forEach(item -> result.add(buildVo("项目", item.getName(), item.getUserId(),
                    item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        // --- 查论文 ---
        if (queryAll || query.getTypes().contains("paper")) {
            LambdaQueryWrapper<Paper> q = new LambdaQueryWrapper<>();
            q.eq(Paper::getStatus, 3);
            if (userIds != null)
                q.in(Paper::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) {
                q.eq(Paper::getClassification, query.getClassification());
            }
            if (query.getDateRange() != null && query.getDateRange().size() == 2) {
                q.between(Paper::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            }
            paperService.list(q).forEach(item -> result.add(buildVo("论文", item.getTitle(), item.getUserId(),
                    item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        // --- 查专利 ---
        if (queryAll || query.getTypes().contains("patent")) {
            LambdaQueryWrapper<Patent> q = new LambdaQueryWrapper<>();
            q.eq(Patent::getStatus, 3);
            if (userIds != null)
                q.in(Patent::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) {
                q.eq(Patent::getClassification, query.getClassification());
            }
            if (query.getDateRange() != null && query.getDateRange().size() == 2) {
                q.between(Patent::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            }
            patentService.list(q).forEach(item -> result.add(buildVo("专利", item.getName(), item.getUserId(),
                    item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        // --- 查软著 ---
        if (queryAll || query.getTypes().contains("software")) {
            LambdaQueryWrapper<SoftwareCopyright> q = new LambdaQueryWrapper<>();
            q.eq(SoftwareCopyright::getStatus, 3);
            if (userIds != null)
                q.in(SoftwareCopyright::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) {
                q.eq(SoftwareCopyright::getClassification, query.getClassification());
            }
            if (query.getDateRange() != null && query.getDateRange().size() == 2) {
                q.between(SoftwareCopyright::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            }
            softService.list(q).forEach(item -> result.add(buildVo("软著", item.getName(), item.getUserId(),
                    item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        // --- 查专著 ---
        if (queryAll || query.getTypes().contains("book")) {
            LambdaQueryWrapper<Book> q = new LambdaQueryWrapper<>();
            q.eq(Book::getStatus, 3);
            if (userIds != null)
                q.in(Book::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) {
                q.eq(Book::getClassification, query.getClassification());
            }
            if (query.getDateRange() != null && query.getDateRange().size() == 2) {
                q.between(Book::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            }
            bookService.list(q).forEach(item -> result.add(buildVo("专著", item.getName(), item.getUserId(),
                    item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        // 4. 🟢 修复排序：按时间倒序 (处理 createTime 为 null 的情况)
        result.sort((a, b) -> {
            // 如果时间是 null，视为最小时间，排在最后
            if (a.getCreateTime() == null)
                return 1;
            if (b.getCreateTime() == null)
                return -1;
            // 正常的倒序比较
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        return Result.success(result);
    }

    // 辅助构建 VO
    private StatsExportVo buildVo(String type, String name, Long userId, String cls, LocalDateTime time,
            Map<Long, String> uMap, Map<Long, Long> ucMap, Map<Long, String> cMap) {
        StatsExportVo vo = new StatsExportVo();
        vo.setType(type);
        vo.setName(name);
        vo.setApplicantName(uMap.getOrDefault(userId, "未知"));

        Long cid = ucMap.getOrDefault(userId, 0L);
        vo.setCollegeName(cMap.getOrDefault(cid, "未知学院"));

        vo.setClassification(cls);
        vo.setStatus("已通过");
        vo.setCreateTime(time);
        return vo;
    }

}