package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.SoftwareCopyrightDto;
import com.school.research_system.entity.SoftwareCopyright;
import com.school.research_system.entity.User;
import com.school.research_system.service.ISoftwareCopyrightService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/software")
public class SoftwareCopyrightController {

    @Autowired
    private ISoftwareCopyrightService softService;
    @Autowired
    private IUserService userService;

    @PostMapping("/add")
    public Result<String> add(@RequestBody SoftwareCopyrightDto dto) {
        if (dto.getId() != null) {
            softService.updateSoft(dto);
            return Result.success("修改成功");
        } else {
            softService.createSoft(dto);
            return Result.success("申报成功");
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        softService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/my-list")
    public Result<List<SoftwareCopyright>> myList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        List<SoftwareCopyright> list = softService.list(
                new LambdaQueryWrapper<SoftwareCopyright>()
                        .eq(SoftwareCopyright::getUserId, user.getId())
                        .orderByDesc(SoftwareCopyright::getCreateTime));
        return Result.success(list);
    }

    @GetMapping("/detail/{id}")
    public Result<SoftwareCopyright> getDetail(@PathVariable Long id) {
        return Result.success(softService.getById(id));
    }

    // @GetMapping("/audit-list")
    // public Result<List<SoftwareCopyright>> auditList() {
    // String username =
    // SecurityContextHolder.getContext().getAuthentication().getName();
    // User user = userService.getOne(new
    // LambdaQueryWrapper<User>().eq(User::getUsername, username));

    // LambdaQueryWrapper<SoftwareCopyright> query = new LambdaQueryWrapper<>();
    // if ("DEAN".equals(user.getRoleKey())) {
    // query.eq(SoftwareCopyright::getStatus, 2);
    // } else if (user.getRoleKey() != null && user.getRoleKey().startsWith("SEC_"))
    // {
    // query.eq(SoftwareCopyright::getStatus, 1);
    // } else {
    // return Result.success(new ArrayList<>());
    // }
    // query.orderByDesc(SoftwareCopyright::getCreateTime);
    // return Result.success(softService.list(query));
    // }
    @GetMapping("/audit-list")
    public Result<List<SoftwareCopyright>> getAuditList() {
        // 1. 获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (currentUser == null || currentUser.getCollegeId() == null) {
            return Result.success(new ArrayList<>());
        }

        Long myCollegeId = currentUser.getCollegeId();
        String role = currentUser.getRoleKey();

        // 🔴 核心修正 1：这里必须是 SoftwareCopyright，不能是 Project
        LambdaQueryWrapper<SoftwareCopyright> query = new LambdaQueryWrapper<>();

        // 🔴 核心修正 2：专利表没有 college_id，需要先获取本学院的所有老师 ID
        // 查出本学院所有用户的 ID 列表
        List<Long> collegeUserIds = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getCollegeId, myCollegeId))
                .stream().map(User::getId).collect(Collectors.toList());

        // 如果学院没人，直接返回空
        if (collegeUserIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 基础过滤：只能看本学院老师提交的专利
        query.in(SoftwareCopyright::getUserId, collegeUserIds);

        // // 3. 角色权限控制
        // if ("DEAN".equals(role)) {
        // // 院长：看状态 2
        // query.eq(SoftwareCopyright::getStatus, 2);

        // } else if ("SEC_RESEARCH".equals(role)) {
        // // 科研秘书：看状态 1 (专利通常属于科研，所以只让科研秘书看)
        // query.eq(SoftwareCopyright::getStatus, 1);

        // } else if ("SEC_TEACHING".equals(role)) {
        // // 教学秘书：通常不负责专利（专利属科研），直接返回空，或者根据你们学校规定修改
        // return Result.success(new ArrayList<>());

        // } else {
        // // 其他人没权限
        // return Result.success(new ArrayList<>());
        // }
        if ("SEC_RESEARCH".equals(role)) {
            // 科研秘书 -> 只看 科研类 + 待初审
            query.eq(SoftwareCopyright::getStatus, 1).eq(SoftwareCopyright::getClassification, "科研");
        } else if ("SEC_TEACHING".equals(role)) {
            // 教学秘书 -> 只看 教学类 + 待初审
            query.eq(SoftwareCopyright::getStatus, 1).eq(SoftwareCopyright::getClassification, "教学");
        } else if ("DEAN".equals(role)) {
            // 院长 -> 看所有 待终审 (不管是科研还是教学，只要秘书过了都归院长)
            query.eq(SoftwareCopyright::getStatus, 2);
        } else {
            // 其他人没权限
            return Result.success(new ArrayList<>());
        }
        query.orderByDesc(SoftwareCopyright::getCreateTime);

        // 4. 查询数据 (现在类型对了，不会报错了)
        List<SoftwareCopyright> list = softService.list(query);

        // 5. 填充申报人姓名
        if (!list.isEmpty()) {
            List<Long> userIds = list.stream().map(SoftwareCopyright::getUserId).distinct()
                    .collect(Collectors.toList());
            List<User> users = userService.listByIds(userIds);
            Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getRealName));

            for (SoftwareCopyright p : list) {
                // 记得在 SoftwareCopyrigh 实体类里加 @TableField(exist=false) private String
                // applicantName;
                p.setApplicantName(userMap.getOrDefault(p.getUserId(), "未知用户"));
            }
        }

        return Result.success(list);
    }

    @PostMapping("/audit")
    public Result<String> audit(@RequestBody AuditDto dto) {
        softService.auditSoft(dto);
        return Result.success("审核完成");
    }
}