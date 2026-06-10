-- ==============================
-- 绩效考核测试数据填充脚本
-- 覆盖2024/2025/2026三年,多个教师,多种成果类型
-- ==============================

-- 2024年 - teacher_new_01 (id=6) - 核心产出大年
INSERT INTO biz_project (user_id, college_id, name, classification, level, funds, start_date, end_date, project_source, project_number, leader, participants, status, create_time) VALUES
(6, 1, '基于深度学习的图像识别研究', '科研', '国家级', 50.00, '2024-03-01', '2024-12-31', '国家自然科学基金', 'NSFC-2024-001', '赵新来的', '李四,王五', 3, '2024-03-15 10:00:00'),
(6, 1, '智慧校园大数据分析平台', '科研', '省部级', 30.00, '2024-06-01', '2025-06-01', '省科技厅', 'PROV-2024-088', '赵新来的', '刘能', 3, '2024-06-10 14:00:00'),
(6, 1, 'AI辅助教学系统研究与开发', '教学', '校级', 10.00, '2024-09-01', '2025-09-01', '校内立项', 'SCH-2024-012', '赵新来的', '', 3, '2024-09-05 09:00:00');

INSERT INTO biz_paper (user_id, title, journal_name, issn, impact_factor, pages, is_sci, sci_partition, publish_date, authors, corresponding_author, proof_file, status, classification, create_time) VALUES
(6, 1, 'Deep Learning for Medical Image Analysis', 'Nature Medicine', '1078-8956', 58.700, '123-145', 1, '一区', '2024-02-15', 'Zhao X, Li S, Wang W', 'Zhao X', '/files/p1.pdf', 3, '科研', '2024-02-20 08:00:00'),
(6, 1, 'A Novel CNN Architecture for Object Detection', 'IEEE TPAMI', '0162-8828', 24.310, '456-478', 1, '一区', '2024-05-10', 'Zhao X, Liu N', 'Zhao X', '/files/p2.pdf', 3, '科研', '2024-05-15 10:00:00'),
(6, 1, '大数据驱动的智慧教育研究综述', '计算机学报', '0254-4164', 5.200, '890-910', 0, NULL, '2024-08-20', '赵新来的,刘能,王五', '赵新来的', '/files/p3.pdf', 3, '科研', '2024-08-25 16:00:00'),
(6, 1, '改进的Transformer模型在NLP中的应用', '软件学报', '1000-9825', 4.800, '234-256', 0, NULL, '2024-11-01', '赵新来的', '赵新来的', '/files/p4.pdf', 3, '科研', '2024-11-05 11:00:00');

INSERT INTO biz_patent (user_id, name, patent_no, patent_type, apply_date, grant_date, owner, proof_file, status, classification, create_time) VALUES
(6, 1, '一种基于深度学习的图像分类方法', 'ZL202410012345.6', '发明专利', '2024-01-15', '2024-06-20', '赵新来的', '/files/pt1.pdf', 3, '科研', '2024-06-25 09:00:00'),
(6, 1, '一种智能教学管理系统', 'ZL202420012346.3', '实用新型', '2024-04-10', '2024-08-15', '赵新来的', '/files/pt2.pdf', 3, '科研', '2024-08-20 14:00:00');

INSERT INTO biz_software_copyright (user_id, name, register_no, grant_date, authors, software_type, proof_file, status, classification, create_time) VALUES
(6, 1, '高校科研数据可视化平台V1.0', '2024SR0123456', '2024-07-15', '赵新来的', '应用软件', '/files/sw1.pdf', 3, '科研', '2024-07-20 10:00:00'),
(6, 1, '在线教学资源管理系统V2.0', '2024SR0234567', '2024-10-20', '赵新来的,刘能', '应用软件', '/files/sw2.pdf', 3, '教学', '2024-10-25 15:00:00');

INSERT INTO biz_book (user_id, name, publisher, isbn, publish_date, authors, editor, book_type, level, proof_file, status, classification, create_time) VALUES
(6, 1, '深度学习理论与实践', '科学出版社', '978-7-03-070123-5', '2024-03-01', '赵新来的', '赵新来的', '学术专著', '国家级出版社', '/files/bk1.pdf', 3, '科研', '2024-03-10 09:00:00'),
(8, 1, 'Python数据分析入门', '清华大学出版社', '978-7-302-58123-4', '2024-09-01', '刘能,李四', '刘能', '教材', '国家级出版社', '/files/bk2.pdf', 3, '教学', '2024-09-10 14:00:00');

