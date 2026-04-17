package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import me.moirai.storyengine.core.port.outbound.ai.ImageGenerationPort;

@Component
public class OpenAiImageGenerationAdapter implements ImageGenerationPort {

    private final RestClient openAiClient;
    private final String imageUri;
    private final String model;
    private final String quality;

    public OpenAiImageGenerationAdapter(
            RestClient openAiClient,
            @Value("${moirai.openai.api.image-uri}") String imageUri,
            @Value("${moirai.image-generation.model}") String model,
            @Value("${moirai.image-generation.quality}") String quality) {

        this.openAiClient = openAiClient;
        this.imageUri = imageUri;
        this.model = model;
        this.quality = quality;
    }

    @Override
    public byte[] generate(String prompt) {

        var request = new ImageGenerationRequest(model, prompt, quality);
        var response = openAiClient.post()
                .uri(imageUri)
                .body(request)
                .retrieve()
                .body(ImageGenerationResponse.class);

        var b64 = response.data().get(0).b64Json();
        return Base64.getDecoder().decode(b64);
    }
}
