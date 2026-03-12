package com.vision.mathcut.dto.cut;

import com.vision.mathcut.domain.Cut;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CutListItem {
    private final Long id;
    private final String title;
    private final String thumbnail;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CutListItem(Cut cut) {
        this.id = cut.getId();
        this.title = cut.getTitle();
        this.thumbnail = cut.getThumbnail();
        this.createdAt = cut.getCreatedAt();
        this.updatedAt = cut.getUpdatedAt();
    }
}
