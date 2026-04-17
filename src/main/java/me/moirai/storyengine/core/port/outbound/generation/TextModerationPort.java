package me.moirai.storyengine.core.port.outbound.generation;

import java.util.List;

public interface TextModerationPort {

    List<TextModerationResult> moderate(String... texts);
}
