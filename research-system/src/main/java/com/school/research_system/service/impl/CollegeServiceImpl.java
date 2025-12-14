package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.entity.College;
import com.school.research_system.mapper.CollegeMapper;
import com.school.research_system.service.ICollegeService;
import org.springframework.stereotype.Service;

@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements ICollegeService {
}