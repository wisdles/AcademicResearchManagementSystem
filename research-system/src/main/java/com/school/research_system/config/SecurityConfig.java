package com.school.research_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsService userDetailsService;
    // 新增：注入刚刚独立出来的 PasswordEncoder
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 关闭 CSRF (前后端分离必须关闭)
                .csrf(csrf -> csrf.disable())
                // 2. 允许跨域 (暂时默认)
                // 🔴 1. 开启 CORS，并指定配置源
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 3. 配置路径权限
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll() // 登录接口公开
                        .requestMatchers("/files/**").permitAll() // 🟢 允许所有人访问上传的文件
                        .anyRequest().authenticated() // 其他所有接口都需要登录
                )
                // 4. Session 设置为无状态 (因为是 JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 5. 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 认证管理器 (登录接口要用)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 认证提供者
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        // 修改：这里使用注入进来的 passwordEncoder 变量，而不是调用方法
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // 🔴 4. 定义具体的跨域配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的前端地址 (VS Code 默认是 5173，如果你用 WebStorm 可能是 8081，按需添加)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

        // 允许的请求方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的头信息
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 允许携带凭证 (Token)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有接口应用该配置
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}