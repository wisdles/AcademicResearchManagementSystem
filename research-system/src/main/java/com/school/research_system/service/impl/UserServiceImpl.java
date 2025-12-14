package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.dto.UserAddDto;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.UserMapper;
import com.school.research_system.service.IUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import java.util.Random;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    // 继承 ServiceImpl 后，你自动拥有了 save, remove, update, getOne, list 等方法
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void createUser(UserAddDto dto) {
        // 1. 检查工号是否已存在
        User exist = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (exist != null) {
            throw new RuntimeException("该工号已存在，请勿重复添加");
        }

        // 2. 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(dto, user);

        // 3. 核心：设置默认密码 abc123 并加密
        // 你的需求是：管理员创建时密码设为 abc123 (或随机)，这里按你要求设为 abc123
        String rawPassword = "abc123";
        user.setPassword(passwordEncoder.encode(rawPassword));

        // 4. 设置初始状态
        user.setIsFirstLogin(true); // 标记为首次登录（前端可强制要求改密）

        // 5. 保存
        this.save(user);
    }

    @Override
    public void updateUserInfo(com.school.research_system.dto.UserUpdateDto dto) {
        User user = this.getById(dto.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 更新非空字段
        if (dto.getRealName() != null)
            user.setRealName(dto.getRealName());
        if (dto.getRoleKey() != null)
            user.setRoleKey(dto.getRoleKey());
        if (dto.getCollegeId() != null)
            user.setCollegeId(dto.getCollegeId());

        this.updateById(user);
    }

    @Override
    public String resetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 1. 生成 8 位随机密码 (明文)
        String newPlainPassword = generateRandomPassword(8);

        // 2. 加密后存入数据库
        user.setPassword(passwordEncoder.encode(newPlainPassword));

        // 3. 设置为首次登录状态
        user.setIsFirstLogin(true);

        // 4. 更新数据库
        this.updateById(user);

        // 5. 🟢 关键点：将明文密码返回出去
        return newPlainPassword;
    }

    /**
     * 辅助工具：生成指定长度的随机字母数字组合
     */
    private String generateRandomPassword(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}