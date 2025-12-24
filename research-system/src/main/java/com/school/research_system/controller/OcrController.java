package com.school.research_system.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.school.research_system.common.Result; // 你的统一返回类

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

@RestController
@RequestMapping("/ocr")
@CrossOrigin(origins = "*") // 允许跨域
public class OcrController {

    // 建议把 Key 配置在 application.yml 中： siliconflow.api-key: sk-xxxx
    // 这里为了演示方便直接写常量，实际使用请替换为你的真实 Key
    private static final String API_KEY = "Bearer sk-yyzcmqscjfsxezynzpfvdlpqarypqrlslvwkkgeuababdjmh";

    // 硅基流动的 API 地址 (兼容 OpenAI 格式)
    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";

    // 使用的模型：通义千问 Qwen2-VL (视觉理解能力极强)
    private static final String MODEL_NAME = "Qwen/Qwen2-VL-72B-Instruct";

    @PostMapping("/predict")
    public Result<String> predict(@RequestParam("file") MultipartFile file, @RequestParam("type") String type // ===
                                                                                                              // 接收前端传来的类型
                                                                                                              // ===) {
    ) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        try {
            // 1. 将图片转换为 Base64 编码
            String base64Image = convertFileToBase64(file);
            // === 核心修改：根据类型获取专属 Prompt ===
            String prompt = getPromptByType(type);
            if (base64Image == null) {
                return Result.error("不支持的文件格式，仅支持图片或 PDF");
            }

            // 3. 构造请求体
            String imageUrl = "data:image/jpeg;base64," + base64Image;

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", MODEL_NAME);
            payload.put("temperature", 0.1); // 低温度保证输出稳定
            payload.put("max_tokens", 1024);

            JSONObject textContent = new JSONObject().set("type", "text").set("text", prompt);
            JSONObject imageContent = new JSONObject().set("type", "image_url").set("image_url",
                    new JSONObject().set("url", imageUrl));

            JSONArray contentArray = new JSONArray();
            contentArray.add(textContent);
            contentArray.add(imageContent);

            JSONObject userMessage = new JSONObject().set("role", "user").set("content", contentArray);
            JSONArray messages = new JSONArray();
            messages.add(userMessage);

            payload.put("messages", messages);

            // 4. 调用 AI
            HttpResponse response = HttpRequest.post(API_URL)
                    .header("Authorization", API_KEY)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(payload))
                    .timeout(60000) // 设置超时 60s
                    .execute();

            if (!response.isOk()) {
                System.err.println("API Error: " + response.body());
                return Result.error("AI 识别服务异常: " + response.getStatus());
            }

            // 5. 解析 AI 返回的 JSON 字符串
            JSONObject jsonRes = JSONUtil.parseObj(response.body());
            String aiContent = jsonRes.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getStr("content");

            // 清洗数据：有时候 AI 会返回 ```json ... ```，需要去掉
            aiContent = cleanJsonString(aiContent);

            System.out.println("AI 提取结果: " + aiContent);

