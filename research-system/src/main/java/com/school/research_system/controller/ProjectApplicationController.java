package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.entity.*;
import com.school.research_system.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 项目申报书模板生成
 * 自动从教师已有成果库提取信息，生成规范化 Word 申报书
 */
@RestController
@RequestMapping("/application")
public class ProjectApplicationController {

    @Autowired private IUserService userService;
    @Autowired private IProjectService projectService;
    @Autowired private IPaperService paperService;
    @Autowired private IPatentService patentService;
    @Autowired private IBookService bookService;
    @Autowired private ICollegeService collegeService;

    /**
     * 生成项目申报书 Word 文档
     * @param projectName 拟申报的项目名称
     */
    @GetMapping("/template")
    public void generateTemplate(@RequestParam String projectName, HttpServletResponse response) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) { response.sendError(401); return; }
        Long uid = user.getId();
        College college = collegeService.getById(user.getCollegeId());

        // 提取已有成果作为申报支撑材料
        List<Paper> papers = paperService.list(new LambdaQueryWrapper<Paper>()
                .eq(Paper::getUserId, uid).eq(Paper::getStatus, 3).orderByDesc(Paper::getPublishDate));
        List<Project> projects = projectService.list(new LambdaQueryWrapper<Project>()
                .eq(Project::getUserId, uid).eq(Project::getStatus, 3).orderByDesc(Project::getStartDate));
        List<Patent> patents = patentService.list(new LambdaQueryWrapper<Patent>()
                .eq(Patent::getUserId, uid).eq(Patent::getStatus, 3));
        List<Book> books = bookService.list(new LambdaQueryWrapper<Book>()
                .eq(Book::getUserId, uid).eq(Book::getStatus, 3));

        XWPFDocument doc = new XWPFDocument();

        // 标题
        addTitle(doc, "项目申报书");
        addCenter(doc, "（系统自动生成 · 请补充实际申报内容）");
        addBlank(doc);

        // 一、基本信息
        addH2(doc, "一、项目基本信息");
        addKV(doc, "项目名称", projectName);
        addKV(doc, "申请人", user.getRealName());
        addKV(doc, "工号", user.getUsername());
        addKV(doc, "所在学院", college != null ? college.getName() : "");
        addKV(doc, "申报日期", java.time.LocalDate.now().toString());
        addBlank(doc);

        // 二、研究基础（自动从成果库提取）
        addH2(doc, "二、研究基础与前期成果");
        addText(doc, "申请人已取得的相关研究成果如下：");
        addBlank(doc);

        // 1. 项目经历
        if (!projects.isEmpty()) {
            addH3(doc, "（一）已主持/参与的科研项目（" + projects.size() + " 项）");
            int i = 1;
            for (Project p : projects) {
                addText(doc, i++ + ". " + p.getName()
                        + "  级别：" + nz(p.getLevel())
                        + "  经费：" + (p.getFunds() != null ? p.getFunds() + " 万元" : "未填")
                        + "  起止：" + nz(p.getStartDate()) + " ~ " + nz(p.getEndDate()));
            }
            addBlank(doc);
        }

        // 2. 论文
        if (!papers.isEmpty()) {
            addH3(doc, "（二）已发表的学术论文（" + papers.size() + " 篇）");
            int i = 1;
            for (Paper p : papers) {
                StringBuilder sb = new StringBuilder();
                sb.append(i++).append(". ").append(nz(p.getAuthors())).append(". ")
                        .append(p.getTitle()).append("[J]. ")
                        .append(nz(p.getJournalName()));
                if (p.getPublishDate() != null) sb.append(", ").append(p.getPublishDate());
                if (p.getSciPartition() != null && !p.getSciPartition().isEmpty()) sb.append(" (SCI ").append(p.getSciPartition()).append(")");
                if (p.getImpactFactor() != null) sb.append(" IF=").append(p.getImpactFactor());
                addText(doc, sb.toString());
            }
            addBlank(doc);
        }

        // 3. 专利
        if (!patents.isEmpty()) {
            addH3(doc, "（三）已授权专利（" + patents.size() + " 项）");
            int i = 1;
            for (Patent p : patents) {
                addText(doc, i++ + ". " + p.getName()
                        + "  专利号：" + nz(p.getPatentNo())
                        + "  类型：" + nz(p.getPatentType())
                        + "  授权日：" + nz(p.getGrantDate()));
            }
            addBlank(doc);
        }

        // 4. 专著
        if (!books.isEmpty()) {
            addH3(doc, "（四）出版专著（" + books.size() + " 部）");
            int i = 1;
            for (Book b : books) {
                addText(doc, i++ + ". " + nz(b.getAuthors()) + ". " + b.getName()
                        + "[M]. " + nz(b.getPublisher())
                        + (b.getPublishDate() != null ? ", " + b.getPublishDate() : "")
                        + "  ISBN：" + nz(b.getIsbn()));
            }
            addBlank(doc);
        }

        if (papers.isEmpty() && projects.isEmpty() && patents.isEmpty() && books.isEmpty()) {
            addText(doc, "（暂无已通过审核的前期成果，请补充。）");
            addBlank(doc);
        }

        // 三、占位说明
        addH2(doc, "三、研究内容与目标");
        addText(doc, "【请在此处补充研究内容、目标、关键技术问题】");
        addBlank(doc);

        addH2(doc, "四、研究方案与技术路线");
        addText(doc, "【请在此处补充研究方法、技术路线、实施步骤】");
        addBlank(doc);

        addH2(doc, "五、经费预算");
        addText(doc, "【请在此处列明经费预算明细】");
        addBlank(doc);

        addH2(doc, "六、预期成果");
        addText(doc, "【请在此处描述预期论文、专利、软著等成果产出】");
        addBlank(doc);

        // 输出
        String fileName = URLEncoder.encode(projectName + "_申报书.docx", StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        doc.write(response.getOutputStream());
        doc.close();
    }

    // === helpers ===
    private void addTitle(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setText(text); r.setFontSize(22); r.setBold(true);
        r.setFontFamily("黑体");
    }
    private void addCenter(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setText(text); r.setFontSize(10); r.setColor("888888");
    }
    private void addH2(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text); r.setFontSize(16); r.setBold(true);
        r.setFontFamily("黑体");
    }
    private void addH3(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text); r.setFontSize(13); r.setBold(true);
        r.setFontFamily("宋体");
    }
    private void addText(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text); r.setFontSize(11); r.setFontFamily("宋体");
    }
    private void addKV(XWPFDocument doc, String k, String v) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun rk = p.createRun();
        rk.setText(k + "：" + (v == null ? "" : v));
        rk.setFontSize(11); rk.setFontFamily("宋体");
    }
    private void addBlank(XWPFDocument doc) { doc.createParagraph(); }
    private String nz(Object o) { return o == null ? "" : o.toString(); }
}
