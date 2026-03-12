package com.vision.mathcut.controller;

import com.vision.mathcut.dto.AISketchRequest;
import com.vision.mathcut.service.AISketchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AISketchController {

    private final AISketchService aiSketchService;

    @PostMapping("/sketch")
    public ResponseEntity<Object> analyzeSketch(@Valid @RequestBody AISketchRequest request) {
        Object result = aiSketchService.analyze(
                request.getImageDataUrl(),
                request.getForcedShapeType(),
                request.getUserHint()
        );
        return ResponseEntity.ok(result);
    }
}
