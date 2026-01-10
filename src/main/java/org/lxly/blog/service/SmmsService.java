package org.lxly.blog.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmmsService {

    @Value("${smms.token}")
    private String apiToken;

    @Value("${smms.default-avatar}")
    private String defaultAvatar;

    // Helper class to return both data points
    @Data
    @Builder
    public static class ImageResponse {
        private String url;
        private String hash;
    }

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    /**
     * Uploads file and returns URL + Hash object
     */
    public ImageResponse upload(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("File is empty");

        try {
            RequestBody fileBody = RequestBody.create(
                    file.getBytes(),
                    MediaType.parse(file.getContentType() != null ? file.getContentType() : "image/jpeg")
            );

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("smfile", file.getOriginalFilename(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url("https://sm.ms/api/v2/upload")
                    .addHeader("Authorization", apiToken)
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new RuntimeException("SMMS API Error: " + response.code());

                String responseStr = response.body().string();
                log.info("SMMS Upload Response: {}", responseStr);

                JsonObject json = gson.fromJson(responseStr, JsonObject.class);
                boolean success = json.get("success").getAsBoolean();

                if (success) {
                    JsonObject data = json.getAsJsonObject("data");
                    return ImageResponse.builder()
                            .url(data.get("url").getAsString())
                            .hash(data.get("hash").getAsString())
                            .build();
                } else {
                    String code = json.get("code").getAsString();
                    if ("image_repeated".equals(code)) {
                        // If repeated, we only get the URL, no hash is returned by SMMS
                        return ImageResponse.builder()
                                .url(json.get("images").getAsString())
                                .hash(null) // Cannot delete duplicates via API later
                                .build();
                    }
                    throw new RuntimeException("Upload Failed: " + json.get("message").getAsString());
                }
            }
        } catch (IOException e) {
            log.error("Upload error", e);
            throw new RuntimeException("Network error uploading to SMMS");
        }
    }

    /**
     * Deletes image using Hash, BUT checks URL first to protect default avatar.
     * @param hash The deletion hash from SMMS
     * @param url The image URL (used for safety check)
     */
    public void delete(String hash, String url) {
        // 1. Safety Check: Is this the protected default avatar?
        if (url != null && url.equals(defaultAvatar)) {
            log.warn("🛡️ SAFETY: Prevented deletion of Default Avatar: {}", url);
            return;
        }

        // 2. Validate Hash
        if (hash == null || hash.isEmpty()) {
            return;
        }

        // 3. Execute Delete
        String deleteUrl = "https://sm.ms/api/v2/delete/" + hash;

        Request request = new Request.Builder()
                .url(deleteUrl)
                .addHeader("Authorization", apiToken)
                .addHeader("User-Agent", "Mozilla/5.0")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String respStr = response.body().string();
            log.info("🗑️ SMMS Delete Result (Hash: {}): {}", hash, respStr);
        } catch (IOException e) {
            log.error("Failed to delete image on SMMS", e);
        }
    }
}