package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.PaperDto;
import com.school.research_system.entity.Paper;
import com.school.research_system.entity.User;
import com.school.research_system.service.IPaperService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/paper")
public class PaperController {

    @Autowired
    private IPaperService paperService;

    @Autowired
    private IUserService userService;

    // 新增 or 修改
    @PostMapping("/add")
    public Result<String> add(@RequestBody PaperDto dto) {
        if (dto.getId() != null) {
            paperService.updatePaper(dto);
            return Result.success("修改成功");
        } else {
            paperService.createPaper(dto);
            return Result.success("申报成功");
        }
    }

    // 删除草稿
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        paperService.removeById(id);
        return Result.success("删除成功");
    }

    // 我的论文列表
    @GetMapping("/my-list")
    public Result<List<Paper>> myList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        List<Paper> list = paperService.list(
                new LambdaQueryWrapper<Paper>()
                        .eq(Paper::getUserId, user.getId())
                        .orderByDesc(Paper::getCreateTime));
        return Result.success(list);
    }

    // 详情
    @GetMapping("/detail/{id}")
    public Result<Paper> getDetail(@PathVariable Long id) {
        Paper paper = paperService.getById(id);
        if (paper == null)
            return Result.error("不存在");
        return Result.success(paper);
    }

    // 审核列表
    // @GetMapping("/audit-list")
    // public Result<List<Paper>> auditList() {
    // String username =
    // SecurityContextHolder.getContext().getAuthentication().getName();
    // User user = userService.getOne(new
    // LambdaQueryWrapper<User>().eq(User::getUsername, username));

    // LambdaQueryWrapper<Paper> query = new LambdaQueryWrapper<>();

    // if ("DEAN".equals(user.getRoleKey())) {
    // // 院长看状态 2
    // query.eq(Paper::getStatus, 2);
    // } else if (user.getRoleKey() != null && user.getRoleKey().startsWith("SEC_"))
    // {
    // // 秘书看状态 1 (通常论文归科研秘书管，这里简化为所有秘书可见，或者你可以加 .eq(category...) 如果有的话)
    // query.eq(Paper::getStatus, 1);
    // } else {
    // return Result.success(new ArrayList<>());
    // }

    // query.orderByDesc(Paper::getCreateTime);
    // return Result.success(paperService.list(query));
    // }
    @GetMapping("/audit-list")
    public Result<List<Paper>> getAuditList() {
        // 1. 获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (currentUser == null || currentUser.getCollegeId() == null) {
            return Result.success(new ArrayList<>());
        }

        Long myCollegeId = currentUser.getCollegeId();
        String role = currentUser.getRoleKey();

        // 🔴 核心修正 1：这里必须是 Paper，不能是 Project
        LambdaQueryWrapper<Paper> query = new LambdaQueryWrapper<>();

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
        query.in(Paper::getUserId, collegeUserIds);

        // // 3. 角色权限控制
        // if ("DEAN".equals(role)) {
        // // 院长：看状态 2
        // query.eq(Paper::getStatus, 2);

        // } else if ("SEC_RESEARCH".equals(role)) {
        // // 科研秘书：看状态 1 (专利通常属于科研，所以只让科研秘书看)
        // query.eq(Paper::getStatus, 1);

        // } else if ("SEC_TEACHING".equals(role)) {
        // // 教学秘书：通常不负责专利（专利属科研），直接返回空，或者根据你们学校规定修改
        // return Result.success(new ArrayList<>());

        // }
        // 🟢 核心：根据秘书类型分流
        if ("SEC_RESEARCH".equals(role)) {
            // 科研秘书 -> 只看 科研类 + 待初审
            query.eq(Paper::getStatus, 1).eq(Paper::getCategory, "RESEARCH");
        } else if ("SEC_TEACHING".equals(role)) {
            // 教学秘书 -> 只看 教学类 + 待初审
            query.eq(Paper::getStatus, 1).eq(Paper::getCategory, "TEACHING");
        } else if ("DEAN".equals(role)) {
            // 院长 -> 看所有 待终审 (不管是科研还是教学，只要秘书过了都归院长)
            query.eq(Paper::getStatus, 2);
        } else {
            // 其他人没权限
            return Result.success(new ArrayList<>());
        }

        query.orderByDesc(Paper::getCreateTime);

        // 4. 查询数据 (现在类型对了，不会报错了)
        List<Paper> list = paperService.list(query);

        // 5. 填充申报人姓名
        if (!list.isEmpty()) {
            List<Long> userIds = list.stream().map(Paper::getUserId).distinct().collect(Collectors.toList());
            List<User> users = userService.listByIds(userIds);
            Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getRealName));

            for (Paper p : list) {
                // 记得在 Paper 实体类里加 @TableField(exist=false) private String applicantName;
                p.setApplicantName(userMap.getOrDefault(p.getUserId(), "未知用户"));
            }
        }

        return Result.success(list);
    }

    // 审核动作
    @PostMapping("/audit")
    public Result<String> audit(@RequestBody AuditDto dto) {
        paperService.auditPaper(dto);
        return Result.success("审核完成");
    }
}