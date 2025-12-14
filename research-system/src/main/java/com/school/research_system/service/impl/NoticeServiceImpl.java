package com.school.research_system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.research_system.entity.Notice;
import com.school.research_system.mapper.NoticeMapper;
import com.school.research_system.service.INoticeService; // 需自行创建接口
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {

    @Value("${file.upload-path}")
    private String uploadPath; // D:/school_files/

    /**
     * 读取附件内容转为文本
     */
    public String extractTextFromAttachment(Long noticeId) {
        Notice notice = this.getById(noticeId);
        if (notice == null || notice.getAttachmentUrl() == null) {
            return "没有附件";
        }

        // attachmentUrl 可能是 /files/xxx.pdf，需要转回本地物理路径
        // 假设 url 是 /files/abc.pdf -> 本地是 D:/school_files/abc.pdf
        String fileName = notice.getAttachmentUrl().replace("/files/", "");
        File file = new File(uploadPath + fileName);

        if (!file.exists()) {
            return "文件在服务器上已丢失";
        }

        try {
            if (fileName.endsWith(".docx")) {
                // 解析 Word
                FileInputStream fis = new FileInputStream(file);
                XWPFDocument doc = new XWPFDocument(fis);
                XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
                String text = extractor.getText();
                extractor.close();
                return text;
            } else if (fileName.endsWith(".pdf")) {
                // 解析 PDF
                PDDocument document = PDDocument.load(file);
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                document.close();
                return text;
            } else {
                return "该文件格式不支持在线预览，请下载查看(当前仅支持预览 Word(.docx) 和 PDF 格式的文档)";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "解析文件失败: " + e.getMessage();
        }
    }
}