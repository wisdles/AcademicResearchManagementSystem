package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.CompetitionDto;
import com.school.research_system.entity.Competition;
import com.school.research_system.entity.User;
import com.school.research_system.service.ICompetitionService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 指导学生竞赛获奖控制器
 */
@RestController
@RequestMapping("/competition")
public class CompetitionController {

    @Autowired
    private ICompetitionService competitionService;

    @Autowired
    private IUserService userService;

    // 申报/修改
    @PostMapping("/add")
    public Result<String> add(@RequestBody CompetitionDto dto) {
        if (dto.getId() != null) {
            competitionService.updateCompetition(dto);
            return Result.success("修改成功");
        } else {
            competitionService.createCompetition(dto);
            return Result.success("申报成功");
        }
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        competitionService.removeById(id);
        return Result.success("删除成功");
    }

    // 撤回提交
    @PutMapping("/withdraw/{id}")
    public Result<String> withdraw(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Competition competition = competitionService.getById(id);
        if (competition == null) return Result.error("记录不存在");
        if (!competition.getUserId().equals(user.getId())) return Result.error("无权操作");
        if (competition.getStatus() != 1) return Result.error("只有待秘书审核状态才能撤回");

        competition.setStatus(0);
        competitionService.updateById(competition);
        return Result.success("撤回成功");
    }

    // 我的竞赛列表
    @GetMapping("/my-list")
    public Result<List<Competition>> myList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        List<Competition> list = competitionService.list(
                new LambdaQueryWrapper<Competition>()
                        .eq(Competition::getUserId, user.getId())
                        .orderByDesc(Competition::getCreateTime));
        return Result.success(list);
    }

    // 查询详情
    @GetMapping("/detail/{id}")
    public Result<Competition> getDetail(@PathVariable Long id) {
        Competition competition = competitionService.getById(id);
        if (competition == null) {
            return Result.error("记录不存在");
        }
        return Result.success(competition);
    }

    // 获取审核列表
    @GetMapping("/audit-list")
    public Result<List<Competition>> getAuditList() {
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

        LambdaQueryWrapper<Competition> query = new LambdaQueryWrapper<>();
        query.in(Competition::getUserId, collegeUserIds);

        // 按角色过滤
        if ("SEC_RESEARCH".equals(role)) {
            query.eq(Competition::getStatus, 1).eq(Competition::getClassification, "科研");
        } else if ("SEC_TEACHING".equals(role)) {
            query.eq(Competition::getStatus, 1).eq(Competition::getClassification, "教学");
        } else if ("DEAN".equals(role)) {
            query.eq(Competition::getStatus, 2);
        } else {
            return Result.success(new ArrayList<>());
        }

        query.orderByDesc(Competition::getCreateTime);
        List<Competition> list = competitionService.list(query);

        // 填充申报人姓名
        if (!list.isEmpty()) {
            List<Long> userIds = list.stream().map(Competition::getUserId).distinct().collect(Collectors.toList());
            List<User> users = userService.listByIds(userIds);
            Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getRealName));
            for (Competition c : list) {
                c.setApplicantName(userMap.getOrDefault(c.getUserId(), "未知用户"));
            }
        }

        return Result.success(list);
    }

    // 审核
    @PostMapping("/audit")
    public Result<String> audit(@RequestBody AuditDto dto) {
        competitionService.auditCompetition(dto);
        return Result.success("审核完成");
    }

}