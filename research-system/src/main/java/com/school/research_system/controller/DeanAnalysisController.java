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
