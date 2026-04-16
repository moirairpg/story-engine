package me.moirai.storyengine.infrastructure.inbound.rest.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.ValidFile;

public record UploadImageRequest(
        @NotNull @ValidFile(types = {"image/jpeg", "image/png"}) MultipartFile file) {
}
