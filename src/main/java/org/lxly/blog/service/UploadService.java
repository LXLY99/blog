package org.lxly.blog.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;   // ✅ Spring 的 @Value
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UploadService {

    /** MinIO 访问地址（例如 http://127.0.0.1:9000） */
    @Value("${minio.endpoint}")
    private String endpoint;

    /** MinIO Access Key */
    @Value("${minio.access-key}")
    private String accessKey;

    /** MinIO Secret Key */
    @Value("${minio.secret-key}")
    private String secretKey;

    /** 存储桶名称 */
    @Value("${minio.bucket}")
    private String bucket;

    /** 创建 MinIO 客户端（每次调用都会返回同一实例） */
    private MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 单文件上传（图片、封面等），返回可直接作为 <img src=""> 使用的 URL。
     *
     * @param file 前端上传的 MultipartFile
     * @return 完整 URL（例如 http://127.0.0.1:9000/blog/2024/09/13/…/xxx.png）
     */
    public String upload(MultipartFile file) throws Exception {
        MinioClient client = minioClient();

        // 1️⃣ 确保 bucket 已经存在
        boolean exists = client.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }

        // 2️⃣ 生成唯一对象名：date/uuid + 原始后缀
        String suffix = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf('.')).toLowerCase();
        String objectName = String.format("%s/%s%s",
                LocalDate.now(),
                UUID.randomUUID().toString().replaceAll("-", ""),
                suffix);

        // 3️⃣ 上传文件
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

        // 4️⃣ 返回可以直接访问的 URL
        return endpoint + "/" + bucket + "/" + objectName;
    }
}
