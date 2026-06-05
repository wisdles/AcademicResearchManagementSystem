package com.school.research_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.research_system.dto.AuditDto;
import com.school.research_system.dto.CompetitionDto;
import com.school.research_system.entity.Competition;

/**
 * 竞赛获奖 Service 接口
 */
public interface ICompetitionService extends IService<Competition> {

    void createCompetition(CompetitionDto dto);

    void updateCompetition(CompetitionDto dto);

    void auditCompetition(AuditDto dto);

}