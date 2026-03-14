package me.moirai.storyengine.core.application.helper;

import java.util.List;
import java.util.Map;

import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageData;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequest;

public interface LorebookEnrichmentHelper {

    Map<String, Object> enrichContextWithLorebookForRpg(List<DiscordMessageData> messagesExtracted, String worldId,
            ModelConfigurationRequest modelConfiguration);

    Map<String, Object> enrichContextWithLorebook(List<DiscordMessageData> messagesExtracted, String worldId,
            ModelConfigurationRequest modelConfiguration);
}
