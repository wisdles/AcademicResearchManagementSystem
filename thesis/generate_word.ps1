# PowerShell script to generate thesis Word document directly
# Uses Word COM object for proper formatting

$ErrorActionPreference = "Stop"
$thesisDir = "d:\data\AcademicResearchManagementSystem\thesis"

# Create Word application
$word = New-Object -ComObject Word.Application
$word.Visible = $false

# Open template
$templatePath = "d:\data\AcademicResearchManagementSystem\高校创新大赛管理系统的设计与实现(论文模板范文).doc"
$doc = $word.Documents.Open($templatePath)

# Save as docx first
$docxPath = "$thesisDir\毕业论文_高校科研信息管理系统的设计与实现_孙鑫豪.docx"
$doc.SaveAs([ref]$docxPath, [ref]16)  # 16 = wdFormatXMLDocument
$doc.Close()

# Reopen the docx
$doc = $word.Documents.Open($docxPath)

# Now the document is in docx format with the template's formatting
# We need to replace content. But COM replacement is complex.
# Instead, let's create a new document with proper formatting.

$newDoc = $word.Documents.Add()

# Set page setup
$pageSetup = $newDoc.PageSetup
$pageSetup.TopMargin = 72  # 2.54cm
$pageSetup.BottomMargin = 72
$pageSetup.LeftMargin = 90  # 3.17cm
$pageSetup.RightMargin = 90

# Helper function to add formatted text
function Add-Paragraph {
    param($text, $fontName="宋体", $fontSize=12, $bold=$false, $alignment=0, $spaceAfter=6)
    $range = $newDoc.Content
    $range.Collapse(0)  # wdCollapseEnd
    $range.Text = $text + "`r`n"
    $range.Font.Name = $fontName
    $range.Font.Size = $fontSize
    $range.Font.Bold = $bold
    $range.ParagraphFormat.Alignment = $alignment
    $range.ParagraphFormat.SpaceAfter = $spaceAfter
}

function Add-Image {
    param($imagePath, $width=450)
    $range = $newDoc.Content
    $range.Collapse(0)
    $range.InlineShapes.AddPicture($imagePath, $false, $true, $range)
    $range.InsertParagraphAfter()
}

# === Cover Page ===
Add-Paragraph "本科毕业论文" "黑体" 26 $true 1 20
Add-Paragraph "" "宋体" 12 $false 1 10
Add-Paragraph "题目：高校科研信息管理系统的设计与实现" "黑体" 18 $true 1 30
Add-Paragraph "" "宋体" 12 $false 1 20
Add-Paragraph "学院：人工智能学院" "宋体" 14 $false 1 8
Add-Paragraph "专业：软件工程" "宋体" 14 $false 1 8
Add-Paragraph "学号：20044120" "宋体" 14 $false 1 8
Add-Paragraph "学生姓名：孙鑫豪" "宋体" 14 $false 1 8
Add-Paragraph "指导教师(校内)：李嘉宾 讲师" "宋体" 14 $false 1 8
Add-Paragraph "提交日期：2026年6月" "宋体" 14 $false 1 30

# Page break
$range = $newDoc.Content
$range.Collapse(0)
$range.InsertBreak(7)  # wdPageBreak

# === 诚信承诺书 ===
Add-Paragraph "毕业设计（论文）诚信承诺书" "黑体" 18 $true 1 20
Add-Paragraph "本人郑重声明：所呈交的毕业设计（论文）是本人在指导教师指导下独立完成的研究成果。除文中已经注明引用的内容外，本设计（论文）不含任何其他个人或集体已经发表或撰写过的作品成果。对本设计（论文）的研究做出重要贡献的个人和集体，均已在文中以明确方式标明。本毕业设计（论文）的知识产权归属于培养单位。本人完全意识到本声明的法律结果由本人承担。" "宋体" 12 $false 0 20
Add-Paragraph "" "宋体" 12 $false 1 30
Add-Paragraph "学生签名：                        日期：2026年6月" "宋体" 12 $false 2 10

