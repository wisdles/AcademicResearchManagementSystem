package com.school.research_system.config;

import com.school.research_system.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. 获取请求头 Authorization
        String authHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();

        // 🟢 调试打印：只打印 notice 相关的请求，避免日志刷屏
        if (requestURI.contains("/notice/add")) {
            System.out.println("====== JWT调试 [" + requestURI + "] ======");
            System.out.println("收到 Header: " + authHeader);
        }

        // 2. 检查头是否合法 (Bearer xxx)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 去掉 "Bearer "
            String username = jwtUtils.getUsernameFromToken(token);
            if (requestURI.contains("/notice/add")) {
                System.out.println("解析出的工号: " + username);
            }

            // 3. 如果 token 有效且当前上下文没有认证信息
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 加载用户信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtils.validateToken(token)) {

                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    // 放入上下文（通过！）
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    if (requestURI.contains("/notice/add")) {
                        System.out.println("✅ 认证成功，已存入上下文");
                    }
                }
            }
        } else {
            if (requestURI.contains("/notice/add")) {
                System.out.println("❌ Header为空 或 格式不对 (必须以 'Bearer ' 开头)");
            }
        }

        // 4. 放行
        chain.doFilter(request, response);
    }
}