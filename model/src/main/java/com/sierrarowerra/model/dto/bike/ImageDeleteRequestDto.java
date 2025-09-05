package com.sierrarowerra.model.dto.bike;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageDeleteRequestDto {

    @Schema(description = "URL of the image to be deleted",
            example = "https://example.com/path/to/image.jpg",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Image URL cannot be blank")
    private String imageUrl;
}
