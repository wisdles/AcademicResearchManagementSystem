// package com.school.research_system.controller;

// import com.school.research_system.common.Result;
// import com.school.research_system.dto.AuditNewDto;
// import com.school.research_system.service.IAuditNewService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/auditNew")
// public class AuditNewController {

// @Autowired
// private IAuditNewService auditNewService;

// @PostMapping
// public Result<String> audit(@RequestBody AuditNewDto dto,
// @RequestHeader("userId") Long userId,
// @RequestHeader("role") String role,
// @RequestHeader("userName") String userName) {
// auditNewService.audit(dto, userId, userName, role);
// return Result.success("审核完成");
// }
// }
