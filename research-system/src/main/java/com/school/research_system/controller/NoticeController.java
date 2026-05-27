package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.entity.Message;
import com.school.research_system.entity.Notice;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.MessageMapper;
import com.school.research_system.service.INoticeService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private INoticeService noticeService;
    @Autowired
    private IUserService userService;
    @Autowired
    private MessageMapper messageMapper;

    // 1. 发布通知 (仅秘书)
    @PostMapping("/add")
    public Result<String> add(@RequestBody Notice notice) {
        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 校验权限 (双重保险)
        if (!user.getRoleKey().startsWith("SEC_")) {
            return Result.error("只有秘书可以发布通知");
        }

        notice.setPublisherId(user.getId());
        notice.setPublisherName(user.getRealName());
        // 核心逻辑：如果是管理员，targetCollegeId = null (全校可见)
        // 如果是秘书/院长，targetCollegeId = user.getCollegeId() (本院可见)
        if ("ADMIN".equals(user.getRoleKey())) {
            notice.setTargetCollegeId(null);
        } else {
            notice.setTargetCollegeId(user.getCollegeId());
        }
        noticeService.save(notice);
        return Result.success("发布成功");
    }

    // // 2. 获取列表 (所有人)
    // @GetMapping("/list")
    // public Result<List<Notice>> list() {
    // // 按时间倒序
    // List<Notice> list = noticeService.list(new LambdaQueryWrapper<Notice>()
    // .orderByDesc(Notice::getCreateTime));

    // return Result.success(list);
    // }
    // 2. 查看通知列表 (教师端/首页)
    @GetMapping("/list")
    public Result<List<Notice>> list() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        LambdaQueryWrapper<Notice> query = new LambdaQueryWrapper<>();

        // 核心逻辑：查询 (目标学院 IS NULL) 或者 (目标学院 == 我的学院)
        // SQL: WHERE (target_college_id IS NULL) OR (target_college_id = 1)
        query.and(wrapper -> wrapper.isNull(Notice::getTargetCollegeId)
                .or()
                .eq(user.getCollegeId() != null, Notice::getTargetCollegeId, user.getCollegeId()));

        query.orderByDesc(Notice::getCreateTime);
        return Result.success(noticeService.list(query));
    }

    // 3. 预览附件文本
    @GetMapping("/preview/{id}")
    public Result<String> preview(@PathVariable Long id) {
        // 直接调用接口方法，不需要强转了
        String text = noticeService.extractTextFromAttachment(id);
        return Result.success(text);
    }

    // 4. 删除通知
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.error("公告不存在");
        }

        // 权限校验：只有发布人自己 或 管理员 可以删除
        if (!notice.getPublisherId().equals(user.getId()) && !"ADMIN".equals(user.getRoleKey())) {
            return Result.error("您无权删除此条公告");
        }

        noticeService.removeById(id);
        return Result.success("删除成功");
    }

    // 5. 修改通知
    @PostMapping("/update")
    public Result<String> update(@RequestBody Notice notice) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Notice oldNotice = noticeService.getById(notice.getId());
        if (oldNotice == null) {
            return Result.error("公告不存在");
        }

        // 权限校验
        if (!oldNotice.getPublisherId().equals(user.getId()) && !"ADMIN".equals(user.getRoleKey())) {
            return Result.error("您无权修改此条公告");
        }

        // 更新字段
        oldNotice.setTitle(notice.getTitle());
        oldNotice.setContent(notice.getContent());
        oldNotice.setClassification(notice.getClassification());
        // 如果重新上传了附件
        if (notice.getAttachmentUrl() != null) {
            oldNotice.setAttachmentUrl(notice.getAttachmentUrl());
            oldNotice.setAttachmentName(notice.getAttachmentName());
        }

        noticeService.updateById(oldNotice);
        return Result.success("修改成功");
    }

    // 6. 催报功能：秘书向本院教师发送催报消息
    @PostMapping("/urge")
    public Result<String> urge(@RequestBody Map<String, String> params) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User operator = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (operator.getRoleKey() == null || !operator.getRoleKey().startsWith("SEC_")) {
            return Result.error("只有秘书可以催报");
        }

        String achievementType = params.get("achievementType"); // 成果类型，如project, paper等
        String typeLabel = params.get("typeLabel"); // 中文名称，如"项目"

        // 查询本院所有教师
        List<User> teachers = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getCollegeId, operator.getCollegeId())
                .eq(User::getRoleKey, "TEACHER"));

        int count = 0;
        for (User teacher : teachers) {
            Message msg = new Message();
            msg.setSenderId(operator.getId());
            msg.setReceiverId(teacher.getId());
            msg.setTitle("催报通知");
            msg.setContent("请您尽快提交" + typeLabel + "成果，谢谢配合！");
            msg.setType("URGE");
            messageMapper.insert(msg);
            count++;
        }

        return Result.success("已成功向" + count + "位教师发送催报通知");
    }

}