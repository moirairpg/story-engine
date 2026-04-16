package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.core.port.inbound.GenerateImage;
import me.moirai.storyengine.infrastructure.inbound.rest.request.ImageGenerationRequest;

@RestController
@RequestMapping("/image-generations")
public class ImageGenerationController {

    private final CommandRunner commandRunner;

    public ImageGenerationController(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @PostMapping
    public ResponseEntity<byte[]> generate(@RequestBody ImageGenerationRequest request) {
        var bytes = commandRunner.run(new GenerateImage(request.prompt()));

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(bytes);
    }
}
