package com.school.research_system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.entity.User;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 这个类会在项目启动完成后立即执行
 * 专门用来修复密码
 */
@Component
public class FixPasswordRunner implements CommandLineRunner {

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=============================================");
        System.out.println("正在执行：管理员密码强制修复程序...");

        String username = "admin";
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user != null) {
            // 生成新的 abc123 密文
            String newPwd = passwordEncoder.encode("abc123");
            user.setPassword(newPwd);
            userService.updateById(user);
            System.out.println("✅ 成功！账号 [admin] 的密码已重置为 [abc123]");
            System.out.println("密文为: " + newPwd);
        } else {
            System.out.println("❌ 失败：未找到 admin 用户，请检查数据库");
        }
        System.out.println("=============================================");
    }
}