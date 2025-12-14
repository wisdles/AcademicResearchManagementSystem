package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.dto.ProjectDto;
import com.school.research_system.entity.Project;
import com.school.research_system.entity.User;
import com.school.research_system.service.IProjectService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private IProjectService projectService;

    @Autowired
    private IUserService userService;

    // 申报项目接口
    @PostMapping("/add")
    public Result<String> add(@RequestBody ProjectDto dto) {
        // 打印项目的id
        System.out.println("项目 ID: " + dto.getId());
        if (dto.getId() != null) {
            // 如果有 id，说明是修改，调用更新逻辑
            projectService.updateProject(dto);
            return Result.success("修改成功");
        } else {
            // 没有 id，说明是新增
            projectService.createProject(dto);
            return Result.success("申报成功");
        }

    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        projectService.removeById(id);
        return Result.success("删除成功");
    }

    // 查询我的项目列表
    @GetMapping("/my-list")
    public Result<List<Project>> myList() {
        // 1. 获取当前登录人
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 这里如果是基于UserDetails实现，Principal通常是UserDetails对象，getName()是工号
        String username = auth.getName();

        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 2. 查询该用户的所有项目
        List<Project> list = projectService.list(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getUserId, user.getId())
                        .orderByDesc(Project::getCreateTime));

        return Result.success(list);
    }

    // 查询项目详情
    @GetMapping("/detail/{id}")
    public Result<Project> getDetail(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        return Result.success(project);
    }

    // 获取待审核列表 (根据角色自动判断)
    // @GetMapping("/audit-list")
    // public Result<List<Project>> getAuditList() {
    // String username =
    // SecurityContextHolder.getContext().getAuthentication().getName();
    // User user = userService.getOne(new
    // LambdaQueryWrapper<User>().eq(User::getUsername, username));

    // LambdaQueryWrapper<Project> query = new LambdaQueryWrapper<>();

    // if ("DEAN".equals(user.getRoleKey())) {
    // // 院长: 看状态 2 的
    // query.eq(Project::getStatus, 2);
    // } else if ("SEC_TEACHING".equals(user.getRoleKey())) {
    // // 教学秘书: 看状态 1 且 类别是 TEACHING
    // query.eq(Project::getStatus, 1).eq(Project::getCategory, "TEACHING");
    // } else if ("SEC_RESEARCH".equals(user.getRoleKey())) {
    // // 科研秘书: 看状态 1 且 类别是 RESEARCH
    // query.eq(Project::getStatus, 1).eq(Project::getCategory, "RESEARCH");
    // } else {
    // return Result.success(new ArrayList<>()); // 其他人没权限看
    // }

    // query.orderByDesc(Project::getCreateTime);
    // return Result.success(projectService.list(query));
    // }
    // 获取待审核列表 (包含学院隔离 + 类别隔离 + 姓名填充)
    @GetMapping("/audit-list")
    public Result<List<Project>> getAuditList() {
        // 1. 获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (currentUser == null || currentUser.getCollegeId() == null) {
            return Result.success(new ArrayList<>());
        }

        Long myCollegeId = currentUser.getCollegeId();
        String role = currentUser.getRoleKey();

        LambdaQueryWrapper<Project> query = new LambdaQueryWrapper<>();

        // 2. 核心权限控制逻辑
        if ("DEAN".equals(role)) {
            // 院长：只看本学院 + 待院长审批(2)
            query.eq(Project::getStatus, 2)
                    .eq(Project::getCollegeId, myCollegeId);

        } else if ("SEC_TEACHING".equals(role)) {
            // 教学秘书：只看本学院 + 待秘书审批(1) + 教学类
            query.eq(Project::getStatus, 1)
                    .eq(Project::getCollegeId, myCollegeId)
                    .eq(Project::getCategory, "TEACHING");

        } else if ("SEC_RESEARCH".equals(role)) {
            // 科研秘书：只看本学院 + 待秘书审批(1) + 科研类
            query.eq(Project::getStatus, 1)
                    .eq(Project::getCollegeId, myCollegeId)
                    .eq(Project::getCategory, "RESEARCH");

        } else {
            // 其他角色无权查看审核列表
            return Result.success(new ArrayList<>());
        }

        query.orderByDesc(Project::getCreateTime);

        // 3. 查询数据
        List<Project> list = projectService.list(query);

        // 4. 填充申报人姓名 (解决 *** 问题)
        if (!list.isEmpty()) {
            // 收集所有涉及的 userId
            List<Long> userIds = list.stream().map(Project::getUserId).distinct().collect(Collectors.toList());
            // 批量查询用户 (比循环里查库性能好)
            List<User> users = userService.listByIds(userIds);
            Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getRealName));

            // 赋值
            for (Project p : list) {
                p.setApplicantName(userMap.getOrDefault(p.getUserId(), "未知用户"));
            }
        }

        return Result.success(list);
    }

    // 审核接口
    @PostMapping("/audit")
    public Result<String> audit(@RequestBody com.school.research_system.dto.AuditDto dto) {
        System.out.println("======> 进入了 ProjectController 的 audit 方法 <======"); // 加句打印验证
        projectService.auditProject(dto);
        return Result.success("审核完成");
    }
}