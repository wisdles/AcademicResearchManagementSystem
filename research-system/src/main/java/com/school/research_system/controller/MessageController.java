package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.entity.Message;
import com.school.research_system.entity.User;
import com.school.research_system.service.IMessageService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息中心控制器
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IUserService userService;

    // 获取我的消息列表
    @GetMapping("/my-list")
    public Result<List<Message>> myList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        List<Message> list = messageService.list(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, user.getId())
                        .orderByDesc(Message::getCreateTime));
        return Result.success(list);
    }

    // 获取未读消息数量
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Long count = messageService.getUnreadCount(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    // 标记单条已读
    @PutMapping("/read/{id}")
    public Result<String> markRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success("已标记为已读");
    }

    // 全部标记已读
    @PutMapping("/read-all")
    public Result<String> markAllRead() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        messageService.markAllRead(user.getId());
        return Result.success("全部已读");
    }

}