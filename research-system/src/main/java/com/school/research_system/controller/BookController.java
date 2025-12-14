package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.BookDto;
import com.school.research_system.entity.Book;
import com.school.research_system.entity.User;
import com.school.research_system.service.IBookService;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private IBookService bookService;

    @Autowired
    private IUserService userService;

    // 申报/修改 接口
    @PostMapping("/add")
    public Result<String> add(@RequestBody BookDto dto) {
        System.out.println("教材 ID: " + dto.getId());
        if (dto.getId() != null) {
            // 修改
            bookService.updateBook(dto);
            return Result.success("修改成功");
        } else {
            // 新增
            bookService.createBook(dto);
            return Result.success("申报成功");
        }
    }

    // 删除 (草稿删除)
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        bookService.removeById(id);
        return Result.success("删除成功");
    }

    // 查询我的教材列表
    @GetMapping("/my-list")
    public Result<List<Book>> myList() {
        // 1. 获取当前登录人
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 2. 查询该用户的所有教材
        List<Book> list = bookService.list(
                new LambdaQueryWrapper<Book>()
                        .eq(Book::getUserId, user.getId())
                        // Book 实体用的是 Date 类型的 createTime
                        .orderByDesc(Book::getCreateTime));

        return Result.success(list);
    }

    // 查询详情
    @GetMapping("/detail/{id}")
    public Result<Book> getDetail(@PathVariable Long id) {
        Book book = bookService.getById(id);
        if (book == null) {
            return Result.error("记录不存在");
        }
        return Result.success(book);
    }

    // 获取待审核列表 (根据角色自动判断)
    // @GetMapping("/audit-list")
    // public Result<List<Book>> getAuditList() {
    // String username =
    // SecurityContextHolder.getContext().getAuthentication().getName();
    // User user = userService.getOne(new
    // LambdaQueryWrapper<User>().eq(User::getUsername, username));

    // LambdaQueryWrapper<Book> query = new LambdaQueryWrapper<>();

    // if ("DEAN".equals(user.getRoleKey())) {
    // // 院长: 看状态 2 的
    // query.eq(Book::getStatus, 2);
    // } else if (user.getRoleKey() != null && user.getRoleKey().startsWith("SEC_"))
    // {
    // // 秘书 (不管是教学还是科研，因为书没有 classification，假设所有秘书都能审，或者你可以限定为 SEC_TEACHING)
    // // 这里逻辑设置为：只要是秘书，就看状态 1 的
    // query.eq(Book::getStatus, 1);
    // } else {
    // return Result.success(new ArrayList<>());
    // }

    // query.orderByDesc(Book::getCreateTime);
    // return Result.success(bookService.list(query));
    // }
    @GetMapping("/audit-list")
    public Result<List<Book>> getAuditList() {
        // 1. 获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (currentUser == null || currentUser.getCollegeId() == null) {
            return Result.success(new ArrayList<>());
        }

        Long myCollegeId = currentUser.getCollegeId();
        String role = currentUser.getRoleKey();

        // 🔴 核心修正 1：这里必须是 Book，不能是 Project
        LambdaQueryWrapper<Book> query = new LambdaQueryWrapper<>();

        // 🔴 核心修正 2：专利表没有 college_id，需要先获取本学院的所有老师 ID
        // 查出本学院所有用户的 ID 列表
        List<Long> collegeUserIds = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getCollegeId, myCollegeId))
                .stream().map(User::getId).collect(Collectors.toList());

        // 如果学院没人，直接返回空
        if (collegeUserIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 基础过滤：只能看本学院老师提交的专利
        query.in(Book::getUserId, collegeUserIds);

        // 3. 角色权限控制
        // if ("DEAN".equals(role)) {
        // // 院长：看状态 2
        // query.eq(Book::getStatus, 2);

        // } else if ("SEC_RESEARCH".equals(role)) {
        // // 科研秘书：看状态 1 (专利通常属于科研，所以只让科研秘书看)
        // query.eq(Book::getStatus, 1);

        // } else if ("SEC_TEACHING".equals(role)) {
        // // 教学秘书：通常不负责专利（专利属科研），直接返回空，或者根据你们学校规定修改
        // return Result.success(new ArrayList<>());

        // } else {
        // // 其他人没权限
        // return Result.success(new ArrayList<>());
        // }
        if ("SEC_RESEARCH".equals(role)) {
            // 科研秘书 -> 只看 待初审Book
            // query.eq(Book::getStatus, 1);
            query.eq(Book::getStatus, 1).eq(Book::getClassification, "科研");
        } else if ("SEC_TEACHING".equals(role)) {
            // 教学秘书 -> 只看 待初审
            // query.eq(Book::getStatus, 1);
            query.eq(Book::getStatus, 1).eq(Book::getClassification, "教学");
        } else if ("DEAN".equals(role)) {
            // 院长 -> 看所有 待终审 (不管是科研还是教学，只要秘书过了都归院长)
            query.eq(Book::getStatus, 2);
        } else {
            // 其他人没权限
            return Result.success(new ArrayList<>());
        }
        query.orderByDesc(Book::getCreateTime);

        // 4. 查询数据 (现在类型对了，不会报错了)
        List<Book> list = bookService.list(query);

        // 5. 填充申报人姓名
        if (!list.isEmpty()) {
            List<Long> userIds = list.stream().map(Book::getUserId).distinct().collect(Collectors.toList());
            List<User> users = userService.listByIds(userIds);
            Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getRealName));

            for (Book p : list) {
                // 记得在 Book 实体类里加 @TableField(exist=false) private String applicantName;
                p.setApplicantName(userMap.getOrDefault(p.getUserId(), "未知用户"));
            }
        }

        return Result.success(list);
    }

    // 审核接口
    @PostMapping("/audit")
    public Result<String> audit(@RequestBody AuditDto dto) {
        System.out.println("======> 进入了 BookController 的 audit 方法 <======");
        bookService.auditBook(dto);
        return Result.success("审核完成");
    }
}