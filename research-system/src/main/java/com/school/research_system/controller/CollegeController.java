package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.entity.College;
import com.school.research_system.service.ICollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/college")
public class CollegeController {

    @Autowired
    private ICollegeService collegeService;

    /**
     * 获取所有学院列表
     * 用途：
     * 1. 学院管理页面的列表展示
     * 2. 用户管理页面添加用户时的下拉选项
     */
    @GetMapping("/list")
    public Result<List<College>> list() {
        // MyBatis Plus 会自动加上 WHERE is_deleted = 0
        List<College> list = collegeService.list();
        return Result.success(list);
    }

    /**
     * 新增学院
     * 权限：仅管理员
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> add(@RequestBody College college) {
        if (college.getName() == null || college.getName().trim().isEmpty()) {
            return Result.error("学院名称不能为空");
        }

        // 校验名称是否已存在
        long count = collegeService.count(new LambdaQueryWrapper<College>()
                .eq(College::getName, college.getName()));
        if (count > 0) {
            return Result.error("该学院名称已存在");
        }

        collegeService.save(college);
        return Result.success("添加成功");
    }

    /**
     * 修改学院
     * 权限：仅管理员
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> update(@RequestBody College college) {
        if (college.getId() == null) {
            return Result.error("ID不能为空");
        }

        // 校验名称是否与其他学院重复 (排除当前ID)
        long count = collegeService.count(new LambdaQueryWrapper<College>()
                .eq(College::getName, college.getName())
                .ne(College::getId, college.getId())); // ne = not equal

        if (count > 0) {
            return Result.error("该学院名称已与其他学院重复");
        }

        collegeService.updateById(college);
        return Result.success("修改成功");
    }

    /**
     * 删除学院
     * 权限：仅管理员
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> delete(@PathVariable Long id) {
        // 建议扩展：先查询 sys_user 表，如果该学院下还有用户，则禁止删除
        // 目前先做直接删除逻辑
        boolean success = collegeService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败，数据可能不存在");
        }
    }
}