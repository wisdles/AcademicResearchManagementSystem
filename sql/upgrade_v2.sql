-- 高校科研信息管理系统 v2 升级脚本
-- 新增: 获奖成果、竞赛（增强）、课程建设、消息中心、标签字段

-- 1. 获奖成果表
CREATE TABLE IF NOT EXISTS `biz_award` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '申报人ID',
  `award_name` varchar(200) NOT NULL COMMENT '获奖名称',
  `award_level` varchar(50) DEFAULT NULL COMMENT '获奖级别: 国家级/省部级/市厅级/校级',
  `award_grade` varchar(50) DEFAULT NULL COMMENT '获奖等级: 一等奖/二等奖/三等奖',
  `award_unit` varchar(200) DEFAULT NULL COMMENT '颁奖单位',
  `award_date` date DEFAULT NULL COMMENT '获奖日期',
  `ranking` varchar(50) DEFAULT NULL COMMENT '本人排名',
  `classification` varchar(50) NOT NULL DEFAULT '科研' COMMENT '分类: 科研/教学',
  `proof_file` varchar(255) DEFAULT NULL COMMENT '证明文件',
  `remark` text COMMENT '备注',
  `status` int DEFAULT 0 COMMENT '审核状态: 0草稿 1待秘书 2待院长 3通过 -1秘书驳回 -2院长驳回',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='获奖成果表';

-- 2. 竞赛表（已有表，添加缺失字段）
-- 检查并添加字段（使用存储过程避免重复添加报错）
-- classification 字段
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_competition ADD COLUMN classification varchar(50) NOT NULL DEFAULT ''科研'' COMMENT ''分类: 科研/教学'';', 'SELECT ''classification already exists'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_competition' AND COLUMN_NAME = 'classification');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- proof_file 字段
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_competition ADD COLUMN proof_file varchar(255) DEFAULT NULL COMMENT ''证明文件'';', 'SELECT ''proof_file already exists'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_competition' AND COLUMN_NAME = 'proof_file');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- remark 字段
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_competition ADD COLUMN remark text COMMENT ''备注'';', 'SELECT ''remark already exists'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_competition' AND COLUMN_NAME = 'remark');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- student_name 字段
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_competition ADD COLUMN student_name varchar(100) DEFAULT NULL COMMENT ''学生姓名'';', 'SELECT ''student_name already exists'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_competition' AND COLUMN_NAME = 'student_name');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ranking 字段
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_competition ADD COLUMN ranking varchar(50) DEFAULT NULL COMMENT ''本人排名'';', 'SELECT ''ranking already exists'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_competition' AND COLUMN_NAME = 'ranking');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- competition_level 字段
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_competition ADD COLUMN competition_level varchar(50) DEFAULT NULL COMMENT ''竞赛级别: 国家级/省部级/校级'';', 'SELECT ''competition_level already exists'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_competition' AND COLUMN_NAME = 'competition_level');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. 课程建设表
CREATE TABLE IF NOT EXISTS `biz_course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '申报人ID',
  `course_name` varchar(200) NOT NULL COMMENT '课程名称',
  `course_type` varchar(50) DEFAULT NULL COMMENT '课程类型: 精品课程/一流课程/思政示范课等',
  `course_level` varchar(50) DEFAULT NULL COMMENT '课程级别: 国家级/省级/校级',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `description` text COMMENT '课程描述',
  `classification` varchar(50) NOT NULL DEFAULT '教学' COMMENT '分类: 科研/教学',
  `proof_file` varchar(255) DEFAULT NULL COMMENT '证明文件',
  `remark` text COMMENT '备注',
  `status` int DEFAULT 0 COMMENT '审核状态: 0草稿 1待秘书 2待院长 3通过 -1秘书驳回 -2院长驳回',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程建设表';

-- 4. 消息通知表
CREATE TABLE IF NOT EXISTS `sys_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint DEFAULT NULL COMMENT '发送者ID，NULL表示系统消息',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `title` varchar(200) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `type` varchar(50) DEFAULT 'SYSTEM' COMMENT '消息类型: AUDIT_RESULT/URGE/NOTICE/SYSTEM',
  `related_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `related_type` varchar(50) DEFAULT NULL COMMENT '关联业务类型',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `is_deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 5. 标签字段（8个成果表）
-- biz_project
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_project ADD COLUMN tags varchar(500) DEFAULT NULL COMMENT ''标签(JSON数组)'';', 'SELECT ''tags already exists in biz_project'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_project' AND COLUMN_NAME = 'tags');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_paper
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_paper ADD COLUMN tags varchar(500) DEFAULT NULL COMMENT ''标签(JSON数组)'';', 'SELECT ''tags already exists in biz_paper'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_paper' AND COLUMN_NAME = 'tags');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_patent
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_patent ADD COLUMN tags varchar(500) DEFAULT NULL COMMENT ''标签(JSON数组)'';', 'SELECT ''tags already exists in biz_patent'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_patent' AND COLUMN_NAME = 'tags');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_software_copyright
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_software_copyright ADD COLUMN tags varchar(500) DEFAULT NULL COMMENT ''标签(JSON数组)'';', 'SELECT ''tags already exists in biz_software_copyright'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_software_copyright' AND COLUMN_NAME = 'tags');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_book
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_book ADD COLUMN tags varchar(500) DEFAULT NULL COMMENT ''标签(JSON数组)'';', 'SELECT ''tags already exists in biz_book'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_book' AND COLUMN_NAME = 'tags');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_award
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_award ADD COLUMN tags varchar(500) DEFAULT NULL COMMENT ''标签(JSON数组)'';', 'SELECT ''tags already exists in biz_award'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_award' AND COLUMN_NAME = 'tags');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_competition
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_competition ADD COLUMN tags varchar(500) DEFAULT NULL COMMENT ''标签(JSON数组)'';', 'SELECT ''tags already exists in biz_competition'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_competition' AND COLUMN_NAME = 'tags');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_course
SET @s = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE biz_course ADD COLUMN tags varchar(500) DEFAULT NULL COMMENT ''标签(JSON数组)'';', 'SELECT ''tags already exists in biz_course'';') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'school_research_db' AND TABLE_NAME = 'biz_course' AND COLUMN_NAME = 'tags');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;