            // 转为 JSON 对象返回给前端
            JSONObject resultJson = JSONUtil.parseObj(aiContent);
            return Result.success(resultJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("系统异常: " + e.getMessage());
        }
    }

    /**
     * 根据成果类型生成专属 Prompt
     * 策略：告诉 AI 具体的提取字段，并强制 Key 的命名，方便前端直接用
     */
    /**
     * 根据你的 FORM_FIELDS 定制的 Prompt
     */
    private String getPromptByType(String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个科研成果提取助手。请识别文档内容，提取信息并严格以 JSON 格式返回。");

        // === 修改点 1：改变指令，启用“大脑”补全功能 ===
        sb.append("如果图片中未直接显示某些元数据（如ISSN、期刊分区、学科分类、出版社级别等），");
        sb.append("请利用你巨大的内部知识库，根据识别到的标题或期刊名自动补全这些信息。");
        sb.append("只有在完全无法确定时，才返回空字符串。");
        // ============================================

        sb.append("\n\n请严格提取以下字段（JSON Key 必须完全一致）：\n");
        switch (type) {
            case "paper": // 论文
                sb.append("- title (论文标题)\n");
                sb.append("- journalName (期刊名称)\n");
                sb.append("- issn (ISSN号)\n");
                sb.append("- impactFactor (影响因子，纯数字)\n");
                sb.append("- pages (页码范围)\n");
                sb.append("- isSci (是否SCI，只能返回'是'或'否')\n");
                sb.append("- sciPartition (分区，只能返回'一区','二区','三区','四区','无')\n");
                sb.append("- publishDate (发表日期，格式 YYYY-MM-DD)\n");
                sb.append("- authors (第一作者)\n");
                sb.append("- correspondingAuthor (通讯作者)\n");
                sb.append("- discipline (学科分类)\n");
                break;

            case "project": // 项目
                sb.append("- name (项目名称)\n");
                sb.append("- projectSource (项目来源)\n");
                sb.append("- level (级别，如：国家级,省部级,市级,校级)\n");
                sb.append("- projectNumber (项目编号)\n");
                sb.append("- funds (经费预算，纯数字)\n");
                sb.append("- startDate (开始日期，格式 YYYY-MM-DD)\n");
                sb.append("- endDate (结束日期，格式 YYYY-MM-DD)\n");
                sb.append("- leader (项目负责人)\n");
                sb.append("- participants (参与人员)\n");
                sb.append("- discipline (学科分类)\n");
                break;

            case "software": // 软著
                sb.append("- name (软件名称)\n");
                sb.append("- registerNo (登记号)\n"); // 注意：你前端是 registerNo
                sb.append("- grantDate (授权日期，格式 YYYY-MM-DD)\n");
                sb.append("- authors (作者)\n");
                sb.append("- softwareType (软件类别，如：应用软件,系统软件,工具软件)\n");
                break;

            case "patent": // 专利
                sb.append("- name (专利名称)\n");
                sb.append("- patentNo (专利号)\n"); // 注意：你前端是 patentNo
                sb.append("- patentType (专利类型，如：发明专利,实用新型,外观设计)\n");
                sb.append("- applyDate (申请日期，格式 YYYY-MM-DD)\n");
                sb.append("- grantDate (授权日期，格式 YYYY-MM-DD)\n");
                sb.append("- owner (专利权人)\n");
                break;

            case "book": // 专著
                sb.append("- name (书名)\n");
                sb.append("- isbn (ISBN)\n");
                sb.append("- publisher (出版社)\n");
                sb.append("- publishDate (出版日期，格式 YYYY-MM-DD)\n");
                sb.append("- authors (作者)\n");
                sb.append("- editor (主编/副主编)\n");
                sb.append("- bookType (出版类别，如：教材,学术专著,普及读物)\n");
                sb.append("- level (出版级别，如：国家级出版社,地方出版社)\n");
                break;
        }

        return sb.toString();
    }

    /**
     * 核心辅助方法：统一将 图片 或 PDF 转换为 Base64 图片字符串
     */
    private String convertFileToBase64(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null)
            return null;
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        // 1. 如果是图片，直接转 Base64
        if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")) {
            return Base64.encode(file.getBytes());
        }

        // 2. 如果是 PDF，将第一页转换为图片，再转 Base64
        if (ext.equals("pdf")) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                if (document.getNumberOfPages() > 0) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    // 渲染第一页 (pageIndex=0)，scale=2.0 保证清晰度
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);

                    // 转字节数组
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(bim, "jpg", os);
                    return Base64.encode(os.toByteArray());
                }
            }
        }
        return null; // 不支持的格式
    }

    /**
     * 清洗 AI 可能返回的 Markdown 标记
     */
    private String cleanJsonString(String content) {
        if (content == null)
            return "{}";
        content = content.trim();
        // 去掉 ```json 开头
        if (content.startsWith("```json")) {
            content = content.substring(7);
        }
        if (content.startsWith("```")) {
            content = content.substring(3);
        }
        // 去掉 ``` 结尾
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        return content.trim();
    }
}