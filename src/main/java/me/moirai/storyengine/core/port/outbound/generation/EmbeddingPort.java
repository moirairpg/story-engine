package me.moirai.storyengine.core.port.outbound.generation;

public interface EmbeddingPort {

    float[] embed(String text);
}
