package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.entity.*;
import com.school.research_system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/dean")
public class DeanAnalysisController {

    @Autowired private IUserService userService;
    @Autowired private IProjectService projectService;
    @Autowired private IPaperService paperService;
    @Autowired private IPatentService patentService;
    @Autowired private ISoftwareCopyrightService softService;
    @Autowired private IBookService bookService;
    @Autowired private IAwardService awardService;
    @Autowired private ICompetitionService competitionService;
    @Autowired private ICourseService courseService;
    @Autowired private IAnnualTargetService annualTargetService;

    // === 年度目标 ===
    @PostMapping("/target/save")
    public Result<String> saveTarget(@RequestBody Map<String, Object> params) {
        Long collegeId = Long.valueOf(params.get("collegeId").toString());
        Integer year = Integer.valueOf(params.get("year").toString());
        LambdaQueryWrapper<AnnualTarget> q = new LambdaQueryWrapper<>();
        q.eq(AnnualTarget::getCollegeId, collegeId).eq(AnnualTarget::getYear, year);
        AnnualTarget t = annualTargetService.getOne(q);
        if (t == null) { t = new AnnualTarget(); t.setCollegeId(collegeId); t.setYear(year); }
        t.setProjectTarget(toInt(params, "projectTarget"));
        t.setPaperTarget(toInt(params, "paperTarget"));
        t.setPatentTarget(toInt(params, "patentTarget"));
        t.setSoftTarget(toInt(params, "softTarget"));
        t.setBookTarget(toInt(params, "bookTarget"));
        t.setAwardTarget(toInt(params, "awardTarget"));
        t.setCompetitionTarget(toInt(params, "competitionTarget"));
        t.setCourseTarget(toInt(params, "courseTarget"));
        annualTargetService.saveOrUpdate(t);
        return Result.success("保存成功");
    }

    @GetMapping("/target/{collegeId}/{year}")
    public Result<AnnualTarget> getTarget(@PathVariable Long collegeId, @PathVariable Integer year) {
        LambdaQueryWrapper<AnnualTarget> q = new LambdaQueryWrapper<>();
        q.eq(AnnualTarget::getCollegeId, collegeId).eq(AnnualTarget::getYear, year);
        return Result.success(annualTargetService.getOne(q));
    }

