package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.AwardDto;
import com.school.research_system.entity.Award;
import com.school.research_system.entity.User;
import com.school.research_system.service.IAwardService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获奖成果控制器
 */
@RestController
@RequestMapping("/award")
public class AwardController {

    @Autowired
    private IAwardService awardService;

    @Autowired
    private IUserService userService;

    // 申报/修改
    @PostMapping("/add")
    public Result<String> add(@RequestBody AwardDto dto) {
        if (dto.getId() != null) {
            awardService.updateAward(dto);
            return Result.success("修改成功");
        } else {
            awardService.createAward(dto);
            return Result.success("申报成功");
        }
    }

    // 删除（草稿状态下）
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        awardService.removeById(id);
        return Result.success("删除成功");
    }

    // 撤回提交
    @PutMapping("/withdraw/{id}")
    public Result<String> withdraw(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Award award = awardService.getById(id);
        if (award == null) return Result.error("记录不存在");
        if (!award.getUserId().equals(user.getId())) return Result.error("无权操作");
        if (award.getStatus() != 1) return Result.error("只有待秘书审核状态才能撤回");

        award.setStatus(0);
        awardService.updateById(award);
        return Result.success("撤回成功");
    }

    // 查询我的获奖列表
    @GetMapping("/my-list")
    public Result<List<Award>> myList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        List<Award> list = awardService.list(
                new LambdaQueryWrapper<Award>()
                        .eq(Award::getUserId, user.getId())
                        .orderByDesc(Award::getCreateTime));
        return Result.success(list);
    }

    // 查询详情
    @GetMapping("/detail/{id}")
    public Result<Award> getDetail(@PathVariable Long id) {
        Award award = awardService.getById(id);
        if (award == null) {
            return Result.error("记录不存在");
        }
        return Result.success(award);
    }

    // 获取审核列表
    @GetMapping("/audit-list")
    public Result<List<Award>> getAuditList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (currentUser == null || currentUser.getCollegeId() == null) {
            return Result.success(new ArrayList<>());
        }

        Long myCollegeId = currentUser.getCollegeId();
        String role = currentUser.getRoleKey();

        // 获取本学院所有教师的ID
        List<Long> collegeUserIds = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getCollegeId, myCollegeId))
                .stream().map(User::getId).collect(Collectors.toList());

        if (collegeUserIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        LambdaQueryWrapper<Award> query = new LambdaQueryWrapper<>();
        query.in(Award::getUserId, collegeUserIds);

        // 按角色过滤审核状态
        if ("SEC_RESEARCH".equals(role)) {
            query.eq(Award::getStatus, 1).eq(Award::getClassification, "科研");
        } else if ("SEC_TEACHING".equals(role)) {
            query.eq(Award::getStatus, 1).eq(Award::getClassification, "教学");
        } else if ("DEAN".equals(role)) {
            query.eq(Award::getStatus, 2);
        } else {
            return Result.success(new ArrayList<>());
        }

        query.orderByDesc(Award::getCreateTime);
        List<Award> list = awardService.list(query);

        // 填充申报人姓名
        if (!list.isEmpty()) {
            List<Long> userIds = list.stream().map(Award::getUserId).distinct().collect(Collectors.toList());
            List<User> users = userService.listByIds(userIds);
            Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getRealName));
            for (Award a : list) {
                a.setApplicantName(userMap.getOrDefault(a.getUserId(), "未知用户"));
            }
        }

        return Result.success(list);
    }

    // 审核
    @PostMapping("/audit")
    public Result<String> audit(@RequestBody AuditDto dto) {
        awardService.auditAward(dto);
        return Result.success("审核完成");
    }

}