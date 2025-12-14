package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.dto.BookDto;
import com.school.research_system.entity.AuditLog;
import com.school.research_system.entity.Book;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.AuditLogMapper;
import com.school.research_system.mapper.BookMapper;
import com.school.research_system.service.IBookService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements IBookService {

    @Autowired
    private IUserService userService;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public void createBook(BookDto dto) {
        // 1. 获取当前登录用户的工号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. 查出用户详细信息 (获取 userId 和 collegeId)
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户异常，无法申报");
        }
        if (user.getCollegeId() == null) {
            throw new RuntimeException("您的账号未绑定所属学院，请联系管理员完善信息后申报！");
        }

        // 3. 复制属性
        Book book = new Book();
        BeanUtils.copyProperties(dto, book);

        // 4. 填充关键字段
        book.setUserId(user.getId());
        // 注意：Book 实体如果没定义 collegeId 字段，这里就不用设，如果有则设
        // book.setCollegeId(user.getCollegeId());

        book.setUpdateTime(LocalDateTime.now());

        // 5. 设置状态
        if (Boolean.TRUE.equals(dto.getIsSubmit())) {
            book.setCreateTime(LocalDateTime.now()); // 提交时设置时间
            book.setStatus(1); // 1 = 待秘书审核
        } else {
            book.setStatus(0); // 0 = 草稿
        }

        // 6. 保存
        this.save(book);
    }

    @Override
    @Transactional
    public void updateBook(BookDto dto) {
        Book book = this.getById(dto.getId());
        if (book == null) {
            throw new RuntimeException("教材/专著信息不存在");
        }
        BeanUtils.copyProperties(dto, book);
        book.setUpdateTime(LocalDateTime.now());

        // 如果是草稿转提交，或者是重新提交
        book.setStatus(dto.getIsSubmit() ? 1 : 0);
        if (dto.getIsSubmit()) {
            book.setCreateTime(LocalDateTime.now());
        }
        this.updateById(book);
    }

    @Override
    @Transactional
    public void auditBook(com.school.research_system.dto.AuditDto dto) {
        // 1. 获取当前操作人
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User operator = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 2. 获取教材信息

        Book book = this.getById(dto.getProjectId());
        if (book == null) {
            throw new RuntimeException("教材信息不存在");
        }

        // 3. 定义变量
        int newStatus = 0;
        String actionCode = "";

        // 4. 角色判断逻辑
        if ("DEAN".equals(operator.getRoleKey())) {
            // ======= 院长逻辑 =======
            // 院长只能审状态为 2 (秘书已过)
            if (book.getStatus() != 2) {
                throw new RuntimeException("该记录未经过秘书审核，院长无法操作");
            }

            if (Boolean.TRUE.equals(dto.getIsPass())) {
                newStatus = 3; // 3 = 已通过
                actionCode = "PASS_DEAN";
            } else {
                newStatus = -2; // -2 = 院长驳回
                actionCode = "REJECT_DEAN";
            }

        } else if (operator.getRoleKey() != null && operator.getRoleKey().startsWith("SEC_")) {
            // ======= 秘书逻辑 =======
            // 秘书只能审状态为 1 (待审核)
            if (book.getStatus() != 1) {
                throw new RuntimeException("该记录状态不可审核");
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

        // 5. 更新状态
        book.setStatus(newStatus);
        this.updateById(book);

        // 6. 记录日志
        AuditLog log = new AuditLog();
        log.setTargetId(book.getId());
        log.setTargetType("BOOK"); // 标记类型为教材
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getRealName());
        log.setAction(actionCode);
        log.setComment(dto.getComment());

        auditLogMapper.insert(log);
    }
}