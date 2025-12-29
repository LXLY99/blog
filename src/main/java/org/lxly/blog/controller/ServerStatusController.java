package org.lxly.blog.controller;

import lombok.*;
import org.lxly.blog.service.ServerStatusService;
import org.lxly.blog.dto.response.Result;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ServerStatusController {

    private final ServerStatusService statusService;

    @GetMapping("/server-status")
    public ResponseEntity<Result<Map<String, Object>>> status() {
        return ResponseEntity.ok(Result.ok(statusService.getStatus()));
    }
}
