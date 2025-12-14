package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.entity.Resume;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.ResumeMapper;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private IUserService userService;

    // 获取我的简历
    @GetMapping("/my")
    public Result<Resume> getMyResume() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Resume resume = resumeMapper.selectOne(new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, user.getId()));
        // 如果还没有简历，返回一个空对象给前端，不要返回 null 报错
        if (resume == null) {
            resume = new Resume();
            resume.setUserId(user.getId()); // 标记一下 ID
        }
        return Result.success(resume);
    }

    // 保存/更新简历
    @PostMapping("/save")
    public Result<String> save(@RequestBody Resume resume) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 查旧记录
        Resume exist = resumeMapper.selectOne(new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, user.getId()));

        if (exist == null) {
            // 新增
            resume.setUserId(user.getId());
            resumeMapper.insert(resume);
        } else {
            // 更新
            resume.setId(exist.getId());
            resume.setUserId(user.getId()); // 确保 ID 不被篡改
            resumeMapper.updateById(resume);
        }
        return Result.success("保存成功");
    }
}