package com.school.research_system.controller;

import com.school.research_system.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 1. 获取原文件名
        String originalFilename = file.getOriginalFilename();

        // 2. 获取后缀名 (如 .pdf)
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 3. 生成唯一文件名 (防止重名覆盖)
        String newFileName = UUID.randomUUID().toString() + suffix;

        // 4. 创建保存目录
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // 5. 保存文件
            File dest = new File(uploadPath + newFileName);
            file.transferTo(dest);

            // 6. 返回访问 URL
            // 假设本地端口是 8080，这里拼接相对路径，前端加 host 或者后端把 host 拼全
            // 返回格式: /files/uuid.pdf
            String fileUrl = "/files/" + newFileName;
            return Result.success(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}