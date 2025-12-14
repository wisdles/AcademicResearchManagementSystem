package com.school.research_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // 如果没引入Security依赖就注释掉
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// 扫描 Mapper 文件夹路径
@MapperScan("com.school.research_system.mapper")
@SpringBootApplication
// @SpringBootApplication(exclude = {SecurityAutoConfiguration.class}) //
// 如果引入了Security但想暂时禁用登录验证，用这一行
public class ResearchSystemApplication {

	public static void main(String[] args) {
		System.out.println("Research System Start加密测试:");
		System.out.println(new BCryptPasswordEncoder().encode("abc123"));
		SpringApplication.run(ResearchSystemApplication.class, args);
	}

}