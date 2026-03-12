package com.vision.mathcut.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AISketchRequest {

    @NotBlank
    private String imageDataUrl;

    @NotBlank
    private String forcedShapeType;

    private String userHint;
}
