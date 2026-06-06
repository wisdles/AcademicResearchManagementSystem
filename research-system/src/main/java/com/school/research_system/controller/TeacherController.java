package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.entity.*;
import com.school.research_system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired private IUserService userService;
    @Autowired private IProjectService projectService;
    @Autowired private IPaperService paperService;
    @Autowired private IPatentService patentService;
    @Autowired private ISoftwareCopyrightService softService;
    @Autowired private IBookService bookService;
    @Autowired private IAwardService awardService;
    @Autowired private ICompetitionService competitionService;
    @Autowired private ICourseService courseService;
    @Autowired private ICollegeService collegeService;

    @GetMapping("/profile")
    public Result<Map<String, Object>> myProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        Long uid = user.getId();

        Map<String, Object> r = new HashMap<>();
        r.put("realName", user.getRealName());
        r.put("username", user.getUsername());
        College c = collegeService.getById(user.getCollegeId());
        r.put("collegeName", c != null ? c.getName() : "未知");

        long proj = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getUserId, uid).eq(Project::getStatus, 3));
        long paper = paperService.count(new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, uid).eq(Paper::getStatus, 3));
        long patent = patentService.count(new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, uid).eq(Patent::getStatus, 3));
        long soft = softService.count(new LambdaQueryWrapper<SoftwareCopyright>().eq(SoftwareCopyright::getUserId, uid).eq(SoftwareCopyright::getStatus, 3));
        long book = bookService.count(new LambdaQueryWrapper<Book>().eq(Book::getUserId, uid).eq(Book::getStatus, 3));
        long award = awardService.count(new LambdaQueryWrapper<Award>().eq(Award::getUserId, uid).eq(Award::getStatus, 3));
        long comp = competitionService.count(new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, uid).eq(Competition::getStatus, 3));
        long course = courseService.count(new LambdaQueryWrapper<Course>().eq(Course::getUserId, uid).eq(Course::getStatus, 3));
        long total = proj + paper + patent + soft + book + award + comp + course;

        Map<String, Integer> dist = new LinkedHashMap<>();
        dist.put("项目", (int) proj);
        dist.put("论文", (int) paper);
        dist.put("专利", (int) patent);
        dist.put("软著", (int) soft);
        dist.put("专著", (int) book);
        dist.put("获奖", (int) award);
        dist.put("竞赛", (int) comp);
        dist.put("课程", (int) course);

        r.put("totalCount", (int) total);
        r.put("distribution", dist);
        return Result.success(r);
    }

    @GetMapping("/export")
    public Result<List<Map<String, String>>> export() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        Long uid = user.getId();
        List<Map<String, String>> list = new ArrayList<>();

        projectService.list(new LambdaQueryWrapper<Project>().eq(Project::getUserId, uid).eq(Project::getStatus, 3))
                .forEach(p -> { Map<String,String> m=new HashMap<>(); m.put("type","项目"); m.put("name",p.getName()); list.add(m); });
        paperService.list(new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, uid).eq(Paper::getStatus, 3))
                .forEach(p -> { Map<String,String> m=new HashMap<>(); m.put("type","论文"); m.put("name",p.getTitle()); list.add(m); });
        patentService.list(new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, uid).eq(Patent::getStatus, 3))
                .forEach(p -> { Map<String,String> m=new HashMap<>(); m.put("type","专利"); m.put("name",p.getName()); list.add(m); });
        softService.list(new LambdaQueryWrapper<SoftwareCopyright>().eq(SoftwareCopyright::getUserId, uid).eq(SoftwareCopyright::getStatus, 3))
                .forEach(p -> { Map<String,String> m=new HashMap<>(); m.put("type","软著"); m.put("name",p.getName()); list.add(m); });
        bookService.list(new LambdaQueryWrapper<Book>().eq(Book::getUserId, uid).eq(Book::getStatus, 3))
                .forEach(p -> { Map<String,String> m=new HashMap<>(); m.put("type","专著"); m.put("name",p.getName()); list.add(m); });
        awardService.list(new LambdaQueryWrapper<Award>().eq(Award::getUserId, uid).eq(Award::getStatus, 3))
                .forEach(p -> { Map<String,String> m=new HashMap<>(); m.put("type","获奖"); m.put("name",p.getAwardName()); list.add(m); });
        competitionService.list(new LambdaQueryWrapper<Competition>().eq(Competition::getUserId, uid).eq(Competition::getStatus, 3))
                .forEach(p -> { Map<String,String> m=new HashMap<>(); m.put("type","竞赛"); m.put("name",p.getName()); list.add(m); });
        courseService.list(new LambdaQueryWrapper<Course>().eq(Course::getUserId, uid).eq(Course::getStatus, 3))
                .forEach(p -> { Map<String,String> m=new HashMap<>(); m.put("type","课程"); m.put("name",p.getCourseName()); list.add(m); });

        return Result.success(list);
    }

    // 项目申报时自动引用已有成果
    @GetMapping("/my-achievements")
    public Result<Map<String, Object>> myAchievements() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        Long uid = user.getId();

        Map<String, Object> r = new HashMap<>();
        r.put("papers", paperService.list(new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, uid).eq(Paper::getStatus, 3))
                .stream().map(p -> { Map<String,String> m=new HashMap<>(); m.put("id",String.valueOf(p.getId())); m.put("title",p.getTitle()); m.put("journal",p.getJournalName()); return m; }).toList());
        r.put("projects", projectService.list(new LambdaQueryWrapper<Project>().eq(Project::getUserId, uid).eq(Project::getStatus, 3))
                .stream().map(p -> { Map<String,String> m=new HashMap<>(); m.put("id",String.valueOf(p.getId())); m.put("name",p.getName()); return m; }).toList());
        r.put("patents", patentService.list(new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, uid).eq(Patent::getStatus, 3))
                .stream().map(p -> { Map<String,String> m=new HashMap<>(); m.put("id",String.valueOf(p.getId())); m.put("name",p.getName()); m.put("no",p.getPatentNo()); return m; }).toList());
        r.put("totalPaper", (int) paperService.count(new LambdaQueryWrapper<Paper>().eq(Paper::getUserId, uid).eq(Paper::getStatus, 3)));
        r.put("totalPatent", (int) patentService.count(new LambdaQueryWrapper<Patent>().eq(Patent::getUserId, uid).eq(Patent::getStatus, 3)));
        r.put("totalProject", (int) projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getUserId, uid).eq(Project::getStatus, 3)));

        return Result.success(r);
    }
}
