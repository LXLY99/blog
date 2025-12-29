package org.lxly.blog.controller;

import lombok.*;
import org.lxly.blog.dto.response.*;
import org.lxly.blog.service.SettingsService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/settings")
    public ResponseEntity<Result<SettingsDto>> getSettings() {
        return ResponseEntity.ok(Result.ok(settingsService.getSettings()));
    }

    @PostMapping("/setup")
    public ResponseEntity<Result<SettingsDto>> setup(@RequestBody SettingsDto dto) {
        SettingsDto saved = settingsService.initOrUpdate(dto);
        return ResponseEntity.ok(Result.ok(saved));
    }

    @GetMapping("/about")
    public ResponseEntity<Result<SettingsDto>> getAbout() {
        // 这里实际上返回 Settings 中的 notice 字段（已渲染为 HTML）
        SettingsDto dto = settingsService.getAbout();
        return ResponseEntity.ok(Result.ok(dto));
    }
}
