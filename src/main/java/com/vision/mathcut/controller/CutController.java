package com.vision.mathcut.controller;

import com.vision.mathcut.dto.cut.CutDetail;
import com.vision.mathcut.dto.cut.CutListItem;
import com.vision.mathcut.dto.cut.CutSaveRequest;
import com.vision.mathcut.service.CutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cuts")
@RequiredArgsConstructor
public class CutController {

    private final CutService cutService;

    @GetMapping
    public ResponseEntity<List<CutListItem>> getMyCuts(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(cutService.getMyCuts(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CutDetail> getCut(@PathVariable Long id,
                                            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(cutService.getCut(id, userId));
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> saveCut(@Valid @RequestBody CutSaveRequest request,
                                                     @AuthenticationPrincipal Long userId) {
        Long id = cutService.saveCut(request, userId);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Long>> updateCut(@PathVariable Long id,
                                                       @Valid @RequestBody CutSaveRequest request,
                                                       @AuthenticationPrincipal Long userId) {
        Long updatedId = cutService.updateCut(id, request, userId);
        return ResponseEntity.ok(Map.of("id", updatedId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCut(@PathVariable Long id,
                                          @AuthenticationPrincipal Long userId) {
        cutService.deleteCut(id, userId);
        return ResponseEntity.noContent().build();
    }
}