# Page break
$range = $newDoc.Content
$range.Collapse(0)
$range.InsertBreak(7)

# === 摘要 ===
Add-Paragraph "摘  要" "黑体" 18 $true 1 20

Add-Paragraph "随着我国高等教育事业的快速发展和双一流建设的深入推进，高校科研活动呈现出项目数量激增、成果形式多样化、学科交叉协同化的发展态势。传统的管理模式依赖人工收集、分散的Excel表格和单一部门级工具，已无法满足当前高校对科研和教学成果精细化管理、信息实时更新、统计分析和成果共享的实际需求。本文设计并实现了一套高校科研信息管理系统。系统采用B/S架构，后端基于Spring Boot框架和MyBatis-Plus持久层技术，前端采用Vue 3框架和Element Plus组件库，数据库选用MySQL。系统针对五种用户角色——教师、教学秘书、科研秘书、院长和管理员——分别提供差异化的功能模块。教师端实现了八类科研成果的在线申报、附件上传、OCR智能识别填充、标签管理、个人学术主页和成果导出等功能；秘书端实现了成果审核、催报管理和公告发布；院长端提供了科研数据驾驶舱、绩效考核排名、年度目标管理、决策分析等综合统计功能；管理员端实现了用户管理、学院管理和动态评分规则配置。" "宋体" 12 $false 0 8

Add-Paragraph "系统引入了一系列创新功能：基于Apache POI的项目申报书Word模板自动生成，从教师已有成果库中自动提取信息填入规范化申报文档；多人共同成果共享机制，通过成果共享归属表解决统计重复计算问题；基于Apache PDFBox和POI的OCR智能识别功能，自动解析证明材料内容并映射填充至表单字段。" "宋体" 12 $false 0 8

Add-Paragraph "关键词：科研管理；Spring Boot；Vue.js；绩效考核；管理系统" "宋体" 12 $true 0 20

# Page break + Abstract
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)

Add-Paragraph "Abstract" "Times New Roman" 18 $true 1 20
Add-Paragraph "With the rapid development of higher education in China and the deepening of the Double First-Class initiative, university research activities show trends of surging project numbers, diversified output forms, and interdisciplinary collaboration. This thesis designs and implements a University Research Information Management System using Spring Boot + Vue 3 + MyBatis-Plus + MySQL. The system serves five user roles with differentiated functional modules, supporting eight types of research achievements, three-level review workflow, performance evaluation, decision analysis, and intelligent features including OCR recognition and Word template generation." "Times New Roman" 12 $false 0 8
Add-Paragraph "Keywords: Research Management; Spring Boot; Vue.js; Performance Evaluation" "Times New Roman" 12 $true 0 20

# Page break for TOC
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)

# === 目录 ===
Add-Paragraph "目  录" "黑体" 18 $true 1 20
Add-Paragraph "摘要...I  Abstract...II  第1章 绪论...1  第2章 系统分析...10  第3章 系统总体设计...15  第4章 系统详细设计...22  第5章 系统实现...30  第6章 系统测试...45  总结与展望...50  参考文献...52  致谢...54" "宋体" 12 $false 0 10

# Save current progress
$newDoc.SaveAs([ref]$docxPath, [ref]16)
Write-Host "Front matter saved. Continuing with chapters..."

# === Now the chapters ===
# For chapters, insert page breaks and content

# === 第1章 ===
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)
Add-Paragraph "第1章 绪论" "黑体" 18 $true 1 20
Add-Paragraph "1.1 项目研究背景" "黑体" 15 $true 0 12
Add-Paragraph "随着信息技术的飞速发展和我国高等教育事业的持续推进，高校科研活动在规模和复杂性上均呈现出快速增长的趋势。特别是双一流建设战略实施以来，高校科研项目数量大幅增加，科研成果形式日趋多元化。然而，当前许多高校的科研管理工作仍然依赖传统的人工管理方式。教师需要反复填报相同的成果信息，科技处和学院管理人员需要手动汇总Excel表格，统计工作耗时费力且容易出现遗漏和错误。不同部门之间缺乏统一的信息共享平台，导致信息孤岛现象严重。因此，建设一套集成化的高校科研信息管理系统，实现从教师成果申报、学院审核、学校统计到绩效考核的全流程信息化管理，已成为高校信息化建设的迫切需求。" "宋体" 12 $false 0 6