    @GetMapping("/target/progress/{collegeId}/{year}")
    public Result<Map<String, Object>> targetProgress(@PathVariable Long collegeId, @PathVariable Integer year) {
        List<User> teachers = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getCollegeId, collegeId).eq(User::getRoleKey, "TEACHER"));
        List<Long> ids = teachers.stream().map(User::getId).toList();

        int proj = countProj(ids), paper = countPaper(ids), patent = countPatent(ids), soft = countSoft(ids);
        int book = countBook(ids), award = countAward(ids), comp = countComp(ids), course = countCourse(ids);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("project", pg("项目", proj));
        r.put("paper", pg("论文", paper));
        r.put("patent", pg("专利", patent));
        r.put("software", pg("软著", soft));
        r.put("book", pg("专著", book));
        r.put("award", pg("获奖", award));
        r.put("competition", pg("竞赛", comp));
        r.put("course", pg("课程", course));

        LambdaQueryWrapper<AnnualTarget> q = new LambdaQueryWrapper<>();
        q.eq(AnnualTarget::getCollegeId, collegeId).eq(AnnualTarget::getYear, year);
        AnnualTarget t = annualTargetService.getOne(q);
        if (t != null) {
            setTg(r, "project", t.getProjectTarget());
            setTg(r, "paper", t.getPaperTarget());
            setTg(r, "patent", t.getPatentTarget());
            setTg(r, "software", t.getSoftTarget());
            setTg(r, "book", t.getBookTarget());
            setTg(r, "award", t.getAwardTarget());
            setTg(r, "competition", t.getCompetitionTarget());
            setTg(r, "course", t.getCourseTarget());
        }
        return Result.success(r);
    }

    // === 决策分析 ===
    @GetMapping("/analysis/keywords/{collegeId}")
    public Result<Map<String, Object>> keywordAnalysis(@PathVariable Long collegeId) {
        List<User> teachers = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getCollegeId, collegeId).eq(User::getRoleKey, "TEACHER"));
        List<Long> ids = teachers.stream().map(User::getId).toList();

        Map<String, Integer> kw = new LinkedHashMap<>();
        for (Project p : projectService.list(new LambdaQueryWrapper<Project>().in(Project::getUserId, ids).eq(Project::getStatus, 3)))
            addKw(kw, p.getName());
        for (Paper p : paperService.list(new LambdaQueryWrapper<Paper>().in(Paper::getUserId, ids).eq(Paper::getStatus, 3)))
            addKw(kw, p.getTitle());

        List<Map<String, Object>> sorted = kw.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())).limit(20)
                .map(e -> { Map<String, Object> m = new HashMap<>(); m.put("name", e.getKey()); m.put("value", e.getValue()); return m; }).toList();
        Map<String, Object> result = new HashMap<>();
        result.put("keywords", sorted);
        result.put("teacherCount", teachers.size());
        return Result.success(result);
    }

    // === 异常数据预警 ===
    @GetMapping("/analysis/anomaly/{collegeId}")
    public Result<List<Map<String, Object>>> anomalyDetection(@PathVariable Long collegeId) {
        List<User> teachers = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getCollegeId, collegeId).eq(User::getRoleKey, "TEACHER"));
        List<Long> ids = teachers.stream().map(User::getId).toList();
        List<Map<String, Object>> anomalies = new ArrayList<>();

        if (ids.isEmpty()) return Result.success(anomalies);

        // 1. 检测论文标题重复
        Map<String, List<Paper>> paperGroups = new HashMap<>();
        for (Paper p : paperService.list(new LambdaQueryWrapper<Paper>().in(Paper::getUserId, ids))) {
            if (p.getTitle() != null) paperGroups.computeIfAbsent(p.getTitle().trim(), k -> new ArrayList<>()).add(p);
        }
        paperGroups.forEach((title, list) -> {
            if (list.size() > 1) {
                Map<String, Object> a = new HashMap<>();
                a.put("type", "论文重复");
                a.put("name", title);
                a.put("count", list.size());
                a.put("level", "warning");
                a.put("desc", "存在 " + list.size() + " 条同名论文记录，请核实是否重复申报");
                anomalies.add(a);
            }
        });

        // 2. 检测项目名称重复
        Map<String, List<Project>> projGroups = new HashMap<>();
        for (Project p : projectService.list(new LambdaQueryWrapper<Project>().in(Project::getUserId, ids))) {
            if (p.getName() != null) projGroups.computeIfAbsent(p.getName().trim(), k -> new ArrayList<>()).add(p);
        }
        projGroups.forEach((name, list) -> {
            if (list.size() > 1) {
                Map<String, Object> a = new HashMap<>();
                a.put("type", "项目重复");
                a.put("name", name);
                a.put("count", list.size());
                a.put("level", "warning");
                a.put("desc", "存在 " + list.size() + " 条同名项目记录");
                anomalies.add(a);
            }
        });

        // 3. 检测专利号重复
        Map<String, List<Patent>> ptGroups = new HashMap<>();
        for (Patent p : patentService.list(new LambdaQueryWrapper<Patent>().in(Patent::getUserId, ids))) {
            if (p.getPatentNo() != null && !p.getPatentNo().isEmpty())
                ptGroups.computeIfAbsent(p.getPatentNo().trim(), k -> new ArrayList<>()).add(p);
        }
        ptGroups.forEach((no, list) -> {
            if (list.size() > 1) {
                Map<String, Object> a = new HashMap<>();
                a.put("type", "专利号重复");
                a.put("name", no);
                a.put("count", list.size());
                a.put("level", "danger");
                a.put("desc", "专利号 " + no + " 被申报 " + list.size() + " 次");
                anomalies.add(a);
            }
        });

        // 4. 检测长期挂起审核 (>30天还在待审核)
        long threshold = System.currentTimeMillis() - 30L * 24 * 3600 * 1000;
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(threshold), java.time.ZoneId.systemDefault());
        long pendingLong = paperService.count(new LambdaQueryWrapper<Paper>().in(Paper::getUserId, ids).in(Paper::getStatus, 1, 2).lt(Paper::getCreateTime, cutoff))
                + projectService.count(new LambdaQueryWrapper<Project>().in(Project::getUserId, ids).in(Project::getStatus, 1, 2).lt(Project::getCreateTime, cutoff));
        if (pendingLong > 0) {
            Map<String, Object> a = new HashMap<>();
            a.put("type", "审核超时");
            a.put("name", "超过30天未审核");
            a.put("count", (int) pendingLong);
            a.put("level", "warning");
            a.put("desc", "本院有 " + pendingLong + " 条成果提交超过30天仍未审核完成");
            anomalies.add(a);
        }

        return Result.success(anomalies);
    }

    // === 资源分配建议 ===
    @GetMapping("/analysis/resource/{collegeId}")
    public Result<List<Map<String, Object>>> resourceSuggestion(@PathVariable Long collegeId) {
        List<User> teachers = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getCollegeId, collegeId).eq(User::getRoleKey, "TEACHER"));
        List<Long> ids = teachers.stream().map(User::getId).toList();

        // 按各成果类型统计本院的成果分布，给出投入建议
        int proj = countProj(ids), paper = countPaper(ids), patent = countPatent(ids);
        int soft = countSoft(ids), book = countBook(ids), award = countAward(ids);

        List<Map<String, Object>> suggestions = new ArrayList<>();
        int total = proj + paper + patent + soft + book + award;
        if (total == 0) return Result.success(suggestions);

        // 短板分析
        Map<String, Integer> typeMap = new LinkedHashMap<>();
        typeMap.put("项目", proj); typeMap.put("论文", paper); typeMap.put("专利", patent);
        typeMap.put("软著", soft); typeMap.put("专著", book); typeMap.put("获奖", award);

        // 找出占比最低的两项作为重点支持方向
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(typeMap.entrySet());
        sorted.sort((a, b) -> a.getValue().compareTo(b.getValue()));

        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            Map.Entry<String, Integer> e = sorted.get(i);
            int percent = e.getValue() * 100 / Math.max(total, 1);
            Map<String, Object> s = new HashMap<>();
            s.put("category", e.getKey());
            s.put("current", e.getValue());
            s.put("percent", percent);
            String level = percent < 5 ? "高优先级" : percent < 15 ? "中优先级" : "低优先级";
            s.put("priority", level);
            s.put("suggestion", String.format("本院 %s 类成果仅占 %d%%，建议加大该方向资源投入与人才培养", e.getKey(), percent));
            suggestions.add(s);
        }

        // 教师数 vs 人均产出
        if (!teachers.isEmpty()) {
            double avg = (double) total / teachers.size();
            Map<String, Object> s = new HashMap<>();
            s.put("category", "整体产出");
            s.put("current", total);
            s.put("percent", (int) (avg * 10));
            s.put("priority", avg < 2 ? "高优先级" : avg < 5 ? "中优先级" : "低优先级");
            s.put("suggestion", String.format("本院共 %d 位教师，人均成果 %.1f 项%s",
                    teachers.size(), avg, avg < 2 ? "，建议加强科研氛围与激励" : ""));
            suggestions.add(s);
        }

        return Result.success(suggestions);
    }

    @GetMapping("/analysis/talent/{collegeId}")
    public Result<List<Map<String, Object>>> talentAnalysis(@PathVariable Long collegeId) {
        List<User> teachers = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getCollegeId, collegeId).eq(User::getRoleKey, "TEACHER"));
        List<Map<String, Object>> list = new ArrayList<>();
        for (User t : teachers) {
            Long uid = t.getId();
            int total = (int) (projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getUserId, uid).eq(Project::getStatus, 3))
                    + paperService.count(new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, uid).eq(Paper::getStatus, 3))
                    + patentService.count(new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, uid).eq(Patent::getStatus, 3))
                    + softService.count(new LambdaQueryWrapper<SoftwareCopyright>().eq(SoftwareCopyright::getUserId, uid).eq(SoftwareCopyright::getStatus, 3))
                    + bookService.count(new LambdaQueryWrapper<Book>().eq(Book::getUserId, uid).eq(Book::getStatus, 3))
                    + awardService.count(new LambdaQueryWrapper<Award>().eq(Award::getUserId, uid).eq(Award::getStatus, 3))
                    + competitionService.count(new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, uid).eq(Competition::getStatus, 3))
                    + courseService.count(new LambdaQueryWrapper<Course>().eq(Course::getUserId, uid).eq(Course::getStatus, 3)));
            Map<String, Object> m = new HashMap<>();
            m.put("name", t.getRealName()); m.put("total", total);
            m.put("level", total >= 10 ? "骨干" : total >= 5 ? "中坚" : total > 0 ? "新锐" : "待激活");
            list.add(m);
        }
        list.sort((a, b) -> ((Integer) b.get("total")).compareTo((Integer) a.get("total")));
        return Result.success(list);
    }

    // --- helpers ---
    private int countProj(List<Long> ids) { return ids.isEmpty() ? 0 : (int) projectService.count(new LambdaQueryWrapper<Project>().in(Project::getUserId, ids).eq(Project::getStatus, 3)); }
    private int countPaper(List<Long> ids) { return ids.isEmpty() ? 0 : (int) paperService.count(new LambdaQueryWrapper<Paper>().in(Paper::getUserId, ids).eq(Paper::getStatus, 3)); }
    private int countPatent(List<Long> ids) { return ids.isEmpty() ? 0 : (int) patentService.count(new LambdaQueryWrapper<Patent>().in(Patent::getUserId, ids).eq(Patent::getStatus, 3)); }
    private int countSoft(List<Long> ids) { return ids.isEmpty() ? 0 : (int) softService.count(new LambdaQueryWrapper<SoftwareCopyright>().in(SoftwareCopyright::getUserId, ids).eq(SoftwareCopyright::getStatus, 3)); }
    private int countBook(List<Long> ids) { return ids.isEmpty() ? 0 : (int) bookService.count(new LambdaQueryWrapper<Book>().in(Book::getUserId, ids).eq(Book::getStatus, 3)); }
    private int countAward(List<Long> ids) { return ids.isEmpty() ? 0 : (int) awardService.count(new LambdaQueryWrapper<Award>().in(Award::getUserId, ids).eq(Award::getStatus, 3)); }
    private int countComp(List<Long> ids) { return ids.isEmpty() ? 0 : (int) competitionService.count(new LambdaQueryWrapper<Competition>().in(Competition::getUserId, ids).eq(Competition::getStatus, 3)); }
    private int countCourse(List<Long> ids) { return ids.isEmpty() ? 0 : (int) courseService.count(new LambdaQueryWrapper<Course>().in(Course::getUserId, ids).eq(Course::getStatus, 3)); }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> pg(String name, int actual) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name); m.put("actual", actual); m.put("target", 0); m.put("percent", 100);
        return m;
    }

    @SuppressWarnings("unchecked")
    private void setTg(Map<String, Object> r, String key, Integer val) {
        if (val == null) return;
        Map<String, Object> m = (Map<String, Object>) r.get(key);
        m.put("target", val);
        int actual = (int) m.get("actual");
        m.put("percent", val > 0 ? Math.min(100, actual * 100 / val) : 100);
    }

    private int toInt(Map<String, Object> p, String k) {
        Object v = p.get(k);
        return v != null ? Integer.parseInt(v.toString()) : 0;
    }

    private void addKw(Map<String, Integer> map, String name) {
        if (name == null || name.length() < 2) return;
        for (int i = 0; i <= name.length() - 2; i++) {
            map.merge(name.substring(i, i + 2), 1, Integer::sum);
        }
    }
}
