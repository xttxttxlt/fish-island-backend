package com.cong.fishisland.service;


import com.cong.fishisland.common.TestBase;
import com.cong.fishisland.manager.MinioManager;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
class FileTest extends TestBase {

    @Resource
    MinioManager minioManage;

    @Test
    void MinioPresignedUploadUrltest() throws IOException {
        // 1. 获取预签名 URL
        String fileName = "test.jpg";
        String uploadUrl = minioManage.generatePresignedUploadUrl(fileName);

        // 2. 读取文件
        Path filePath = Paths.get("src/main/resources/y31qYBxk-moyu.png");
        byte[] fileBytes = Files.readAllBytes(filePath);

        // 3. 发送 HTTP PUT 请求
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), fileBytes);
        Request request = new Request.Builder().url(uploadUrl).put(requestBody).build();

        Response response = client.newCall(request).execute();

        // 4. 断言上传成功
        assert response.isSuccessful();
        System.out.println("文件上传成功：" + uploadUrl);
        Assertions.assertTrue(true);
    }

    @Test
    void test111666ImageUpload() throws IOException {
        // 1. 读取本地文件
        Path filePath = Paths.get("src/main/resources/y31qYBxk-moyu.png");
        byte[] fileBytes = Files.readAllBytes(filePath);

        // 2. 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // 3. 创建请求体
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "test.png",
                        RequestBody.create(MediaType.parse("image/png"), fileBytes))
                .build();

        // 4. 创建请求
        Request request = new Request.Builder()
                .url("https://i.111666.best/image")
                .addHeader("Auth-Token", "YOUR-TOKEN")
                .post(requestBody)
                .build();

        // 5. 发送请求
        try (Response response = client.newCall(request).execute()) {
            // 6. 验证响应
            Assertions.assertTrue(response.isSuccessful(), "上传失败：" + response.code());
            String responseBody = response.body().string();
            log.info("上传成功，响应内容：{}", responseBody);
        }
    }
}