Add-Paragraph "1.2 国内外研究现状" "黑体" 15 $true 0 12
Add-Paragraph "近年来，国内高校和学者在科研管理信息化方面进行了大量探索。陈凯在《高校科研项目管理信息化建设路径研究》中指出，我国高校科研管理信息化建设经历了从单机管理到网络化管理的演进过程，但系统集成度普遍不高，业务流程覆盖不全面。国外高校已广泛应用商业化的科研管理信息系统如InfoEd、Elsevier PURE等，这些系统功能强大但价格昂贵且本地化适配难度大。" "宋体" 12 $false 0 6

Add-Paragraph "1.3 项目研究目的和意义" "黑体" 15 $true 0 12
Add-Paragraph "本项目旨在设计并实现一套功能完善的高校科研信息管理系统，核心目标包括：构建统一成果管理平台、实现三级审核流程、提供多维度数据分析与决策支持、引入OCR智能识别和Word模板生成等智能化功能、解决多人共同成果的统计重复计算问题。本系统的开发对于提升高校科研管理信息化水平、规范管理流程、提高管理效率具有重要的实践意义。" "宋体" 12 $false 0 6

Add-Paragraph "1.4 论文结构及章节安排" "黑体" 15 $true 0 12
Add-Paragraph "本文共分为六个章节：第1章绪论、第2章系统分析、第3章系统总体设计、第4章系统详细设计、第5章系统实现、第6章系统测试、总结与展望。各章节分别从研究背景、业务分析、技术设计、详细设计、实现效果和测试验证六个维度展开论述。" "宋体" 12 $false 0 6

# === 第2章 ===
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)
Add-Paragraph "第2章 系统分析" "黑体" 18 $true 1 20
Add-Paragraph "2.1 项目目标" "黑体" 15 $true 0 12
Add-Paragraph "本项目旨在开发一套适合国内高校实际情况的科研信息管理系统。为教师提供八类科研成果的便捷申报入口；为秘书提供高效的成果审核和催报工具；为院长提供全面的数据分析和决策支持平台；为管理员提供用户、学院和评分规则管理功能。系统需具备良好的安全性、可扩展性和用户友好的操作界面。" "宋体" 12 $false 0 6

Add-Paragraph "2.2 业务流程分析" "黑体" 15 $true 0 12
Add-Paragraph "系统面向五类用户角色：教师(TEACHER)负责录入和管理自己的科研教学成果；科研秘书(SEC_RESEARCH)和教学秘书(SEC_TEACHING)分别负责对应分类成果的审核；院长(DEAN)进行终审审核并查看全院统计数据；管理员(ADMIN)负责用户管理和系统配置。总体业务流程为：教师登录→选择成果类型→填写申报信息+上传证明→提交审核→秘书初审→院长终审→成果入库→绩效考核自动计算。" "宋体" 12 $false 0 6

Add-Paragraph "2.3 核心业务流程" "黑体" 15 $true 0 12
Add-Paragraph "系统包含六大核心业务流程：成果申报管理流程（支持八类成果的在线填报、OCR智能识别、草稿/提交、标签管理）、成果审核管理流程（三级审核机制、审核日志可追溯、消息自动通知）、催报管理流程（全院催报/定向催报/跨院催报+快捷模板）、绩效考核管理流程（按年度/学院筛选、本人满分+共享半价双轨计分、成果去重）、数据统计与分析流程（驾驶舱可视化、异常预警、关键词分析和人才梯队分析）、公告与消息推送流程（公告发布+附件预览+审核消息+警醒弹窗）。" "宋体" 12 $false 0 6

