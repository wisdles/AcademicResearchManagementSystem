-- 创建数据库
CREATE DATABASE IF NOT EXISTS `school_research_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE `school_research_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 学院表 (用于归属统计)
-- ----------------------------
DROP TABLE IF EXISTS `sys_college`;
CREATE TABLE `sys_college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '学院名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院信息表';

-- ----------------------------
-- 2. 用户表 (包含 教师、秘书、院长、管理员)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '工号/账号 (唯一)',
  `password` varchar(100) NOT NULL COMMENT '加密后的密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `college_id` bigint DEFAULT NULL COMMENT '所属学院ID',
  `role_key` varchar(20) NOT NULL COMMENT '角色标识: ADMIN(管理员), DEAN(院长), SEC_TEACHING(教学秘书), SEC_RESEARCH(科研秘书), TEACHER(教师)',
  `is_first_login` tinyint(1) DEFAULT 1 COMMENT '是否首次登录: 1-是(需重置密码), 0-否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`) USING BTREE COMMENT '工号唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 3. 全局审核日志表 (核心：记录所有打回、通过、提交操作)
-- ----------------------------
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_id` bigint NOT NULL COMMENT '关联的具体业务ID (项目ID, 论文ID等)',
  `target_type` varchar(50) NOT NULL COMMENT '业务类型: PROJECT, PAPER, PATENT, SOFT_COPY, COMPETITION',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名(冗余方便查询)',
  `action` varchar(50) NOT NULL COMMENT '动作: SUBMIT(提交), PASS_SEC(秘书通过), REJECT_SEC(秘书打回), PASS_DEAN(院长通过), REJECT_DEAN(院长打回)',
  `comment` varchar(500) DEFAULT NULL COMMENT '审核意见/驳回理由',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`, `target_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务审核日志表';

-- ----------------------------
-- 4. 通知公告表
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` longtext COMMENT '公告内容(富文本)',
  `type` varchar(20) DEFAULT 'NOTICE' COMMENT '类型: NOTICE-通知, GUIDE-申报指南',
  `category` varchar(20) DEFAULT 'ALL' COMMENT '分类: TEACHING-教学, RESEARCH-科研, ALL-全部',
  `publisher_id` bigint NOT NULL COMMENT '发布人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- ----------------------------
-- 5. 个人科研简历与职称表
-- ----------------------------
DROP TABLE IF EXISTS `biz_resume`;
CREATE TABLE `biz_resume` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `title_level` varchar(50) DEFAULT NULL COMMENT '职称: 教授, 副教授, 讲师, 助教',
  `education` varchar(50) DEFAULT NULL COMMENT '学历: 博士, 硕士, 本科',
  `research_direction` varchar(200) DEFAULT NULL COMMENT '研究方向',
  `bio` text COMMENT '个人简介',
  `resume_file_url` varchar(255) DEFAULT NULL COMMENT '简历附件地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '一个老师一份简历'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师档案表';

