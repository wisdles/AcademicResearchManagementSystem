package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.entity.User;

public interface IUserService extends IService<User> {
    // 可以在这里定义复杂的业务接口
    // 在接口里加这一行
    void createUser(com.school.research_system.dto.UserAddDto dto);

    // 在接口中添加
    void updateUserInfo(com.school.research_system.dto.UserUpdateDto dto);

    // 🟢 修改这里：返回值由 void 改为 String
    String resetPassword(Long userId);
}