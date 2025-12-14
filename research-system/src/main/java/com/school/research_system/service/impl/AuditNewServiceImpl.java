// package com.school.research_system.service.impl;

// import com.school.research_system.dto.AuditNewDto;
// import com.school.research_system.entity.AuditNewLog;
// import com.school.research_system.entity.Book;
// import com.school.research_system.entity.Paper;
// import com.school.research_system.entity.Project;
// // import com.school.research_system.entity.Patent;
// // import com.school.research_system.entity.Software;
// import com.school.research_system.mapper.*;
// import com.school.research_system.service.IAuditNewService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// @Service
// public class AuditNewServiceImpl implements IAuditNewService {

// @Autowired
// private AuditLogNewMapper auditNewLogMapper;
// @Autowired
// private BookMapper bookMapper;
// @Autowired
// private PaperMapper paperMapper;
// @Autowired
// private ProjectMapper projectMapper;
// // @Autowired
// // private PatentMapper patentMapper;
// // @Autowired
// // private SoftwareMapper softwareMapper;

// @Override
// public void audit(AuditNewDto dto, Long operatorId, String operatorName,
// String role) {
// AuditNewLog log = new AuditNewLog();
// log.setTargetId(dto.getTargetId());
// log.setTargetType(dto.getTargetType());
// log.setOperatorId(operatorId);
// log.setOperatorName(operatorName);
// log.setComment(dto.getComment());

// // 通用状态流转
// if ("PROJECT".equals(dto.getTargetType())) {
// Project project = projectMapper.selectById(dto.getTargetId());
// handleAudit(dto, project, role, log);
// projectMapper.updateById(project);
// } else if ("PAPER".equals(dto.getTargetType())) {
// Paper paper = paperMapper.selectById(dto.getTargetId());
// handleAudit(dto, paper, role, log);
// paperMapper.updateById(paper);
// }
// // } else if ("PATENT".equals(dto.getTargetType())) {
// // Patent patent = patentMapper.selectById(dto.getTargetId());
// // handleAudit(dto, patent, role, log);
// // patentMapper.updateById(patent);
// // } else if ("SOFT_COPY".equals(dto.getTargetType())) {
// // Software software = softwareMapper.selectById(dto.getTargetId());
// // handleAudit(dto, software, role, log);
// // softwareMapper.updateById(software);
// // } else if ("BOOK".equals(dto.getTargetType())) {
// // Book book = bookMapper.selectById(dto.getTargetId());
// // handleAudit(dto, book, role, log);
// // bookMapper.updateById(book);
// // }

// auditNewLogMapper.insert(log);
// }

// private void handleAudit(AuditNewDto dto, Object entity, String role,
// AuditNewLog log) {
// // entity 必须有 status 字段
// try {
// var statusField = entity.getClass().getDeclaredField("status");
// statusField.setAccessible(true);
// Integer status = (Integer) statusField.get(entity);

// if ("SECRETARY".equals(role)) {
// if (dto.getIsPass()) {
// statusField.set(entity, 2);
// log.setAction("PASS_SEC");
// } else {
// statusField.set(entity, -1);
// log.setAction("REJECT_SEC");
// }
// } else if ("DEAN".equals(role)) {
// if (dto.getIsPass()) {
// statusField.set(entity, 3);
// log.setAction("PASS_DEAN");
// } else {
// statusField.set(entity, -1);
// log.setAction("REJECT_DEAN");
// }
// }
// } catch (Exception e) {
// throw new RuntimeException("状态更新失败", e);
// }
// }
// }
