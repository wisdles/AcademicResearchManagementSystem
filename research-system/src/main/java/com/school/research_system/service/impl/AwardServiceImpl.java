package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.AwardDto;
import com.school.research_system.entity.AuditLog;
import com.school.research_system.entity.Award;
import com.school.research_system.entity.Message;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.AuditLogMapper;
import com.school.research_system.mapper.AwardMapper;
import com.school.research_system.mapper.MessageMapper;
import com.school.research_system.service.IAwardService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 获奖成果 Service 实现
 */
@Service
public class AwardServiceImpl extends ServiceImpl<AwardMapper, Award> implements IAwardService {

    @Autowired
    private IUserService userService;

    @Autowired
    private AuditLogMapper auditLogMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional
    public void createAward(AwardDto dto) {
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
        Award award = new Award();
        BeanUtils.copyProperties(dto, award);

        // 设置用户ID和状态
        award.setUserId(user.getId());
        award.setUpdateTime(LocalDateTime.now());

        if (Boolean.TRUE.equals(dto.getIsSubmit())) {
            award.setCreateTime(LocalDateTime.now());
            award.setStatus(1); // 待秘书审核
        } else {
            award.setStatus(0); // 草稿
        }

        this.save(award);
    }

    @Override
    @Transactional
    public void updateAward(AwardDto dto) {
        Award award = this.getById(dto.getId());
        if (award == null) {
            throw new RuntimeException("获奖记录不存在");
        }

        BeanUtils.copyProperties(dto, award);
        award.setUpdateTime(LocalDateTime.now());

        award.setStatus(dto.getIsSubmit() ? 1 : 0);
        if (dto.getIsSubmit()) {
            award.setCreateTime(LocalDateTime.now());
        }

        this.updateById(award);
    }

    @Override
    @Transactional
    public void auditAward(AuditDto dto) {
        // 获取当前操作人
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User operator = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 获取待审核的获奖记录
        Award award = this.getById(dto.getProjectId());
        if (award == null) {
            throw new RuntimeException("获奖记录不存在");
        }

        int newStatus = 0;
        String actionCode = "";

        // 角色判断
        if ("DEAN".equals(operator.getRoleKey())) {
            // 院长只能审核状态为2的（秘书已通过）
            if (award.getStatus() != 2) {
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
            // 秘书只能审核状态为1的
            if (award.getStatus() != 1) {
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
        award.setStatus(newStatus);
        this.updateById(award);

        // 记录审核日志
        AuditLog log = new AuditLog();
        log.setTargetId(award.getId());
        log.setTargetType("AWARD");
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getRealName());
        log.setAction(actionCode);
        log.setComment(dto.getComment());
        auditLogMapper.insert(log);

        // 发送审核结果消息给教师
        Message msg = new Message();
        msg.setReceiverId(award.getUserId());
        msg.setTitle("获奖审核结果通知");
        msg.setContent("您的获奖《" + award.getAwardName() + "》已被" + (Boolean.TRUE.equals(dto.getIsPass()) ? "通过" : "驳回") + "。审核意见：" + (dto.getComment() != null ? dto.getComment() : "无"));
        msg.setType("AUDIT_RESULT");
        msg.setRelatedId(award.getId());
        msg.setRelatedType("AWARD");
        messageMapper.insert(msg);
    }

}