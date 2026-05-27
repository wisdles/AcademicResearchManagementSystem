package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.CompetitionDto;
import com.school.research_system.entity.AuditLog;
import com.school.research_system.entity.Competition;
import com.school.research_system.entity.Message;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.AuditLogMapper;
import com.school.research_system.mapper.CompetitionMapper;
import com.school.research_system.mapper.MessageMapper;
import com.school.research_system.service.ICompetitionService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 竞赛获奖 Service 实现
 */
@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition> implements ICompetitionService {

    @Autowired
    private IUserService userService;

    @Autowired
    private AuditLogMapper auditLogMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional
    public void createCompetition(CompetitionDto dto) {
        // 获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户异常，无法申报");
        }
        if (user.getCollegeId() == null) {
            throw new RuntimeException("您的账号未绑定所属学院，请联系管理员完善信息后申报！");
        }

        // 复制DTO属性到实体
        Competition competition = new Competition();
        BeanUtils.copyProperties(dto, competition);

        // 设置用户ID和状态
        competition.setUserId(user.getId());
        competition.setUpdateTime(LocalDateTime.now());

        if (Boolean.TRUE.equals(dto.getIsSubmit())) {
            competition.setCreateTime(LocalDateTime.now());
            competition.setStatus(1); // 待秘书审核
        } else {
            competition.setStatus(0); // 草稿
        }

        this.save(competition);
    }

    @Override
    @Transactional
    public void updateCompetition(CompetitionDto dto) {
        Competition competition = this.getById(dto.getId());
        if (competition == null) {
            throw new RuntimeException("竞赛记录不存在");
        }

        BeanUtils.copyProperties(dto, competition);
        competition.setUpdateTime(LocalDateTime.now());

        competition.setStatus(dto.getIsSubmit() ? 1 : 0);
        if (dto.getIsSubmit()) {
            competition.setCreateTime(LocalDateTime.now());
        }

        this.updateById(competition);
    }

    @Override
    @Transactional
    public void auditCompetition(AuditDto dto) {
        // 获取当前操作人
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User operator = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 获取待审核的竞赛记录
        Competition competition = this.getById(dto.getProjectId());
        if (competition == null) {
            throw new RuntimeException("竞赛记录不存在");
        }

        int newStatus = 0;
        String actionCode = "";

        // 角色判断
        if ("DEAN".equals(operator.getRoleKey())) {
            if (competition.getStatus() != 2) {
                throw new RuntimeException("该记录未经过秘书审核，院长无法操作");
            }
            if (Boolean.TRUE.equals(dto.getIsPass())) {
                newStatus = 3; // 已通过
                actionCode = "PASS_DEAN";
            } else {
                newStatus = -2; // 院长驳回
                actionCode = "REJECT_DEAN";
            }
        } else if (operator.getRoleKey() != null && operator.getRoleKey().startsWith("SEC_")) {
            if (competition.getStatus() != 1) {
                throw new RuntimeException("该记录状态不可审核");
            }
            if (Boolean.TRUE.equals(dto.getIsPass())) {
                newStatus = 2; // 待院长审核
                actionCode = "PASS_SEC";
            } else {
                newStatus = -1; // 秘书驳回
                actionCode = "REJECT_SEC";
            }
        } else {
            throw new RuntimeException("您无权进行审核操作");
        }

        // 更新状态
        competition.setStatus(newStatus);
        this.updateById(competition);

        // 记录审核日志
        AuditLog log = new AuditLog();
        log.setTargetId(competition.getId());
        log.setTargetType("COMPETITION");
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getRealName());
        log.setAction(actionCode);
        log.setComment(dto.getComment());
        auditLogMapper.insert(log);

        // 发送审核结果消息给教师
        Message msg = new Message();
        msg.setReceiverId(competition.getUserId());
        msg.setTitle("竞赛审核结果通知");
        msg.setContent("您的竞赛《" + competition.getName() + "》已被" + (Boolean.TRUE.equals(dto.getIsPass()) ? "通过" : "驳回") + "。审核意见：" + (dto.getComment() != null ? dto.getComment() : "无"));
        msg.setType("AUDIT_RESULT");
        msg.setRelatedId(competition.getId());
        msg.setRelatedType("COMPETITION");
        messageMapper.insert(msg);
    }

}