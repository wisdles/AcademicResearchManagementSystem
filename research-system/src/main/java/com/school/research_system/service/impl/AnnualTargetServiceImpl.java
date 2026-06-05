package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.entity.AnnualTarget;
import com.school.research_system.mapper.AnnualTargetMapper;
import com.school.research_system.service.IAnnualTargetService;
import org.springframework.stereotype.Service;

@Service
public class AnnualTargetServiceImpl extends ServiceImpl<AnnualTargetMapper, AnnualTarget> implements IAnnualTargetService {
}
