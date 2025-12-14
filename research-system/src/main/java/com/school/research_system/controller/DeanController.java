package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.dto.TeacherPortfolioVo;
import com.school.research_system.entity.Paper;
import com.school.research_system.entity.Project;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.PaperMapper;
import com.school.research_system.mapper.ProjectMapper;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/dean")
public class DeanController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ProjectMapper projectMapper; // 直接注入Mapper查数据

    @Autowired
    private PaperMapper paperMapper;

    /**
     * 院长查询老师科研档案
     * 
     * @param keyword 老师姓名(模糊) 或 工号(精确)
     */
    @PreAuthorize("hasAuthority('DEAN')") // 暂时注释掉，你可以后面开启权限控制
    @GetMapping("/search-teacher")
    public Result<List<TeacherPortfolioVo>> searchTeacher(@RequestParam String keyword) {

        // 1. 先根据关键字查人
        // 逻辑：姓名包含 keyword 或者 工号等于 keyword
        List<User> teachers = userService.list(new LambdaQueryWrapper<User>()
                .like(User::getRealName, keyword)
                .or()
                .eq(User::getUsername, keyword));

        if (teachers.isEmpty()) {
            return Result.error("未找到匹配的教师");
        }

        List<TeacherPortfolioVo> resultList = new ArrayList<>();

        // 2. 遍历查到的每一个老师，去捞他的项目和论文
        for (User teacher : teachers) {
            TeacherPortfolioVo vo = new TeacherPortfolioVo();
            vo.setUserId(teacher.getId());
            vo.setRealName(teacher.getRealName());
            vo.setUsername(teacher.getUsername());
            // vo.setCollegeName(...) 如果需要学院名，这里得再查一下学院表，或者前端只展示ID

            // 2.1 查项目 (按时间倒序)
            List<Project> projects = projectMapper.selectList(new LambdaQueryWrapper<Project>()
                    .eq(Project::getUserId, teacher.getId())
                    .orderByDesc(Project::getCreateTime));
            vo.setProjectList(projects);

            // 2.2 查论文 (按时间倒序)
            List<Paper> papers = paperMapper.selectList(new LambdaQueryWrapper<Paper>()
                    .eq(Paper::getUserId, teacher.getId())
                    .orderByDesc(Paper::getCreateTime));
            vo.setPaperList(papers);

            resultList.add(vo);
        }

        return Result.success(resultList);
    }
}