-- ----------------------------
-- 6. 项目申报表 (核心表：包含教学项目和科研项目)
-- ----------------------------
DROP TABLE IF EXISTS `biz_project`;
CREATE TABLE `biz_project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '申报人ID',
  `college_id` bigint NOT NULL COMMENT '归属学院ID',
  `name` varchar(200) NOT NULL COMMENT '项目名称',
  `category` varchar(20) NOT NULL COMMENT '大类: TEACHING(教学类), RESEARCH(科研类)',
  `sub_type` varchar(50) DEFAULT NULL COMMENT '子类型: 纵向/横向/校级/教改',
  `funds` decimal(10,2) DEFAULT '0.00' COMMENT '项目经费(万元)',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `app_file_url` varchar(255) DEFAULT NULL COMMENT '申报书附件路径',
  `contract_file_url` varchar(255) DEFAULT NULL COMMENT '合同附件路径',
  `status` int DEFAULT 0 COMMENT '状态: 0-暂存, 1-待秘书审核, 2-待院长审核, 3-已通过, -1-秘书驳回, -2-院长驳回',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目申报表';

-- ----------------------------
-- 7. 论文表 (包含OCR字段)
-- ----------------------------
DROP TABLE IF EXISTS `biz_paper`;
CREATE TABLE `biz_paper` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL COMMENT '论文标题',
  `journal_name` varchar(100) DEFAULT NULL COMMENT '期刊/会议名称',
  `publish_date` date DEFAULT NULL COMMENT '发表日期',
  `is_sci` tinyint(1) DEFAULT 0 COMMENT '是否SCI/EI收录',
  `file_url` varchar(255) DEFAULT NULL COMMENT '论文原件PDF',
  `ocr_content` longtext COMMENT 'OCR识别出的文本内容(用于自动调整/检索)',
  `status` int DEFAULT 0 COMMENT '审核状态: 同项目表',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论文成果表';

-- ----------------------------
-- 8. 专利表
-- ----------------------------
DROP TABLE IF EXISTS `biz_patent`;
CREATE TABLE `biz_patent` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `name` varchar(200) NOT NULL COMMENT '专利名称',
  `patent_no` varchar(100) DEFAULT NULL COMMENT '专利号',
  `type` varchar(50) DEFAULT NULL COMMENT '类型: 发明/实用新型/外观',
  `auth_date` date DEFAULT NULL COMMENT '授权日期',
  `file_url` varchar(255) DEFAULT NULL,
  `status` int DEFAULT 0 COMMENT '审核状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专利表';

-- ----------------------------
-- 9. 软著表
-- ----------------------------
DROP TABLE IF EXISTS `biz_software_copyright`;
CREATE TABLE `biz_software_copyright` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `name` varchar(200) NOT NULL COMMENT '软件名称',
  `register_no` varchar(100) DEFAULT NULL COMMENT '登记号',
  `develop_date` date DEFAULT NULL COMMENT '开发完成日期',
  `file_url` varchar(255) DEFAULT NULL,
  `status` int DEFAULT 0 COMMENT '审核状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='软著表';

-- ----------------------------
-- 10. 教学竞赛表 (教学类竞赛)
-- ----------------------------
DROP TABLE IF EXISTS `biz_competition`;
CREATE TABLE `biz_competition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `name` varchar(200) NOT NULL COMMENT '竞赛名称',
  `award_level` varchar(50) DEFAULT NULL COMMENT '获奖等级: 国家级/省级/校级',
  `award_grade` varchar(50) DEFAULT NULL COMMENT '奖项: 一等奖/二等奖',
  `award_date` date DEFAULT NULL,
  `cert_file_url` varchar(255) DEFAULT NULL COMMENT '证书附件',
  `status` int DEFAULT 0 COMMENT '审核状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学竞赛表';

-- ----------------------------
-- 11. 著作/专著表
-- ----------------------------
DROP TABLE IF EXISTS `biz_book`;
CREATE TABLE `biz_book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `name` varchar(200) NOT NULL COMMENT '书名',
  `publisher` varchar(100) DEFAULT NULL COMMENT '出版社',
  `isbn` varchar(50) DEFAULT NULL COMMENT 'ISBN号',
  `publish_date` date DEFAULT NULL,
  `status` int DEFAULT 0 COMMENT '审核状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='著作表';

SET FOREIGN_KEY_CHECKS = 1;
-- 插入学院示例
INSERT INTO `sys_college` (`id`, `name`) VALUES (1, '计算机学院');
INSERT INTO `sys_college` (`id`, `name`) VALUES (2, '机械工程学院');

-- 插入系统管理员 (密码 abc123 的 BCrypt 哈希值通常为 $2a$10$...)
-- 这里为了演示，先写明文，请务必在 Java Service 中做好加密逻辑
-- 假设 role_key 为 'ADMIN'
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role_key`, `is_first_login`, `is_deleted`) 
VALUES ('admin', '$2a$10$N.zmdrAM.lzJjOd91I.2.O/6s6jXq3.v.f.x.x.x', '系统管理员', 'ADMIN', 0, 0);

-- 插入一个测试老师 (工号 1001)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `college_id`, `role_key`, `is_first_login`) 
VALUES ('1001', '$2a$10$N.zmdrAM.lzJjOd91I.2.O/6s6jXq3.v.f.x.x.x', '张老师', 1, 'TEACHER', 1);