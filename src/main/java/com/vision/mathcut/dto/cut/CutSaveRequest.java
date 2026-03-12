package com.vision.mathcut.dto.cut;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CutSaveRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotNull
    private JsonNode canvasData;

    private String thumbnail;
}
