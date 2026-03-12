package com.vision.mathcut.dto.cut;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CutDetail {
    private final Long id;
    private final String title;
    private final JsonNode canvasData;
    private final String thumbnail;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CutDetail(Long id, String title, JsonNode canvasData, String thumbnail,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.canvasData = canvasData;
        this.thumbnail = thumbnail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
