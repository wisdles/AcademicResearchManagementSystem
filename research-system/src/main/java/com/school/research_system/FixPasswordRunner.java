package com.school.research_system;

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
        System.out.println("正在执行：全部账号密码强制修复程序...");

        String newPwd = passwordEncoder.encode("abc123");
        java.util.List<User> allUsers = userService.list();
        int count = 0;
        for (User u : allUsers) {
            u.setPassword(newPwd);
            u.setIsFirstLogin(false);
            userService.updateById(u);
            count++;
        }
        System.out.println("✅ 成功！已将 " + count + " 个账号的密码重置为 [abc123]");
        System.out.println("=============================================");
    }
}