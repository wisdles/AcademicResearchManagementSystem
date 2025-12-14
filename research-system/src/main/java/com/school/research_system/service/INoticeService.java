package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.entity.Notice;

public interface INoticeService extends IService<Notice> {
    /**
     * 从附件中提取文本 (Word/PDF)
     * 
     * @param noticeId 公告ID
     * @return 解析后的文本内容
     */
    String extractTextFromAttachment(Long noticeId);
}