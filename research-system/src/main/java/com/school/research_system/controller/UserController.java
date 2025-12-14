package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.entity.User;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    // 测试接口：获取所有用户
    @GetMapping("/list")
    public Result<List<User>> list() {
        // MP 的 list() 方法会自动过滤 is_deleted=1 的数据
        List<User> list = userService.list();
        return Result.success(list);
    }

    // 👇 新增：管理员添加用户接口
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')") // 暂时注释，后续可开启权限校验
    public Result<String> add(@RequestBody com.school.research_system.dto.UserAddDto dto) {
        // 简单的参数校验
        if (dto.getUsername() == null || dto.getRoleKey() == null) {
            return Result.error("工号和角色不能为空");
        }

        userService.createUser(dto);
        return Result.success("用户创建成功，初始密码为: abc123");
    }

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // 修改密码接口
    @PostMapping("/update-password")
    public Result<String> updatePassword(@RequestBody com.school.research_system.dto.PasswordDto dto) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 1. 校验旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码错误");
        }

        // 2. 更新新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        // 3. 如果是首次登录，更新状态
        if (Boolean.TRUE.equals(user.getIsFirstLogin())) {
            user.setIsFirstLogin(false);
        }

        userService.updateById(user);
        return Result.success("密码修改成功");
    }

    // 1. 管理员修改用户信息 (角色、姓名等)
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> update(@RequestBody com.school.research_system.dto.UserUpdateDto dto) {
        userService.updateUserInfo(dto);
        return Result.success("用户信息更新成功");
    }

    // 2. 管理员删除用户
    @PostMapping("/delete/{id}") // 或者用 @DeleteMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("用户已删除");
    }

    // 管理员重置用户密码
    @PostMapping("/reset-pwd/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> resetPwd(@PathVariable Long id) {
        // 1. 调用 Service，获取新生成的明文密码
        String newPwd = userService.resetPassword(id);

        // 2. 将密码放入 Result 的 data 字段返回给前端
        return Result.success(newPwd);
    }
}