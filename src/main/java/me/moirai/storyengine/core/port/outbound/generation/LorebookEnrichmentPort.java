package me.moirai.storyengine.core.port.outbound.generation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.moirai.storyengine.core.port.outbound.discord.DiscordMessageData;

public interface LorebookEnrichmentPort {

    Map<String, Object> enrichContextWithLorebookForRpg(
            List<DiscordMessageData> messagesExtracted,
            UUID adventureId,
            ModelConfigurationRequest modelConfiguration);

    Map<String, Object> enrichContextWithLorebook(
            List<DiscordMessageData> messagesExtracted,
            UUID adventureId,
            ModelConfigurationRequest modelConfiguration);
}
