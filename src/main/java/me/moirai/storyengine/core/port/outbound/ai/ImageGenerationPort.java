package me.moirai.storyengine.core.port.outbound.ai;

public interface ImageGenerationPort {

    byte[] generate(String prompt);
}