INSERT INTO biz_award (user_id, award_name, award_level, award_grade, award_unit, award_date, ranking, proof_file, status, classification, create_time) VALUES
(6, 1, '国家科技进步奖', '国家级', '一等奖', '科技部', '2024-12-01', '1', '/files/aw1.pdf', 3, '科研', '2024-12-10 16:00:00'),
(8, 1, '省级教学成果奖', '省部级', '二等奖', '省教育厅', '2024-11-15', '2', '/files/aw2.pdf', 3, '教学', '2024-11-20 10:00:00');

-- 2025年
INSERT INTO biz_project (user_id, college_id, name, classification, level, funds, start_date, end_date, project_source, project_number, leader, participants, status, create_time) VALUES
(6, 1, '乡村振兴与电商结合模式研究', '科研', '国家级', 80.00, '2025-03-01', '2026-12-31', '国家社科基金', 'NSSFC-2025-001', '赵新来的', '刘能,王五', 3, '2025-03-20 08:00:00'),
(8, 1, 'Python在线教育平台开发', '教学', '省部级', 15.00, '2025-04-01', '2026-04-01', '省教育厅', 'EDU-2025-056', '刘能', '', 3, '2025-04-10 10:00:00'),
(9, 1, '机械工程数字化设计平台', '科研', '省部级', 25.00, '2025-05-01', '2026-05-01', '省科技厅', 'PROV-2025-123', '李四', '', 3, '2025-05-05 14:00:00');

INSERT INTO biz_paper (user_id, title, journal_name, issn, impact_factor, pages, is_sci, sci_partition, publish_date, authors, corresponding_author, proof_file, status, classification, create_time) VALUES
(6, 1, 'Federated Learning for Privacy-Preserving AI', 'IEEE TIFS', '1556-6013', 13.480, '567-589', 1, '二区', '2025-01-20', 'Zhao X, Wang W', 'Zhao X', '/files/p5.pdf', 3, '科研', '2025-01-25 09:00:00'),
(6, 1, '教育数字化转型的关键技术研究', '电化教育研究', '1003-1553', 3.800, '45-60', 0, NULL, '2025-04-15', '赵新来的', '赵新来的', '/files/p6.pdf', 3, '教学', '2025-04-20 15:00:00'),
(8, 1, 'Machine Learning Applications in Finance', 'Expert Systems with Applications', '0957-4174', 8.500, '890-912', 1, '二区', '2025-06-10', 'Liu N, Li S', 'Liu N', '/files/p7.pdf', 3, '科研', '2025-06-15 11:00:00'),
(9, 1, '智能制造中的数据驱动方法', '机械工程学报', '0577-6686', 2.800, '123-140', 0, NULL, '2025-08-01', '李四', '李四', '/files/p8.pdf', 3, '科研', '2025-08-05 08:00:00');

INSERT INTO biz_patent (user_id, name, patent_no, patent_type, apply_date, grant_date, owner, proof_file, status, classification, create_time) VALUES
(8, 1, '一种在线教育互动系统', 'ZL202510012345.7', '发明专利', '2025-02-01', '2025-07-15', '刘能', '/files/pt3.pdf', 3, '教学', '2025-07-20 10:00:00'),
(9, 1, '一种机械臂精准控制系统', 'ZL202520012346.4', '实用新型', '2025-03-10', '2025-08-20', '李四', '/files/pt4.pdf', 3, '科研', '2025-08-25 14:00:00');

INSERT INTO biz_software_copyright (user_id, name, register_no, grant_date, authors, software_type, proof_file, status, classification, create_time) VALUES
(8, 1, '在线考试防作弊系统V1.0', '2025SR0345678', '2025-05-10', '刘能', '应用软件', '/files/sw3.pdf', 3, '教学', '2025-05-15 09:00:00');

