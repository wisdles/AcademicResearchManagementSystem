package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.SoftwareCopyrightDto;
import com.school.research_system.entity.AuditLog;
import com.school.research_system.entity.Message;
import com.school.research_system.entity.SoftwareCopyright;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.AuditLogMapper;
import com.school.research_system.mapper.MessageMapper;
import com.school.research_system.mapper.SoftwareCopyrightMapper;
import com.school.research_system.service.ISoftwareCopyrightService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SoftwareCopyrightServiceImpl extends ServiceImpl<SoftwareCopyrightMapper, SoftwareCopyright>
        implements ISoftwareCopyrightService {

    @Autowired
    private IUserService userService;

    @Autowired
    private AuditLogMapper auditLogMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional
    public void createSoft(SoftwareCopyrightDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null)
            throw new RuntimeException("用户异常");
        if (user.getCollegeId() == null)
            throw new RuntimeException("未绑定学院信息");

        SoftwareCopyright soft = new SoftwareCopyright();
        BeanUtils.copyProperties(dto, soft);

        soft.setUserId(user.getId());
        soft.setUpdateTime(LocalDateTime.now());

        if (Boolean.TRUE.equals(dto.getIsSubmit())) {
            soft.setCreateTime(LocalDateTime.now());
            soft.setStatus(1);
        } else {
            soft.setStatus(0);
        }
        this.save(soft);
    }

    @Override
    @Transactional
    public void updateSoft(SoftwareCopyrightDto dto) {
        SoftwareCopyright soft = this.getById(dto.getId());
        if (soft == null)
            throw new RuntimeException("软著不存在");

        BeanUtils.copyProperties(dto, soft);
        soft.setUpdateTime(LocalDateTime.now());

        soft.setStatus(dto.getIsSubmit() ? 1 : 0);
        if (dto.getIsSubmit()) {
            soft.setCreateTime(LocalDateTime.now());
        }
        this.updateById(soft);
    }

    @Override
    @Transactional
    public void auditSoft(AuditDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User operator = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        SoftwareCopyright soft = this.getById(dto.getProjectId());
        if (soft == null)
            throw new RuntimeException("软著不存在");

        int newStatus = 0;
        String actionCode = "";

        if ("DEAN".equals(operator.getRoleKey())) {
            if (soft.getStatus() != 2)
                throw new RuntimeException("状态不符");
            if (Boolean.TRUE.equals(dto.getIsPass())) {
                newStatus = 3;
                actionCode = "PASS_DEAN";
            } else {
                newStatus = -2;
                actionCode = "REJECT_DEAN";
            }
        } else if (operator.getRoleKey() != null && operator.getRoleKey().startsWith("SEC_")) {
            if (soft.getStatus() != 1)
                throw new RuntimeException("状态不符");
            if (Boolean.TRUE.equals(dto.getIsPass())) {
                newStatus = 2;
                actionCode = "PASS_SEC";
            } else {
                newStatus = -1;
                actionCode = "REJECT_SEC";
            }
        } else {
            throw new RuntimeException("无权审核");
        }

        soft.setStatus(newStatus);
        this.updateById(soft);

        AuditLog log = new AuditLog();
        log.setTargetId(soft.getId());
        log.setTargetType("SOFT_COPYRIGHT"); // 类型标记
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getRealName());
        log.setAction(actionCode);
        log.setComment(dto.getComment());
        auditLogMapper.insert(log);

        // 发送审核结果消息给教师
        Message msg = new Message();
        msg.setReceiverId(soft.getUserId());
        msg.setTitle("软著审核结果通知");
        msg.setContent("您的软著《" + soft.getName() + "》已被" + (Boolean.TRUE.equals(dto.getIsPass()) ? "通过" : "驳回") + "。审核意见：" + (dto.getComment() != null ? dto.getComment() : "无"));
        msg.setType("AUDIT_RESULT");
        msg.setRelatedId(soft.getId());
        msg.setRelatedType("SOFTWARE");
        messageMapper.insert(msg);
    }
}