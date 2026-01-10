package org.lxly.blog.controller;

import lombok.RequiredArgsConstructor;
import org.lxly.blog.dto.response.Result;
import org.lxly.blog.service.SmmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final SmmsService smmsService;

    @PostMapping("/upload")
    public ResponseEntity<Result<Map<String, String>>> upload(@RequestParam("file") MultipartFile file) {
        SmmsService.ImageResponse resp = smmsService.upload(file);

        // Return both URL and Hash
        return ResponseEntity.ok(Result.ok(Map.of(
                "url", resp.getUrl(),
                "hash", resp.getHash() != null ? resp.getHash() : ""
        )));
    }
}