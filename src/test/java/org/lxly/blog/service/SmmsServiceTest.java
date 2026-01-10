package org.lxly.blog.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SmmsServiceTest {

    @Autowired
    private SmmsService smmsService;

    @Test
    @Disabled("Manual test only to prevent spamming SMMS")
    void testRealUploadLocalImage() throws Exception {

        Path path = Path.of("F:\\Py-Project\\GL-Blog-main\\BG\\icon.jpg");
        // Path path = Path.of("F:\\Py-Project\\GL-Blog-main\\BG\\icon.png");

        assertTrue(Files.exists(path), "测试图片不存在: " + path);
        assertTrue(Files.isRegularFile(path), "不是普通文件: " + path);

        byte[] bytes = Files.readAllBytes(path);
        assertTrue(bytes.length > 0, "图片内容为空: " + path);

        String filename = path.getFileName().toString();
        String lower = filename.toLowerCase();
        String contentType;
        if (lower.endsWith(".png")) {
            contentType = "image/png";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (lower.endsWith(".gif")) {
            contentType = "image/gif";
        } else {
            fail("不支持的图片格式: " + filename);
            return;
        }

        MockMultipartFile file = new MockMultipartFile(
                "smfile",
                filename,
                contentType,
                bytes
        );

        SmmsService.ImageResponse resp = smmsService.upload(file);

        assertNotNull(resp, "upload() 返回为 null");
        assertNotNull(resp.getUrl(), "返回的 url 为 null");
        assertTrue(resp.getUrl().startsWith("http"), "返回的 URL 不合法: " + resp.getUrl());

        System.out.println("✅ SMMS Upload Success!");
        System.out.println("URL  = " + resp.getUrl());
        System.out.println("HASH = " + resp.getHash());

        if (resp.getHash() != null) {
            assertFalse(resp.getHash().isBlank(), "hash 不应为空字符串");
        }
    }

    /**
     * 手动删除：你填入 hash + url
     * ⚠️ 会真实删除 SM.MS 上的图片
     */
    @Test
    @Disabled("Manual test only - will really delete on SMMS")
    void testDeleteByHashAndUrl() {
        String hash = "PUT_HASH_HERE";
        String url  = "PUT_URL_HERE"; // 用于默认头像保护判断（建议填真实 url）

        assertNotNull(hash);
        assertFalse(hash.isBlank());

        smmsService.delete(hash, url);
        System.out.println("🗑️ Delete request sent. hash=" + hash + " url=" + url);
    }

    /**
     * 自动删除：先上传一张图片，再删除它（更自动化）
     * ⚠️ 会真实删除 SM.MS 上的图片
     */
    @Test
    @Disabled("Manual test only - uploads then deletes on SMMS")
    void testUploadThenDelete() throws Exception {
        Path path = Path.of("F:\\Py-Project\\GL-Blog-main\\BG\\icon.jpg");
        byte[] bytes = Files.readAllBytes(path);

        MockMultipartFile file = new MockMultipartFile(
                "smfile",
                "icon.jpg",
                "image/jpeg",
                bytes
        );

        SmmsService.ImageResponse resp = smmsService.upload(file);
        assertNotNull(resp);
        assertNotNull(resp.getUrl());
        System.out.println("Uploaded URL=" + resp.getUrl() + " hash=" + resp.getHash());

        // 如果 hash 为 null（重复图），则无法删
        assertNotNull(resp.getHash(), "hash 为 null（可能是 image_repeated），无法删除");

        smmsService.delete(resp.getHash(), resp.getUrl());
        System.out.println("Deleted hash=" + resp.getHash());
    }

    @Test
    @Disabled("Manual test only to prevent spamming SMMS")
    void testBatchUploadBGImages_1_to_6() throws Exception {

        Path dir = Path.of("F:\\Java-Project\\Blog\\src\\main\\resources\\static\\BG");

        assertTrue(Files.exists(dir), "目录不存在: " + dir);
        assertTrue(Files.isDirectory(dir), "不是目录: " + dir);

        Map<String, SmmsService.ImageResponse> results = new LinkedHashMap<>();

        for (int i = 1; i <= 6; i++) {
            String filename = i + ".jpg";
            Path path = dir.resolve(filename);

            assertTrue(Files.exists(path), "图片不存在: " + path);
            assertTrue(Files.isRegularFile(path), "不是普通文件: " + path);

            byte[] bytes = Files.readAllBytes(path);
            assertTrue(bytes.length > 0, "图片内容为空: " + path);

            MockMultipartFile file = new MockMultipartFile(
                    "smfile",
                    filename,
                    "image/jpeg",
                    bytes
            );

            SmmsService.ImageResponse resp = smmsService.upload(file);

            assertNotNull(resp, "upload() 返回为 null: " + filename);
            assertNotNull(resp.getUrl(), "返回 url 为 null: " + filename);
            assertTrue(resp.getUrl().startsWith("http"), "返回 URL 不合法: " + resp.getUrl());

            results.put(filename, resp);

            System.out.printf("✅ Uploaded %-6s | url=%s | hash=%s%n",
                    filename, resp.getUrl(), resp.getHash());
        }

        System.out.println("\n===== SUMMARY (filename -> url) =====");
        results.forEach((name, resp) -> System.out.printf("%s -> %s%n", name, resp.getUrl()));

        System.out.println("\n===== SUMMARY (filename -> hash) =====");
        results.forEach((name, resp) -> System.out.printf("%s -> %s%n", name, resp.getHash()));
    }
}