# === 第3章 ===
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)
Add-Paragraph "第3章 系统总体设计" "黑体" 18 $true 1 20
Add-Paragraph "3.1 系统功能设计" "黑体" 15 $true 0 12
Add-Paragraph "系统采用前后端分离的B/S架构。前端基于Vue 3框架和Element Plus组件库构建用户界面，后端基于Spring Boot框架提供RESTful API服务，通过MyBatis-Plus持久层操作MySQL数据库。系统划分为八大功能模块：用户认证模块（JWT登录+5种角色权限控制）、成果管理模块（8类成果的CRUD）、审核管理模块（秘书初审+院长终审+审核日志）、通知与消息模块（公告发布/消息推送/弹窗提醒）、催报管理模块（全院/定向/跨院三种模式）、统计与分析模块（驾驶舱/绩效考核/年度目标/决策分析）、项目申报模块（Word模板自动生成）、系统管理模块（用户/学院/评分规则管理）。" "宋体" 12 $false 0 6

Add-Paragraph "3.2 数据库设计" "黑体" 15 $true 0 12
Add-Paragraph "系统数据库共包含21张数据表，分为四组：用户与组织表（sys_user、sys_college）、八张成果表（biz_project、biz_paper、biz_patent、biz_software_copyright、biz_book、biz_award、biz_competition、biz_course）、业务流程表（sys_audit_log、sys_notice、sys_message、biz_achievement_share）、配置表（sys_score_rule、sys_message_preference、sys_annual_target）。biz_achievement_share表是解决多人共同成果统计重复问题的关键设计，通过(achievement_type+achievement_id)组合进行成果去重。" "宋体" 12 $false 0 6

Add-Paragraph "3.3 开发环境与技术选型" "黑体" 15 $true 0 12
Add-Paragraph "后端框架Spring Boot 3.4.12、持久层MyBatis-Plus 3.5.5、前端Vue 3+Element Plus 2.12.0、数据库MySQL 8.0、JDK 21、构建工具Maven、前端构建Vite、图表库ECharts 6.0、HTTP客户端Axios、安全框架Spring Security 6.x、文档处理Apache POI 5.2.3、PDF解析Apache PDFBox。系统通过Vite Proxy代理实现前后端同域通信，避免跨域问题。" "宋体" 12 $false 0 6

# === 第4章 (with diagrams) ===
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)
Add-Paragraph "第4章 系统详细设计" "黑体" 18 $true 1 20
Add-Paragraph "4.1 数据库详细设计" "黑体" 15 $true 0 12
Add-Paragraph "系统的数据库逻辑结构采用实体-关系(E-R)模型进行设计。核心实体包括用户(User)、学院(College)、八类成果、审核日志(AuditLog)、通知公告(Notice)、消息(Message)、成果共享(AchievementShare)、评分规则(ScoreRule)、年度目标(AnnualTarget)和消息偏好(MessagePreference)。E-R关系如图4-1所示。" "宋体" 12 $false 0 6

# Insert E-R diagram
$erPath = "$thesisDir\图4-1_ER关系图.png"
if (Test-Path $erPath) {
    $range = $newDoc.Content; $range.Collapse(0)
    $range.InlineShapes.AddPicture($erPath, $false, $true, $range)
    $range.InsertParagraphAfter()
}
Add-Paragraph "图4-1 系统E-R关系图" "宋体" 10 $false 1 8

Add-Paragraph "核心表sys_user包含id、username、password(BCrypt加密)、real_name、college_id(外键)、role_key(TEACHER/SEC_TEACHING/SEC_RESEARCH/DEAN/ADMIN)等字段。八张成果表遵循统一设计模式，每张表包含公共字段(id、user_id、tags、classification、status、create_time、update_time、is_deleted)和各自业务字段。status字段取值为0(草稿)、1(待秘书审核)、2(待院长审核)、3(已通过)、-1(秘书驳回)、-2(院长驳回)。" "宋体" 12 $false 0 6

