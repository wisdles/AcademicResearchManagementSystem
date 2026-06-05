package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.entity.Message;

/**
 * 消息 Service 接口
 */
public interface IMessageService extends IService<Message> {

    Long getUnreadCount(Long userId);

    void markAsRead(Long messageId);

    void markAllRead(Long userId);

    void sendMessage(Message message);

}