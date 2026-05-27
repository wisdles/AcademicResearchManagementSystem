package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.PatentDto;
import com.school.research_system.entity.AuditLog;
import com.school.research_system.entity.Message;
import com.school.research_system.entity.Patent;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.AuditLogMapper;
import com.school.research_system.mapper.MessageMapper;
import com.school.research_system.mapper.PatentMapper;
import com.school.research_system.service.IPatentService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PatentServiceImpl extends ServiceImpl<PatentMapper, Patent> implements IPatentService {

    @Autowired
    private IUserService userService;

    @Autowired
    private AuditLogMapper auditLogMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional
    public void createPatent(PatentDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null)
            throw new RuntimeException("用户异常");
        if (user.getCollegeId() == null)
            throw new RuntimeException("未绑定学院信息");

        Patent patent = new Patent();
        BeanUtils.copyProperties(dto, patent);

        patent.setUserId(user.getId());
        patent.setUpdateTime(LocalDateTime.now());

        if (Boolean.TRUE.equals(dto.getIsSubmit())) {
            patent.setCreateTime(LocalDateTime.now());
            patent.setStatus(1); // 待审核
        } else {
            patent.setStatus(0); // 草稿
        }

        this.save(patent);
    }

    @Override
    @Transactional
    public void updatePatent(PatentDto dto) {
        Patent patent = this.getById(dto.getId());
        if (patent == null)
            throw new RuntimeException("专利不存在");

        BeanUtils.copyProperties(dto, patent);
        patent.setUpdateTime(LocalDateTime.now());

        patent.setStatus(dto.getIsSubmit() ? 1 : 0);
        if (dto.getIsSubmit()) {
            patent.setCreateTime(LocalDateTime.now());
        }

        this.updateById(patent);
    }

    @Override
    @Transactional
    public void auditPatent(AuditDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User operator = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Patent patent = this.getById(dto.getProjectId());
        if (patent == null)
            throw new RuntimeException("专利不存在");

        int newStatus = 0;
        String actionCode = "";

        if ("DEAN".equals(operator.getRoleKey())) {
            if (patent.getStatus() != 2)
                throw new RuntimeException("状态不符，院长无法审核");
            if (Boolean.TRUE.equals(dto.getIsPass())) {
                newStatus = 3;
                actionCode = "PASS_DEAN";
            } else {
                newStatus = -2;
                actionCode = "REJECT_DEAN";
            }
        } else if (operator.getRoleKey() != null && operator.getRoleKey().startsWith("SEC_")) {
            if (patent.getStatus() != 1)
                throw new RuntimeException("状态不符，秘书无法审核");
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

        patent.setStatus(newStatus);
        this.updateById(patent);

        AuditLog log = new AuditLog();
        log.setTargetId(patent.getId());
        log.setTargetType("PATENT");
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getRealName());
        log.setAction(actionCode);
        log.setComment(dto.getComment());
        auditLogMapper.insert(log);

        // 发送审核结果消息给教师
        Message msg = new Message();
        msg.setReceiverId(patent.getUserId());
        msg.setTitle("专利审核结果通知");
        msg.setContent("您的专利《" + patent.getName() + "》已被" + (Boolean.TRUE.equals(dto.getIsPass()) ? "通过" : "驳回") + "。审核意见：" + (dto.getComment() != null ? dto.getComment() : "无"));
        msg.setType("AUDIT_RESULT");
        msg.setRelatedId(patent.getId());
        msg.setRelatedType("PATENT");
        messageMapper.insert(msg);
    }
}