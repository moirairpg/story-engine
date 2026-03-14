package me.moirai.storyengine.core.port.outbound.generation;

import java.util.List;
import java.util.Map;

import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;

public interface LorebookEnrichmentPort {

    Map<String, Object> enrichContextWithLorebookForRpg(List<DiscordMessageData> messagesExtracted, String worldId,
            ModelConfigurationRequest modelConfiguration);

    Map<String, Object> enrichContextWithLorebook(List<DiscordMessageData> messagesExtracted, String worldId,
            ModelConfigurationRequest modelConfiguration);
}
