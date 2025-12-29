package org.lxly.blog.controller;

import lombok.*;
import org.lxly.blog.service.UploadService;
import org.lxly.blog.dto.response.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Result<UploadResult>> upload(@RequestPart("file") MultipartFile file) throws Exception {
        String url = uploadService.upload(file);
        return ResponseEntity.ok(Result.ok(new UploadResult(url)));
    }

    @Getter @AllArgsConstructor
    static class UploadResult {
        private String url;
    }
}
