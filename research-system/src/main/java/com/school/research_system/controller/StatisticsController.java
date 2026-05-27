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
import org.springframework.security.core.context.SecurityContextHolder;
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
    private IAwardService awardService;
    @Autowired
    private ICompetitionService competitionService;
    @Autowired
    private ICourseService courseService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ICollegeService collegeService;

    @PostMapping("/dashboard")
    public Result<DashboardVo> getDashboardData(@RequestBody StatsQueryDto query) {
        DashboardVo vo = new DashboardVo();

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
        if (users.isEmpty()) return Result.success(vo);

        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());

        long projCount = projectService.count(new LambdaQueryWrapper<Project>().in(Project::getUserId, userIds));
        long paperCount = paperService.count(new LambdaQueryWrapper<Paper>().in(Paper::getUserId, userIds));
        long patentCount = patentService.count(new LambdaQueryWrapper<Patent>().in(Patent::getUserId, userIds));
        long softCount = softService.count(new LambdaQueryWrapper<SoftwareCopyright>().in(SoftwareCopyright::getUserId, userIds));
        long bookCount = bookService.count(new LambdaQueryWrapper<Book>().in(Book::getUserId, userIds));
        long awardCount = awardService.count(new LambdaQueryWrapper<Award>().in(Award::getUserId, userIds));
        long competitionCount = competitionService.count(new LambdaQueryWrapper<Competition>().in(Competition::getUserId, userIds));
        long courseCount = courseService.count(new LambdaQueryWrapper<Course>().in(Course::getUserId, userIds));

        vo.setTotalAchievements((int) (projCount + paperCount + patentCount + softCount + bookCount + awardCount + competitionCount + courseCount));
        vo.setActiveTeachers(users.size());

        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        long newProj = projectService.count(new LambdaQueryWrapper<Project>().in(Project::getUserId, userIds).ge(Project::getCreateTime, startOfMonth));
        long newPaper = paperService.count(new LambdaQueryWrapper<Paper>().in(Paper::getUserId, userIds).ge(Paper::getCreateTime, startOfMonth));
        long newPatent = patentService.count(new LambdaQueryWrapper<Patent>().in(Patent::getUserId, userIds).ge(Patent::getCreateTime, startOfMonth));
        long newSoft = softService.count(new LambdaQueryWrapper<SoftwareCopyright>().in(SoftwareCopyright::getUserId, userIds).ge(SoftwareCopyright::getCreateTime, startOfMonth));
        long newBook = bookService.count(new LambdaQueryWrapper<Book>().in(Book::getUserId, userIds).ge(Book::getCreateTime, startOfMonth));
        long newAward = awardService.count(new LambdaQueryWrapper<Award>().in(Award::getUserId, userIds).ge(Award::getCreateTime, startOfMonth));
        long newCompetition = competitionService.count(new LambdaQueryWrapper<Competition>().in(Competition::getUserId, userIds).ge(Competition::getCreateTime, startOfMonth));
        long newCourse = courseService.count(new LambdaQueryWrapper<Course>().in(Course::getUserId, userIds).ge(Course::getCreateTime, startOfMonth));
        vo.setCurrentMonthNew((int) (newProj + newPaper + newPatent + newSoft + newBook + newAward + newCompetition + newCourse));

        List<Project> projects = projectService.list(new LambdaQueryWrapper<Project>().in(Project::getUserId, userIds).select(Project::getFunds));
        BigDecimal totalFunds = projects.stream().map(Project::getFunds).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalFunds(totalFunds);

        Map<String, Integer> pieData = new LinkedHashMap<>();
        pieData.put("项目", (int) projCount);
        pieData.put("论文", (int) paperCount);
        pieData.put("专利", (int) patentCount);
        pieData.put("软著", (int) softCount);
        pieData.put("专著", (int) bookCount);
        pieData.put("获奖", (int) awardCount);
        pieData.put("竞赛", (int) competitionCount);
        pieData.put("课程", (int) courseCount);
        vo.setClassificationDistribution(pieData);

        Map<Long, Integer> teacherScoreMap = new HashMap<>();
        for (Long uid : userIds) teacherScoreMap.put(uid, 0);
        countByGroup(teacherScoreMap, projectService, userIds);
        countByGroup(teacherScoreMap, paperService, userIds);
        countByGroup(teacherScoreMap, patentService, userIds);
        countByGroup(teacherScoreMap, softService, userIds);
        countByGroup(teacherScoreMap, bookService, userIds);
        countByGroup(teacherScoreMap, awardService, userIds);
        countByGroup(teacherScoreMap, competitionService, userIds);
        countByGroup(teacherScoreMap, courseService, userIds);

        List<Map<String, Object>> topList = teacherScoreMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    String realName = users.stream().filter(u -> u.getId().equals(entry.getKey())).findFirst().map(User::getRealName).orElse("未知");
                    map.put("name", realName);
                    map.put("count", entry.getValue());
                    return map;
                })
                .filter(m -> (int) m.get("count") > 0)
                .collect(Collectors.toList());
        vo.setTopTeachers(topList);
        vo.setTrendData(calculateTrend(userIds));
        return Result.success(vo);
    }

    private <T> void countByGroup(Map<Long, Integer> scoreMap, IService<T> service, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.select("user_id as userId", "count(*) as c").in("user_id", userIds).groupBy("user_id");
        List<Map<String, Object>> maps = service.listMaps(wrapper);
        for (Map<String, Object> map : maps) {
            if (map.get("userId") != null && map.get("c") != null) {
                Long uid = Long.valueOf(map.get("userId").toString());
                Integer count = Integer.valueOf(map.get("c").toString());
                scoreMap.merge(uid, count, Integer::sum);
            }
        }
    }

    private List<Map<String, Object>> calculateTrend(List<Long> userIds) {
        return new ArrayList<>();
    }

    // 教师个人业绩看板
    @GetMapping("/teacher-dashboard")
    public Result<Map<String, Object>> teacherDashboard() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) return Result.error("用户异常");

        Long userId = user.getId();
        Map<String, Object> result = new HashMap<>();

        // 各类型数量
        long projCount = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getUserId, userId));
        long paperCount = paperService.count(new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, userId));
        long patentCount = patentService.count(new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, userId));
        long softCount = softService.count(new LambdaQueryWrapper<SoftwareCopyright>().eq(SoftwareCopyright::getUserId, userId));
        long bookCount = bookService.count(new LambdaQueryWrapper<Book>().eq(Book::getUserId, userId));
        long awardCount = awardService.count(new LambdaQueryWrapper<Award>().eq(Award::getUserId, userId));
        long competitionCount = competitionService.count(new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, userId));
        long courseCount = courseService.count(new LambdaQueryWrapper<Course>().eq(Course::getUserId, userId));

        long approvedCount = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getUserId, userId).eq(Project::getStatus, 3))
                + paperService.count(new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, userId).eq(Paper::getStatus, 3))
                + patentService.count(new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, userId).eq(Patent::getStatus, 3))
                + softService.count(new LambdaQueryWrapper<SoftwareCopyright>().eq(SoftwareCopyright::getUserId, userId).eq(SoftwareCopyright::getStatus, 3))
                + bookService.count(new LambdaQueryWrapper<Book>().eq(Book::getUserId, userId).eq(Book::getStatus, 3))
                + awardService.count(new LambdaQueryWrapper<Award>().eq(Award::getUserId, userId).eq(Award::getStatus, 3))
                + competitionService.count(new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, userId).eq(Competition::getStatus, 3))
                + courseService.count(new LambdaQueryWrapper<Course>().eq(Course::getUserId, userId).eq(Course::getStatus, 3));

        long pendingCount = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getUserId, userId).in(Project::getStatus, 1, 2))
                + paperService.count(new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, userId).in(Paper::getStatus, 1, 2))
                + patentService.count(new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, userId).in(Patent::getStatus, 1, 2))
                + softService.count(new LambdaQueryWrapper<SoftwareCopyright>().eq(SoftwareCopyright::getUserId, userId).in(SoftwareCopyright::getStatus, 1, 2))
                + bookService.count(new LambdaQueryWrapper<Book>().eq(Book::getUserId, userId).in(Book::getStatus, 1, 2))
                + awardService.count(new LambdaQueryWrapper<Award>().eq(Award::getUserId, userId).in(Award::getStatus, 1, 2))
                + competitionService.count(new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, userId).in(Competition::getStatus, 1, 2))
                + courseService.count(new LambdaQueryWrapper<Course>().eq(Course::getUserId, userId).in(Course::getStatus, 1, 2));

        long rejectedCount = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getUserId, userId).in(Project::getStatus, -1, -2))
                + paperService.count(new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, userId).in(Paper::getStatus, -1, -2))
                + patentService.count(new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, userId).in(Patent::getStatus, -1, -2))
                + softService.count(new LambdaQueryWrapper<SoftwareCopyright>().eq(SoftwareCopyright::getUserId, userId).in(SoftwareCopyright::getStatus, -1, -2))
                + bookService.count(new LambdaQueryWrapper<Book>().eq(Book::getUserId, userId).in(Book::getStatus, -1, -2))
                + awardService.count(new LambdaQueryWrapper<Award>().eq(Award::getUserId, userId).in(Award::getStatus, -1, -2))
                + competitionService.count(new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, userId).in(Competition::getStatus, -1, -2))
                + courseService.count(new LambdaQueryWrapper<Course>().eq(Course::getUserId, userId).in(Course::getStatus, -1, -2));

        long total = projCount + paperCount + patentCount + softCount + bookCount + awardCount + competitionCount + courseCount;

        Map<String, Integer> distMap = new LinkedHashMap<>();
        distMap.put("项目", (int) projCount);
        distMap.put("论文", (int) paperCount);
        distMap.put("专利", (int) patentCount);
        distMap.put("软著", (int) softCount);
        distMap.put("专著", (int) bookCount);
        distMap.put("获奖", (int) awardCount);
        distMap.put("竞赛", (int) competitionCount);
        distMap.put("课程", (int) courseCount);

        Map<String, Integer> statusMap = new LinkedHashMap<>();
        statusMap.put("草稿", (int) (total - approvedCount - pendingCount - rejectedCount));
        statusMap.put("审核中", (int) pendingCount);
        statusMap.put("已通过", (int) approvedCount);
        statusMap.put("已驳回", (int) rejectedCount);

        result.put("totalCount", (int) total);
        result.put("approvedCount", (int) approvedCount);
        result.put("pendingCount", (int) pendingCount);
        result.put("rejectedCount", (int) rejectedCount);
        result.put("achievementDistribution", distMap);
        result.put("statusBreakdown", statusMap);

        return Result.success(result);
    }

    // 教师绩效考核排名
    @PostMapping("/performance")
    public Result<List<Map<String, Object>>> getPerformance(@RequestBody StatsQueryDto query) {
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getRoleKey, "TEACHER");
        if (query.getCollegeId() != null) {
            userQuery.eq(User::getCollegeId, query.getCollegeId());
        }

        List<User> users = userService.list(userQuery);
        List<Map<String, Object>> result = new ArrayList<>();

        for (User teacher : users) {
            Long uid = teacher.getId();
            long projCount = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getUserId, uid).eq(Project::getStatus, 3));
            long paperCount = paperService.count(new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, uid).eq(Paper::getStatus, 3));
            long patentCount = patentService.count(new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, uid).eq(Patent::getStatus, 3));
            long softCount = softService.count(new LambdaQueryWrapper<SoftwareCopyright>().eq(SoftwareCopyright::getUserId, uid).eq(SoftwareCopyright::getStatus, 3));
            long bookCount = bookService.count(new LambdaQueryWrapper<Book>().eq(Book::getUserId, uid).eq(Book::getStatus, 3));
            long awardCount = awardService.count(new LambdaQueryWrapper<Award>().eq(Award::getUserId, uid).eq(Award::getStatus, 3));
            long competitionCount = competitionService.count(new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, uid).eq(Competition::getStatus, 3));
            long courseCount = courseService.count(new LambdaQueryWrapper<Course>().eq(Course::getUserId, uid).eq(Course::getStatus, 3));

            // 计算总分：项目5分，论文3分，专利3分，软著2分，专著3分，获奖3分，竞赛2分，课程2分
            long score = projCount * 5 + paperCount * 3 + patentCount * 3 + softCount * 2 + bookCount * 3 + awardCount * 3 + competitionCount * 2 + courseCount * 2;
            long total = projCount + paperCount + patentCount + softCount + bookCount + awardCount + competitionCount + courseCount;

            Map<String, Object> map = new HashMap<>();
            map.put("userId", uid);
            map.put("realName", teacher.getRealName());
            map.put("username", teacher.getUsername());
            College college = collegeService.getById(teacher.getCollegeId());
            map.put("collegeName", college != null ? college.getName() : "未知");
            map.put("totalAchievements", (int) total);
            map.put("score", (int) score);
            map.put("projectCount", (int) projCount);
            map.put("paperCount", (int) paperCount);
            map.put("patentCount", (int) patentCount);
            map.put("softCount", (int) softCount);
            map.put("bookCount", (int) bookCount);
            map.put("awardCount", (int) awardCount);
            map.put("competitionCount", (int) competitionCount);
            map.put("courseCount", (int) courseCount);
            result.add(map);
        }

        // 按分数降序排列
        result.sort((a, b) -> ((Integer) b.get("score")).compareTo((Integer) a.get("score")));

        // 添加排名
        for (int i = 0; i < result.size(); i++) {
            result.get(i).put("rank", i + 1);
        }

        return Result.success(result);
    }

    @PostMapping("/detail-list")
    public Result<List<StatsExportVo>> getDetailList(@RequestBody StatsDetailQueryDto query) {
        List<StatsExportVo> result = new ArrayList<>();

        List<Long> userIds = null;
        if (query.getCollegeId() != null || (query.getTeacherName() != null && !query.getTeacherName().isEmpty())) {
            LambdaQueryWrapper<User> userQ = new LambdaQueryWrapper<>();
            if (query.getCollegeId() != null) userQ.eq(User::getCollegeId, query.getCollegeId());
            if (query.getTeacherName() != null) userQ.like(User::getRealName, query.getTeacherName());
            List<User> users = userService.list(userQ);
            if (users.isEmpty()) return Result.success(new ArrayList<>());
            userIds = users.stream().map(User::getId).collect(Collectors.toList());
        }

        List<User> allUsers = userService.list();
        List<College> allColleges = collegeService.list();
        Map<Long, String> userMap = allUsers.stream().collect(Collectors.toMap(User::getId, User::getRealName));
        Map<Long, Long> userCollegeMap = allUsers.stream().collect(Collectors.toMap(User::getId, u -> u.getCollegeId() != null ? u.getCollegeId() : 0L));
        Map<Long, String> collegeMap = allColleges.stream().collect(Collectors.toMap(College::getId, College::getName));

        boolean queryAll = query.getTypes() == null || query.getTypes().isEmpty();

        if (queryAll || query.getTypes().contains("project")) {
            LambdaQueryWrapper<Project> q = new LambdaQueryWrapper<>();
            q.eq(Project::getStatus, 3);
            if (userIds != null) q.in(Project::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) q.eq(Project::getClassification, query.getClassification());
            if (query.getDateRange() != null && query.getDateRange().size() == 2) q.between(Project::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            projectService.list(q).forEach(item -> result.add(buildVo("项目", item.getName(), item.getUserId(), item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        if (queryAll || query.getTypes().contains("paper")) {
            LambdaQueryWrapper<Paper> q = new LambdaQueryWrapper<>();
            q.eq(Paper::getStatus, 3);
            if (userIds != null) q.in(Paper::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) q.eq(Paper::getClassification, query.getClassification());
            if (query.getDateRange() != null && query.getDateRange().size() == 2) q.between(Paper::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            paperService.list(q).forEach(item -> result.add(buildVo("论文", item.getTitle(), item.getUserId(), item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        if (queryAll || query.getTypes().contains("patent")) {
            LambdaQueryWrapper<Patent> q = new LambdaQueryWrapper<>();
            q.eq(Patent::getStatus, 3);
            if (userIds != null) q.in(Patent::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) q.eq(Patent::getClassification, query.getClassification());
            if (query.getDateRange() != null && query.getDateRange().size() == 2) q.between(Patent::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            patentService.list(q).forEach(item -> result.add(buildVo("专利", item.getName(), item.getUserId(), item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        if (queryAll || query.getTypes().contains("software")) {
            LambdaQueryWrapper<SoftwareCopyright> q = new LambdaQueryWrapper<>();
            q.eq(SoftwareCopyright::getStatus, 3);
            if (userIds != null) q.in(SoftwareCopyright::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) q.eq(SoftwareCopyright::getClassification, query.getClassification());
            if (query.getDateRange() != null && query.getDateRange().size() == 2) q.between(SoftwareCopyright::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            softService.list(q).forEach(item -> result.add(buildVo("软著", item.getName(), item.getUserId(), item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        if (queryAll || query.getTypes().contains("book")) {
            LambdaQueryWrapper<Book> q = new LambdaQueryWrapper<>();
            q.eq(Book::getStatus, 3);
            if (userIds != null) q.in(Book::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) q.eq(Book::getClassification, query.getClassification());
            if (query.getDateRange() != null && query.getDateRange().size() == 2) q.between(Book::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            bookService.list(q).forEach(item -> result.add(buildVo("专著", item.getName(), item.getUserId(), item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        if (queryAll || query.getTypes().contains("award")) {
            LambdaQueryWrapper<Award> q = new LambdaQueryWrapper<>();
            q.eq(Award::getStatus, 3);
            if (userIds != null) q.in(Award::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) q.eq(Award::getClassification, query.getClassification());
            if (query.getDateRange() != null && query.getDateRange().size() == 2) q.between(Award::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            awardService.list(q).forEach(item -> result.add(buildVo("获奖", item.getAwardName(), item.getUserId(), item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        if (queryAll || query.getTypes().contains("competition")) {
            LambdaQueryWrapper<Competition> q = new LambdaQueryWrapper<>();
            q.eq(Competition::getStatus, 3);
            if (userIds != null) q.in(Competition::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) q.eq(Competition::getClassification, query.getClassification());
            if (query.getDateRange() != null && query.getDateRange().size() == 2) q.between(Competition::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            competitionService.list(q).forEach(item -> result.add(buildVo("竞赛", item.getName(), item.getUserId(), item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        if (queryAll || query.getTypes().contains("course")) {
            LambdaQueryWrapper<Course> q = new LambdaQueryWrapper<>();
            q.eq(Course::getStatus, 3);
            if (userIds != null) q.in(Course::getUserId, userIds);
            if (query.getClassification() != null && !query.getClassification().isEmpty()) q.eq(Course::getClassification, query.getClassification());
            if (query.getDateRange() != null && query.getDateRange().size() == 2) q.between(Course::getCreateTime, query.getDateRange().get(0), query.getDateRange().get(1));
            courseService.list(q).forEach(item -> result.add(buildVo("课程", item.getCourseName(), item.getUserId(), item.getClassification(), item.getCreateTime(), userMap, userCollegeMap, collegeMap)));
        }

        result.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        return Result.success(result);
    }

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