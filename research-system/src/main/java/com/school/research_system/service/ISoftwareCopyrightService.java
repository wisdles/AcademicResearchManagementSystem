package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.SoftwareCopyrightDto;
import com.school.research_system.entity.SoftwareCopyright;

public interface ISoftwareCopyrightService extends IService<SoftwareCopyright> {
    void createSoft(SoftwareCopyrightDto dto);

    void updateSoft(SoftwareCopyrightDto dto);

    void auditSoft(AuditDto dto);
}