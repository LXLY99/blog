package org.lxly.blog;


import org.junit.jupiter.api.Test;
import org.lxly.blog.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// This loads the full application context, including properties from application.yml
@SpringBootTest
public class UploadServiceTest {

    @Autowired
    private UploadService uploadService;

    @Test
    public void testRealUploadToMinIO() {
        try {
            // 1. Create a dummy file
            String content = "Hello MinIO, this is a test upload!";
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "integration-test.txt",
                    "text/plain",
                    content.getBytes(StandardCharsets.UTF_8)
            );

            // 2. Call the service (this will try to connect to localhost:9000)
            System.out.println("Attempting to upload file to MinIO...");
            String url = uploadService.upload(file);

            // 3. Verify
            System.out.println("Upload successful! URL: " + url);
            assertNotNull(url);
            assertTrue(url.contains("http://127.0.0.1:9000/blog/")); // Matches endpoint/bucket in your YML

        } catch (Exception e) {
            e.printStackTrace();
            // Fail the test if connection is refused or auth fails
            throw new RuntimeException("MinIO upload failed. Is MinIO running on port 9000?", e);
        }
    }
}