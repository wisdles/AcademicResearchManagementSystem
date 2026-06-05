package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.AwardDto;
import com.school.research_system.entity.Award;

/**
 * 获奖成果 Service 接口
 */
public interface IAwardService extends IService<Award> {

    void createAward(AwardDto dto);

    void updateAward(AwardDto dto);

    void auditAward(AuditDto dto);

}