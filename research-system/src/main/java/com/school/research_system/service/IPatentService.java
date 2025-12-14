package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.PatentDto;
import com.school.research_system.entity.Patent;

public interface IPatentService extends IService<Patent> {
    void createPatent(PatentDto dto);

    void updatePatent(PatentDto dto);

    void auditPatent(AuditDto dto);
}