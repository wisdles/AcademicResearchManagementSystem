package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.dto.LoginDto;
import com.school.research_system.entity.User;
import com.school.research_system.service.IUserService;
import com.school.research_system.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDto loginDto) {
        try {
            // 1. 尝试认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 2. 生成 Token
            String token = jwtUtils.generateToken(loginDto.getUsername());

            // 3. 获取用户信息
            User user = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, loginDto.getUsername()));

            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("role", user.getRoleKey());
            map.put("realName", user.getRealName());
            map.put("isFirstLogin", user.getIsFirstLogin());
            map.put("userId", user.getId());
            return Result.success(map);

        } catch (Exception e) {
            // 🔴 关键修改：打印报错堆栈到控制台，并返回错误信息给前端
            e.printStackTrace();
            return Result.error("登录失败: " + e.getMessage());
        }
    }

}