package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.entity.Message;
import com.school.research_system.mapper.MessageMapper;
import com.school.research_system.service.IMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息 Service 实现
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Override
    public Long getUnreadCount(Long userId) {
        return this.count(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0));
    }

    @Override
    public void markAsRead(Long messageId) {
        Message msg = new Message();
        msg.setId(messageId);
        msg.setIsRead(1);
        this.updateById(msg);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId).eq(Message::getIsRead, 0);
        Message update = new Message();
        update.setIsRead(1);
        this.update(update, wrapper);
    }

    @Override
    public void sendMessage(Message message) {
        this.save(message);
    }

}