INSERT INTO biz_competition (user_id, name, competition_level, award_level, award_grade, award_date, student_name, ranking, cert_file_url, status, classification, create_time) VALUES
(6, 1, '全国大学生人工智能大赛', '国家级', '国家级', '一等奖', '2025-06-15', '张某,李某', '指导教师', '/files/comp1.pdf', 3, '教学', '2025-06-20 16:00:00'),
(8, 1, '全国数学建模竞赛', '国家级', '国家级', '二等奖', '2025-09-20', '王某,赵某', '指导教师', '/files/comp2.pdf', 3, '教学', '2025-09-25 10:00:00');

INSERT INTO biz_course (user_id, course_name, course_type, course_level, start_date, description, proof_file, status, classification, create_time) VALUES
(6, 1, '深度学习导论', '一流课程', '省级', '2025-03-01', '面向本科生的人工智能核心课程', '/files/co1.pdf', 3, '教学', '2025-03-05 09:00:00'),
(8, 1, 'Python程序设计', '精品课程', '校级', '2025-09-01', '面向全校的通识编程课程', '/files/co2.pdf', 3, '教学', '2025-09-05 14:00:00');

-- 2026年
INSERT INTO biz_project (user_id, college_id, name, classification, level, funds, start_date, end_date, project_source, project_number, leader, participants, status, create_time) VALUES
(6, 1, '大语言模型在教育领域的应用研究', '科研', '国家级', 60.00, '2026-02-01', '2027-12-31', '国家自然科学基金', 'NSFC-2026-056', '赵新来的', '刘能,李四,王五', 3, '2026-02-15 10:00:00'),
(8, 1, '智慧校园管理系统', '教学', '校级', 8.00, '2026-03-01', '2027-03-01', '校内立项', 'SCH-2026-034', '刘能', '赵新来的', 3, '2026-03-10 09:00:00'),
(15, 1, '基于SpringBoot的科研管理系统', '科研', '校级', 5.00, '2026-01-01', '2026-06-30', '校内立项', 'SCH-2026-001', 'sxh', '', 3, '2026-01-15 14:00:00');

INSERT INTO biz_paper (user_id, title, journal_name, issn, impact_factor, pages, is_sci, sci_partition, publish_date, authors, corresponding_author, proof_file, status, classification, create_time) VALUES
(6, 1, 'LLM-based Automated Code Review System', 'ACM Computing Surveys', '0360-0300', 18.600, '1-35', 1, '一区', '2026-01-15', 'Zhao X, Liu N, Li S', 'Zhao X', '/files/p9.pdf', 3, '科研', '2026-01-20 08:00:00'),
(8, 1, 'A Survey of Educational Data Mining', 'Computers & Education', '0360-1315', 10.200, '45-67', 1, '二区', '2026-03-01', 'Liu N', 'Liu N', '/files/p10.pdf', 3, '科研', '2026-03-05 10:00:00'),
(15, 1, '高校科研信息管理系统的设计与实现', '软件工程', '2096-1472', 0.800, '12-18', 0, NULL, '2026-04-01', 'sxh', 'sxh', '/files/p11.pdf', 3, '科研', '2026-04-05 15:00:00');

INSERT INTO biz_patent (user_id, name, patent_no, patent_type, apply_date, grant_date, owner, proof_file, status, classification, create_time) VALUES
(15, 1, '一种科研成果自动分类方法', 'ZL202610012345.8', '发明专利', '2026-03-01', '2026-05-20', 'sxh', '/files/pt5.pdf', 3, '科研', '2026-05-25 09:00:00');

INSERT INTO biz_software_copyright (user_id, name, register_no, grant_date, authors, software_type, proof_file, status, classification, create_time) VALUES
(15, 1, '科研管理系统V1.0', '2026SR0456789', '2026-06-01', 'sxh', '应用软件', '/files/sw4.pdf', 3, '科研', '2026-06-05 10:00:00');

-- 共享成果
INSERT INTO biz_achievement_share (achievement_type, achievement_id, owner_user_id, shared_user_id, role_type, contribution_percent, create_time) VALUES
('paper', 11, 6, 8, 'CO_AUTHOR', 50, '2026-01-20 08:00:00'),
('project', 22, 6, 8, 'CO_LEADER', 40, '2026-02-15 10:00:00'),
('paper', 11, 6, 15, 'CO_AUTHOR', 30, '2026-01-20 08:00:00');
