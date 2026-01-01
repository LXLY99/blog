package org.lxly.blog.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadService {

    /** MinIO 访问地址 */
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

    /** 创建 MinIO 客户端 */
    private MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 上传文件并返回可直接访问的 URL
     * 自动处理 Bucket 创建和权限设置
     */
    public String upload(MultipartFile file) throws Exception {
        MinioClient client = minioClient();

        // 1️⃣ 检查存储桶是否存在
        boolean exists = client.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build());

        if (!exists) {
            log.info("Bucket '{}' 不存在，正在创建...", bucket);
            // 创建 Bucket
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());

            // 🔥【核心修复】设置存储桶策略为 "Public Read"（公开读）
            // 这样前端 <img> 标签才能直接加载图片，无需 Token
            String policyJson = """
                {
                  "Statement": [
                    {
                      "Action": ["s3:GetObject"],
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ],
                  "Version": "2012-10-17"
                }
                """.formatted(bucket);

            client.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucket)
                            .config(policyJson)
                            .build()
            );
            log.info("✅ 已自动将 Bucket '{}' 设置为公开访问 (Public Read)", bucket);
        }

        // 2️⃣ 生成唯一文件名：日期/UUID.后缀
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) originalFilename = "unknown.file";

        String suffix = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase()
                : "";

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

        // 4️⃣ 返回完整 URL
        // 格式：http://127.0.0.1:9000/blog/2025-01-01/uuid.jpg
        return endpoint + "/" + bucket + "/" + objectName;
    }
}