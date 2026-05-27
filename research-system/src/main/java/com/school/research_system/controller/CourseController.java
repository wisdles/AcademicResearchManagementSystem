package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.CourseDto;
import com.school.research_system.entity.Course;
import com.school.research_system.entity.User;
import com.school.research_system.service.ICourseService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程建设控制器
 */
@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IUserService userService;

    // 申报/修改
    @PostMapping("/add")
    public Result<String> add(@RequestBody CourseDto dto) {
        if (dto.getId() != null) {
            courseService.updateCourse(dto);
            return Result.success("修改成功");
        } else {
            courseService.createCourse(dto);
            return Result.success("申报成功");
        }
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success("删除成功");
    }

    // 撤回提交
    @PutMapping("/withdraw/{id}")
    public Result<String> withdraw(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Course course = courseService.getById(id);
        if (course == null) return Result.error("记录不存在");
        if (!course.getUserId().equals(user.getId())) return Result.error("无权操作");
        if (course.getStatus() != 1) return Result.error("只有待秘书审核状态才能撤回");

        course.setStatus(0);
        courseService.updateById(course);
        return Result.success("撤回成功");
    }

    // 我的课程列表
    @GetMapping("/my-list")
    public Result<List<Course>> myList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        List<Course> list = courseService.list(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getUserId, user.getId())
                        .orderByDesc(Course::getCreateTime));
        return Result.success(list);
    }

    // 查询详情
    @GetMapping("/detail/{id}")
    public Result<Course> getDetail(@PathVariable Long id) {
        Course course = courseService.getById(id);
        if (course == null) {
            return Result.error("记录不存在");
        }
        return Result.success(course);
    }

    // 获取审核列表
    @GetMapping("/audit-list")
    public Result<List<Course>> getAuditList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (currentUser == null || currentUser.getCollegeId() == null) {
            return Result.success(new ArrayList<>());
        }

        Long myCollegeId = currentUser.getCollegeId();
        String role = currentUser.getRoleKey();

        // 获取本学院所有教师
        List<Long> collegeUserIds = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getCollegeId, myCollegeId))
                .stream().map(User::getId).collect(Collectors.toList());

        if (collegeUserIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        LambdaQueryWrapper<Course> query = new LambdaQueryWrapper<>();
        query.in(Course::getUserId, collegeUserIds);

        // 按角色过滤
        if ("SEC_RESEARCH".equals(role)) {
            query.eq(Course::getStatus, 1).eq(Course::getClassification, "科研");
        } else if ("SEC_TEACHING".equals(role)) {
            query.eq(Course::getStatus, 1).eq(Course::getClassification, "教学");
        } else if ("DEAN".equals(role)) {
            query.eq(Course::getStatus, 2);
        } else {
            return Result.success(new ArrayList<>());
        }

        query.orderByDesc(Course::getCreateTime);
        List<Course> list = courseService.list(query);

        // 填充申报人姓名
        if (!list.isEmpty()) {
            List<Long> userIds = list.stream().map(Course::getUserId).distinct().collect(Collectors.toList());
            List<User> users = userService.listByIds(userIds);
            Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getRealName));
            for (Course c : list) {
                c.setApplicantName(userMap.getOrDefault(c.getUserId(), "未知用户"));
            }
        }

        return Result.success(list);
    }

    // 审核
    @PostMapping("/audit")
    public Result<String> audit(@RequestBody AuditDto dto) {
        courseService.auditCourse(dto);
        return Result.success("审核完成");
    }

}