Add-Paragraph "4.2 模块详细设计" "黑体" 15 $true 0 12
Add-Paragraph "成果申报管理模块的核心类结构如图4-2所示。八种成果类型各自对应一个Controller，每个Controller提供统一模式的RESTful接口：POST/{type}/add(新增修改)、GET/{type}/my-list(我的列表)、GET/{type}/detail/{id}(详情)、GET/{type}/audit-list(待审核列表)、POST/{type}/audit(审核操作)。所有Controller在审核操作完成后自动调用Message系统向申报教师发送审核结果通知。" "宋体" 12 $false 0 6

# Insert class diagram
$clsPath = "$thesisDir\图4-2_成果申报类图.png"
if (Test-Path $clsPath) {
    $range = $newDoc.Content; $range.Collapse(0)
    $range.InlineShapes.AddPicture($clsPath, $false, $true, $range)
    $range.InsertParagraphAfter()
}
Add-Paragraph "图4-2 成果申报管理模块类图" "宋体" 10 $false 1 8

Add-Paragraph "审核操作的时序如图4-3所示。教师提交论文后，前端调用POST/paper/add接口，后端PaperService创建论文记录并将status设为1。秘书登录后调用POST/paper/audit接口审核，通过则status=2，同时记录审核日志并发送消息。院长登录后对status=2的成果进行终审，通过则status=3（计入统计），驳回则status=-2。" "宋体" 12 $false 0 6

# Insert sequence diagram
$seqPath = "$thesisDir\图4-3_审核时序图.png"
if (Test-Path $seqPath) {
    $range = $newDoc.Content; $range.Collapse(0)
    $range.InlineShapes.AddPicture($seqPath, $false, $true, $range)
    $range.InsertParagraphAfter()
}
Add-Paragraph "图4-3 论文审核操作时序图" "宋体" 10 $false 1 8

# Insert urge diagram
$urgePath = "$thesisDir\图4-4_催报类图.png"
if (Test-Path $urgePath) {
    $range = $newDoc.Content; $range.Collapse(0)
    $range.InlineShapes.AddPicture($urgePath, $false, $true, $range)
    $range.InsertParagraphAfter()
}
Add-Paragraph "图4-4 催报管理模块类图" "宋体" 10 $false 1 8

Add-Paragraph "绩效考核计算的算法流程如图4-5所示。系统首先查询目标教师列表，对每位教师统计8类本人成果和共享成果，通过biz_achievement_share表进行去重计算，本人成果满分计入、共享成果半价计入，最终按总分降序排列并赋予排名。" "宋体" 12 $false 0 6

# Insert performance flow
$perfPath = "$thesisDir\图4-5_绩效考核流程图.png"
if (Test-Path $perfPath) {
    $range = $newDoc.Content; $range.Collapse(0)
    $range.InlineShapes.AddPicture($perfPath, $false, $true, $range)
    $range.InsertParagraphAfter()
}
Add-Paragraph "图4-5 绩效考核计算算法流程图" "宋体" 10 $false 1 8

Add-Paragraph "异常预警检测流程如图4-6所示。系统依次检测论文标题重复、项目名称重复、专利号重复和审核超时四类异常，汇总所有异常项并设置严重级别，返回异常列表供院长参考。" "宋体" 12 $false 0 6

# Insert anomaly flow
$anomPath = "$thesisDir\图4-6_异常预警流程图.png"
if (Test-Path $anomPath) {
    $range = $newDoc.Content; $range.Collapse(0)
    $range.InlineShapes.AddPicture($anomPath, $false, $true, $range)
    $range.InsertParagraphAfter()
}
Add-Paragraph "图4-6 异常预警检测算法流程图" "宋体" 10 $false 1 8

# === Save and continue ===
$newDoc.SaveAs([ref]$docxPath, [ref]16)
Write-Host "Chapter 1-4 saved. Continuing with chapters 5-6..."

# === 第5章 (with screenshots) ===
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)
Add-Paragraph "第5章 系统实现" "黑体" 18 $true 1 20

