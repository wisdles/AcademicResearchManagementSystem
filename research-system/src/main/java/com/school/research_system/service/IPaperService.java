package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.PaperDto;
import com.school.research_system.entity.Paper;

public interface IPaperService extends IService<Paper> {
    void createPaper(PaperDto dto);

    void updatePaper(PaperDto dto);

    void auditPaper(AuditDto dto);
}