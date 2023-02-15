package com.study.controller;

import com.study.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basepath;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("获取文件：{}", file.toString());
        File dir = new File(basepath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //获取一下传入的原文件名
        String originalFilename = file.getOriginalFilename();
        //我们只需要获取一下格式后缀，取子串，起始点为最后一个.
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //为了防止出现重复的文件名，我们需要使用UUID
        String fileName = UUID.randomUUID() + suffix;
        try {
            //我们将其转存为E盘下的test.jpg
            file.transferTo(new File(basepath +"\\" + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        FileInputStream fis = null;
        ServletOutputStream os = null;
        try {
            fis = new FileInputStream(basepath + name);
            os = response.getOutputStream();
            response.setContentType("image/jpeg");
            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1)
                os.write(buffer, 0, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {

                try {
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
