package me.moirai.storyengine.core.port.outbound.generation;

import java.util.List;

public interface EmbeddingPort {

    float[] embed(String text);

    List<float[]> embedAll(List<String> texts);
}
