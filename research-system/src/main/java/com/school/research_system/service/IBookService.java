package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.dto.BookDto;

import com.school.research_system.entity.Book;

public interface IBookService extends IService<Book> {

    void createBook(BookDto dto);

    // 添加这个方法定义
    void auditBook(com.school.research_system.dto.AuditDto dto);

    void updateBook(BookDto dto);

}
