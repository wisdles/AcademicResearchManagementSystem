package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.school.research_system.dto.PaperDto;
import com.school.research_system.entity.AuditLog;
import com.school.research_system.entity.Paper;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.AuditLogMapper;
import com.school.research_system.mapper.PaperMapper;
import com.school.research_system.service.IPaperService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements IPaperService {

    @Autowired
    private IUserService userService;

    // 🔴 记得注入日志 Mapper
    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public void createPaper(PaperDto dto) {
        // 1. 获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null) {
            throw new RuntimeException("用户异常");
        }
        // 校验学院信息（可选）
        if (user.getCollegeId() == null) {
            throw new RuntimeException("您的账号未绑定所属学院，请联系管理员完善信息后申报！");
        }

        // 2. 复制属性
        Paper paper = new Paper();
        BeanUtils.copyProperties(dto, paper);

        // 3. 设置关键信息
        paper.setUserId(user.getId());
        paper.setUpdateTime(LocalDateTime.now());

        // 4. 设置状态
        if (Boolean.TRUE.equals(dto.getIsSubmit())) {
            paper.setCreateTime(LocalDateTime.now());
            paper.setStatus(1); // 1 = 待秘书审核
        } else {
            paper.setStatus(0); // 0 = 草稿
        }

        // 5. 保存
        this.save(paper);
    }

    @Override
    @Transactional
    public void updatePaper(PaperDto dto) {
        Paper paper = this.getById(dto.getId());
        if (paper == null) {
            throw new RuntimeException("论文记录不存在");
        }

        BeanUtils.copyProperties(dto, paper);
        paper.setUpdateTime(LocalDateTime.now());

        // 更新状态：如果是提交操作，设为1，否则保持0
        paper.setStatus(dto.getIsSubmit() ? 1 : 0);

        if (dto.getIsSubmit()) {
            paper.setCreateTime(LocalDateTime.now());
        }

        this.updateById(paper);
    }

    @Override
    @Transactional
    public void auditPaper(com.school.research_system.dto.AuditDto dto) {
        // 1. 获取操作人
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User operator = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 2. 获取论文
        Paper paper = this.getById(dto.getProjectId());
        if (paper == null) {
            throw new RuntimeException("论文记录不存在");
        }

        int newStatus = 0;
        String actionCode = "";

        // 3. 角色判断
        if ("DEAN".equals(operator.getRoleKey())) {
            // ======= 院长 =======
            if (paper.getStatus() != 2) {
                throw new RuntimeException("该论文未经过秘书审核，院长无法操作");
            }
            if (Boolean.TRUE.equals(dto.getIsPass())) {
                newStatus = 3; // 3 = 已通过
                actionCode = "PASS_DEAN";
            } else {
                newStatus = -2; // -2 = 院长驳回
                actionCode = "REJECT_DEAN";
            }
        } else if (operator.getRoleKey() != null && operator.getRoleKey().startsWith("SEC_")) {
            // ======= 秘书 (这里假设所有秘书都能审论文，或者你可以限定为 SEC_RESEARCH) =======
            if (paper.getStatus() != 1) {
                throw new RuntimeException("该状态无法审核");
            }
            if (Boolean.TRUE.equals(dto.getIsPass())) {
                newStatus = 2; // 2 = 待院长审
                actionCode = "PASS_SEC";
            } else {
                newStatus = -1; // -1 = 秘书驳回
                actionCode = "REJECT_SEC";
            }
        } else {
            throw new RuntimeException("您无权审核");
        }

        // 4. 更新
        paper.setStatus(newStatus);
        this.updateById(paper);

        // 5. 写日志
        AuditLog log = new AuditLog();
        log.setTargetId(paper.getId());
        log.setTargetType("PAPER"); // 类型
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getRealName());
        log.setAction(actionCode);
        log.setComment(dto.getComment());

        auditLogMapper.insert(log);
    }
}