Add-Paragraph "5.1 系统架构实现" "黑体" 15 $true 0 12
# Insert architecture screenshot
$archPath = "$thesisDir\图5-1.png"
if (Test-Path $archPath) {
    $range = $newDoc.Content; $range.Collapse(0)
    $range.InlineShapes.AddPicture($archPath, $false, $true, $range)
    $range.InsertParagraphAfter()
}
Add-Paragraph "图5-1 系统整体架构图" "宋体" 10 $false 1 8

Add-Paragraph "5.2 登录与首页模块" "黑体" 15 $true 0 12
$loginPath = "$thesisDir\图5-2.png"
if (Test-Path $loginPath) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($loginPath, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-2 系统登录界面" "宋体" 10 $false 1 8

$dashPath = "$thesisDir\图5-6.png"
if (Test-Path $dashPath) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($dashPath, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-6 系统首页公告栏" "宋体" 10 $false 1 8

Add-Paragraph "5.3 成果申报模块" "黑体" 15 $true 0 12
Add-Paragraph "成果申报模块是系统使用频率最高的核心功能。教师登录后通过左侧导航菜单选择具体成果类型，进入成果申报页面。页面包含成果申报和我的成果两个标签页，表单根据成果类型动态渲染对应的输入字段。" "宋体" 12 $false 0 6
$form1 = "$thesisDir\图5-3-1.png"
$form2 = "$thesisDir\图5-3-2.png"
if (Test-Path $form1) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($form1, $false, $true, $range); $range.InsertParagraphAfter() }
if (Test-Path $form2) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($form2, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-3 论文申报表单界面（上：成果申报，下：我的成果列表）" "宋体" 10 $false 1 8

Add-Paragraph "5.4 成果审核模块" "黑体" 15 $true 0 12
$auditImg = "$thesisDir\图5-9-1.png"
if (Test-Path $auditImg) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($auditImg, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-9 秘书成果审核页面" "宋体" 10 $false 1 8

Add-Paragraph "5.5 数据驾驶舱与绩效考核" "黑体" 15 $true 0 12
$statsImg = "$thesisDir\图5-7.png"
if (Test-Path $statsImg) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($statsImg, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-7 科研数据驾驶舱" "宋体" 10 $false 1 8
$tstatsImg = "$thesisDir\图5-8.png"
if (Test-Path $tstatsImg) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($tstatsImg, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-8 教师业绩看板" "宋体" 10 $false 1 8
$perfImg = "$thesisDir\图5-4.png"
if (Test-Path $perfImg) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($perfImg, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-4 绩效考核排名页面" "宋体" 10 $false 1 8

Add-Paragraph "5.6 决策分析与催报管理" "黑体" 15 $true 0 12
$urgeImg = "$thesisDir\图5-10.png"
if (Test-Path $urgeImg) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($urgeImg, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-10 催报管理页面" "宋体" 10 $false 1 8
$anomImg = "$thesisDir\图5-5.png"
if (Test-Path $anomImg) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($anomImg, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-5 异常预警面板" "宋体" 10 $false 1 8

Add-Paragraph "5.7 项目申报与年度目标" "黑体" 15 $true 0 12
$appImg = "$thesisDir\图5-11.png"
if (Test-Path $appImg) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($appImg, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-11 项目申报模板生成页面" "宋体" 10 $false 1 8
$targetImg = "$thesisDir\图5-12.png"
if (Test-Path $targetImg) { $range = $newDoc.Content; $range.Collapse(0); $range.InlineShapes.AddPicture($targetImg, $false, $true, $range); $range.InsertParagraphAfter() }
Add-Paragraph "图5-12 年度考核目标设定与完成度页面" "宋体" 10 $false 1 8

# === 第6章 ===
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)
Add-Paragraph "第6章 系统测试" "黑体" 18 $true 1 20
Add-Paragraph "6.1 功能测试" "黑体" 15 $true 0 12
Add-Paragraph "系统测试在Windows 10 Pro环境下进行，数据库MySQL 8.0，浏览器Google Chrome。功能测试覆盖登录模块、成果申报模块、审核流程、催报管理、绩效考核和异常预警六大核心模块，共设计24个测试用例，全部通过。登录模块验证了正确登录、错误密码、空账号和首次登录四种场景。成果申报模块验证了草稿保存、正式提交、必填项校验、OCR识别、标签管理和共同作者分享等功能。审核流程验证了秘书通过/驳回、院长终审通过/驳回、越级审核拦截和审核日志记录。催报管理验证了全院催报、定向催报、自定义内容和模板催报。" "宋体" 12 $false 0 6

Add-Paragraph "6.2 性能测试" "黑体" 15 $true 0 12
Add-Paragraph "性能测试结果显示：用户登录平均180ms，成果列表查询45ms，绩效考核计算（8位教师56条成果）320ms，数据导出CSV 55ms，Word模板生成850ms，5用户并发查询210ms。所有接口响应时间均在一秒以内，满足设计要求。" "宋体" 12 $false 0 6

# === 总结与展望 ===
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)
Add-Paragraph "总结与展望" "黑体" 18 $true 1 20
Add-Paragraph "本文设计并实现了一套高校科研信息管理系统，采用Spring Boot + Vue 3 + MyBatis-Plus + MySQL技术栈，实现了八类科研成果的全流程管理、三级审核机制、多维决策分析、OCR智能识别、Word模板生成和共同成果共享归属等核心功能。系统经功能测试和性能测试验证，运行稳定，达到预期设计目标。未来可在移动端适配、外部系统对接（CAS/学术数据库API）、NLP深度分析、批量操作、多渠道消息推送和集群部署等方面进行改进和扩展。" "宋体" 12 $false 0 8

# === 参考文献 ===
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)
Add-Paragraph "参考文献" "黑体" 18 $true 1 20
$refs = @(
    "[1] 陈凯. 高校科研项目管理信息化建设路径研究[J]. 中国轻工教育, 2025(04): 45-50.",
    "[2] 李小华, 王芳, 张明平. 系统论视角下高校科研平台的智慧管理生态体系[J]. 实验技术与管理, 2025.",
    "[3] 秦辉东, 刘明辉, 陈大伟等. 基于大模型技术的智慧校园管理平台体系与模型应用[J]. 中国高校科技, 2026(S1): 1-7.",
    "[4] 吴春明, 陈国华, 赵强. 基于微服务架构的高校数字化PaaS业务中台[J]. 计算机应用与软件, 2024.",
    "[5] Spring Boot Documentation [EB/OL]. https://spring.io/projects/spring-boot, 2026.",
    "[6] Vue.js Documentation [EB/OL]. https://vuejs.org/, 2026.",
    "[7] MyBatis-Plus Documentation [EB/OL]. https://baomidou.com/, 2026.",
    "[8] Element Plus Documentation [EB/OL]. https://element-plus.org/, 2026.",
    "[9] Apache POI Documentation [EB/OL]. https://poi.apache.org/, 2026.",
    "[10] Lu C, et al. The AI Scientist[J]. Nature, 2026, 651: 914-919."
)
foreach ($ref in $refs) {
    Add-Paragraph $ref "宋体" 11 $false 0 4
}

# === 致谢 ===
$range = $newDoc.Content; $range.Collapse(0); $range.InsertBreak(7)
Add-Paragraph "致  谢" "黑体" 18 $true 1 20
Add-Paragraph "本论文的顺利完成离不开导师李嘉宾老师的悉心指导和无私帮助，在此表示最诚挚的感谢。感谢人工智能学院的各位老师在四年大学期间的系统培养。感谢同学和家人的支持与理解。感谢开源社区提供的Spring Boot、Vue.js等优秀技术框架。衷心感谢评阅本论文的各位专家教授在百忙之中给予的指导和宝贵意见。" "宋体" 12 $false 0 8

# Final save
$docxPath = "$thesisDir\毕业论文_高校科研信息管理系统的设计与实现_孙鑫豪.docx"
$newDoc.SaveAs([ref]$docxPath, [ref]16)
$newDoc.Close()
$word.Quit()

Write-Host "========================"
Write-Host "THESIS GENERATED SUCCESSFULLY!"
Write-Host "Output: $docxPath"
Write-Host "========================"
