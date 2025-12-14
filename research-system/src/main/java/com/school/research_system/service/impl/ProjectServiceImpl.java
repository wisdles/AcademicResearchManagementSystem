package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.dto.ProjectDto;
import com.school.research_system.entity.AuditLog;
import com.school.research_system.entity.Project;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.ProjectMapper;
import com.school.research_system.service.IProjectService;
import com.school.research_system.service.IUserService;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {

    @Autowired
    private IUserService userService;

    @Override
    @Transactional // 事务管理
    public void createProject(ProjectDto dto) {
        // 1. 获取当前登录用户的工号
        // String username = (String)
        // SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 🔴 删除旧代码: String username = (String) SecurityContextHolder...

        // 🟢 换成新代码: 使用 getName() 获取用户名，绝对安全
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 2. 查出用户详细信息 (需要获取 userId 和 collegeId)
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户异常，无法申报");
        }
        // 🔴 新增：严谨的校验逻辑
        if (user.getCollegeId() == null) {
            // 如果是管理员测试，可以临时给个默认值 1L，或者直接报错提示去配数据
            // 这里我们选择抛出清晰的错误，倒逼管理员去维护数据
            throw new RuntimeException("您的账号未绑定所属学院，请联系管理员完善信息后申报！");

            // 如果你想偷懒，用下面这行代替上面那行，自动归属到1号学院：
            // project.setCollegeId(1L);
        }

        // 3. 复制属性
        Project project = new Project();
        BeanUtils.copyProperties(dto, project); // 把 DTO 里的 name, funds 等拷过去

        // 4. 填充关键字段
        project.setUserId(user.getId());
        project.setCollegeId(user.getCollegeId()); // 项目归属老师所在的学院
        project.setUpdateTime(LocalDateTime.now()); // 更新时间
        // 5. 设置状态
        if (Boolean.TRUE.equals(dto.getIsSubmit())) {
            project.setCreateTime(LocalDateTime.now()); // 提交时设置申报时间
            project.setStatus(1); // 1 = 待秘书审核
        } else {
            project.setStatus(0); // 0 = 草稿
        }

        // 6. 保存到数据库
        this.save(project);
    }

    @Override
    public void updateProject(ProjectDto dto) {
        Project project = this.getById(dto.getId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        BeanUtils.copyProperties(dto, project);
        project.setUpdateTime(LocalDateTime.now());
        project.setStatus(dto.getIsSubmit() ? 1 : 0);
        if (dto.getIsSubmit()) {
            project.setCreateTime(LocalDateTime.now()); // 提交状态下设置申报时间
        }
        this.updateById(project);
    }

    // 1. 在类开头注入 Mapper
    @Autowired
    private com.school.research_system.mapper.AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public void auditProject(com.school.research_system.dto.AuditDto dto) {
        // 1. 获取当前操作人
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User operator = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 2. 获取项目
        Project project = this.getById(dto.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 3. 定义变量
        int newStatus = 0;
        String actionCode = "";

        // 🔴 核心修改：判断操作人角色
        if ("DEAN".equals(operator.getRoleKey())) {
            // ======= 院长逻辑 =======
            // 院长只能审状态为 2 (秘书已过) 的项目
            if (project.getStatus() != 2) {
                throw new RuntimeException("该项目未经过秘书审核，院长无法操作");
            }

            if (Boolean.TRUE.equals(dto.getIsPass())) {
                newStatus = 3; // 3 = 已通过 (归档)
                actionCode = "PASS_DEAN";
            } else {
                newStatus = -2; // -2 = 院长驳回
                actionCode = "REJECT_DEAN";
            }

        } else if (operator.getRoleKey().startsWith("SEC_")) {
            // ======= 秘书逻辑 (兼容 SEC_TEACHING 和 SEC_RESEARCH) =======
            // 秘书只能审状态为 1 (待审核) 的项目
            if (project.getStatus() != 1) {
                throw new RuntimeException("该项目状态不可审核");
            }

            if (Boolean.TRUE.equals(dto.getIsPass())) {
                newStatus = 2; // 2 = 待院长审
                actionCode = "PASS_SEC";
            } else {
                newStatus = -1; // -1 = 秘书驳回
                actionCode = "REJECT_SEC";
            }
        } else {
            throw new RuntimeException("您无权进行审核操作");
        }

        // 4. 执行更新
        project.setStatus(newStatus);
        this.updateById(project);

        // 5. 记录日志
        AuditLog log = new AuditLog();
        log.setTargetId(project.getId());
        log.setTargetType("PROJECT");
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getRealName());
        log.setAction(actionCode);
        log.setComment(dto.getComment());

        auditLogMapper.insert(log);
    }
}