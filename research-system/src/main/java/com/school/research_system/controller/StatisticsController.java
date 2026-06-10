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
    @Autowired
    private com.school.research_system.mapper.AchievementShareMapper shareMapper;

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

    // 教师个人成果明细列表（支持筛选）
    @PostMapping("/my-achievements")
    @SuppressWarnings("unchecked")
    public Result<List<Map<String, Object>>> myAchievementsFiltered(@RequestBody Map<String, Object> params) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (me == null) return Result.error("用户异常");
        Long uid = me.getId();

        String filterType = (String) params.get("type");          // 成果类型，null=全部
        Integer yearFrom = params.get("yearFrom") != null ? Integer.valueOf(params.get("yearFrom").toString()) : null;
        Integer yearTo = params.get("yearTo") != null ? Integer.valueOf(params.get("yearTo").toString()) : null;
        String keyword = (String) params.get("keyword");          // 名称/标题关键词
        String tag = (String) params.get("tag");                  // 标签关键词

        java.time.LocalDateTime from = yearFrom != null ? java.time.LocalDateTime.of(yearFrom, 1, 1, 0, 0) : null;
        java.time.LocalDateTime to = yearTo != null ? java.time.LocalDateTime.of(yearTo, 12, 31, 23, 59) : null;

        List<Map<String, Object>> result = new java.util.ArrayList<>();

        // 每个类型
        if (filterType == null || "project".equals(filterType))
            collect(result, projectService, uid, "project", "项目", from, to, keyword, tag);
        if (filterType == null || "paper".equals(filterType))
            collect(result, paperService, uid, "paper", "论文", from, to, keyword, tag);
        if (filterType == null || "patent".equals(filterType))
            collect(result, patentService, uid, "patent", "专利", from, to, keyword, tag);
        if (filterType == null || "software".equals(filterType))
            collect(result, softService, uid, "software", "软著", from, to, keyword, tag);
        if (filterType == null || "book".equals(filterType))
            collect(result, bookService, uid, "book", "专著", from, to, keyword, tag);
        if (filterType == null || "award".equals(filterType))
            collect(result, awardService, uid, "award", "获奖", from, to, keyword, tag);
        if (filterType == null || "competition".equals(filterType))
            collect(result, competitionService, uid, "competition", "竞赛", from, to, keyword, tag);
        if (filterType == null || "course".equals(filterType))
            collect(result, courseService, uid, "course", "课程", from, to, keyword, tag);

        result.sort((a, b) -> {
            String t1 = (String) a.getOrDefault("createTime", "");
            String t2 = (String) b.getOrDefault("createTime", "");
            return t2.compareTo(t1);
        });
        return Result.success(result);
    }

    // 快速更新成果标签
    @PutMapping("/update-tags")
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Result<String> updateTags(@RequestBody Map<String, String> params) {
        String type = params.get("type");
        Long id = Long.valueOf(params.get("id"));
        String tags = params.get("tags");
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper uw =
            new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>().set("tags", tags).eq("id", id);
        int rows = switch (type) {
            case "project" -> projectService.getBaseMapper().update(null, uw);
            case "paper" -> paperService.getBaseMapper().update(null, uw);
            case "patent" -> patentService.getBaseMapper().update(null, uw);
            case "software" -> softService.getBaseMapper().update(null, uw);
            case "book" -> bookService.getBaseMapper().update(null, uw);
            case "award" -> awardService.getBaseMapper().update(null, uw);
            case "competition" -> competitionService.getBaseMapper().update(null, uw);
            case "course" -> courseService.getBaseMapper().update(null, uw);
            default -> 0;
        };
        return rows > 0 ? Result.success("标签已更新") : Result.error("更新失败");
    }

    // 导出我的成果为 CSV（与筛选条件一致）
    @PostMapping("/my-export")
    @SuppressWarnings("unchecked")
    public Result<String> myExport(@RequestBody Map<String, Object> params) {
        // 复用 myAchievementsFiltered 逻辑
        return Result.success("请通过前端 CSV 导出");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void collect(List<Map<String, Object>> result, com.baomidou.mybatisplus.extension.service.IService s,
            Long uid, String type, String typeLabel, java.time.LocalDateTime from, java.time.LocalDateTime to,
            String keyword, String tag) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper q =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper();
        q.eq("user_id", uid);
        if (from != null) q.ge("create_time", from);
        if (to != null) q.le("create_time", to);

        List<?> list = s.list(q);
        for (Object obj : list) {
            try {
                String name = null;
                // 反射获取名称：依次尝试 getTitle / getAwardName / getCourseName / getName
                for (String mn : new String[]{"getTitle", "getAwardName", "getCourseName", "getName"}) {
                    try { name = (String) obj.getClass().getMethod(mn).invoke(obj); break; } catch (Exception ignored) {}
                }
                if (name == null) continue;

                Integer status = (Integer) obj.getClass().getMethod("getStatus").invoke(obj);
                java.time.LocalDateTime createTime = (java.time.LocalDateTime) obj.getClass().getMethod("getCreateTime").invoke(obj);
                String tags = null;
                try { tags = (String) obj.getClass().getMethod("getTags").invoke(obj); } catch (Exception ignored) {}
                String classification = null;
                try { classification = (String) obj.getClass().getMethod("getClassification").invoke(obj); } catch (Exception ignored) {}

                // 关键词筛选
                if (keyword != null && !keyword.isEmpty() && (name == null || !name.contains(keyword))) continue;
                if (tag != null && !tag.isEmpty() && (tags == null || !tags.contains(tag))) continue;

                Long id = (Long) obj.getClass().getMethod("getId").invoke(obj);
                Map<String, Object> m = new HashMap<>();
                m.put("id", id);
                m.put("type", type);
                m.put("typeLabel", typeLabel);
                m.put("name", name);
                m.put("status", status);
                m.put("statusText", statusText(status));
                m.put("classification", classification != null ? classification : "");
                m.put("tags", tags != null ? tags : "");
                m.put("createTime", createTime != null ? createTime.toString().substring(0, 10) : "");
                result.add(m);
            } catch (Exception ignored) {}
        }
    }

    private String statusText(int s) {
        return switch (s) {
            case 0 -> "草稿"; case 1 -> "待秘书审核"; case 2 -> "待院长审核";
            case 3 -> "已通过"; case -1 -> "秘书驳回"; case -2 -> "院长驳回";
            default -> "未知";
        };
    }

    // 教师绩效考核排名
    @PostMapping("/performance")
    public Result<List<Map<String, Object>>> getPerformance(@RequestBody StatsQueryDto query) {
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getRoleKey, "TEACHER");
        if (query.getCollegeId() != null) {
            userQuery.eq(User::getCollegeId, query.getCollegeId());
        }

        // 年份筛选：用 createTime 范围
        java.time.LocalDateTime yearStart = null, yearEnd = null;
        if (query.getYear() != null) {
            yearStart = java.time.LocalDateTime.of(query.getYear(), 1, 1, 0, 0, 0);
            yearEnd = java.time.LocalDateTime.of(query.getYear(), 12, 31, 23, 59, 59);
        }

        List<User> users = userService.list(userQuery);
        List<Map<String, Object>> result = new ArrayList<>();

        for (User teacher : users) {
            Long uid = teacher.getId();

            // 自己的成果
            LambdaQueryWrapper<Project> projQ = new LambdaQueryWrapper<Project>().eq(Project::getUserId, uid).eq(Project::getStatus, 3);
            LambdaQueryWrapper<Paper> paperQ = new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, uid).eq(Paper::getStatus, 3);
            LambdaQueryWrapper<Patent> patentQ = new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, uid).eq(Patent::getStatus, 3);
            LambdaQueryWrapper<SoftwareCopyright> softQ = new LambdaQueryWrapper<SoftwareCopyright>().eq(SoftwareCopyright::getUserId, uid).eq(SoftwareCopyright::getStatus, 3);
            LambdaQueryWrapper<Book> bookQ = new LambdaQueryWrapper<Book>().eq(Book::getUserId, uid).eq(Book::getStatus, 3);
            LambdaQueryWrapper<Award> awardQ = new LambdaQueryWrapper<Award>().eq(Award::getUserId, uid).eq(Award::getStatus, 3);
            LambdaQueryWrapper<Competition> compQ = new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, uid).eq(Competition::getStatus, 3);
            LambdaQueryWrapper<Course> courseQ = new LambdaQueryWrapper<Course>().eq(Course::getUserId, uid).eq(Course::getStatus, 3);
            if (yearStart != null) {
                projQ.between(Project::getCreateTime, yearStart, yearEnd);
                paperQ.between(Paper::getCreateTime, yearStart, yearEnd);
                patentQ.between(Patent::getCreateTime, yearStart, yearEnd);
                softQ.between(SoftwareCopyright::getCreateTime, yearStart, yearEnd);
                bookQ.between(Book::getCreateTime, yearStart, yearEnd);
                awardQ.between(Award::getCreateTime, yearStart, yearEnd);
                compQ.between(Competition::getCreateTime, yearStart, yearEnd);
                courseQ.between(Course::getCreateTime, yearStart, yearEnd);
            }
            long projCount = projectService.count(projQ);
            long paperCount = paperService.count(paperQ);
            long patentCount = patentService.count(patentQ);
            long softCount = softService.count(softQ);
            long bookCount = bookService.count(bookQ);
            long awardCount = awardService.count(awardQ);
            long competitionCount = competitionService.count(compQ);
            long courseCount = courseService.count(courseQ);

            // 别人共享给我的成果（去重后的唯一成果数，50% 计入分数）
            List<AchievementShare> myShares = shareMapper.selectList(
                new LambdaQueryWrapper<AchievementShare>().eq(AchievementShare::getSharedUserId, uid));
            java.util.Set<String> uniqueShared = new java.util.HashSet<>();
            long sharedProj = 0, sharedPaper = 0, sharedPatent = 0, sharedSoft = 0, sharedBook = 0, sharedAward = 0, sharedComp = 0, sharedCourse = 0;
            for (AchievementShare s : myShares) {
                String key = s.getAchievementType() + "_" + s.getAchievementId();
                if (uniqueShared.add(key)) {
                    switch (s.getAchievementType()) {
                        case "project": sharedProj++; break;
                        case "paper": sharedPaper++; break;
                        case "patent": sharedPatent++; break;
                        case "software": sharedSoft++; break;
                        case "book": sharedBook++; break;
                        case "award": sharedAward++; break;
                        case "competition": sharedComp++; break;
                        case "course": sharedCourse++; break;
                    }
                }
            }

            // 总分：自己成果满分 + 共享成果半价
            long score = projCount * 5 + paperCount * 3 + patentCount * 3 + softCount * 2 + bookCount * 3 + awardCount * 3 + competitionCount * 2 + courseCount * 2;
            long sharedScore = (sharedProj * 5 + sharedPaper * 3 + sharedPatent * 3 + sharedSoft * 2 + sharedBook * 3 + sharedAward * 3 + sharedComp * 2 + sharedCourse * 2) / 2;
            long total = projCount + paperCount + patentCount + softCount + bookCount + awardCount + competitionCount + courseCount + sharedProj + sharedPaper + sharedPatent + sharedSoft + sharedBook + sharedAward + sharedComp + sharedCourse;

            Map<String, Object> map = new HashMap<>();
            map.put("userId", uid);
            map.put("realName", teacher.getRealName());
            map.put("username", teacher.getUsername());
            College college = collegeService.getById(teacher.getCollegeId());
            map.put("collegeName", college != null ? college.getName() : "未知");
            map.put("totalAchievements", (int) total);
            map.put("score", (int) (score + sharedScore));
            map.put("ownCount", (int) (projCount + paperCount + patentCount + softCount + bookCount + awardCount + competitionCount + courseCount));
            map.put("sharedCount", (int) (sharedProj + sharedPaper + sharedPatent + sharedSoft + sharedBook + sharedAward + sharedComp + sharedCourse));
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