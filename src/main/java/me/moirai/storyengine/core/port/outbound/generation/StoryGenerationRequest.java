package me.moirai.storyengine.core.port.outbound.generation;

import java.util.List;
import java.util.UUID;

import me.moirai.storyengine.core.port.outbound.discord.DiscordMessageData;

public record StoryGenerationRequest(

        String botId,
        String botUsername,
        String botNickname,
        String channelId,
        String guildId,
        UUID adventureId,
        UUID personaId,
        String gameMode,
        String nudge,
        String authorsNote,
        String remember,
        String bump,
        int bumpFrequency,
        ModelConfigurationRequest modelConfiguration,
        ModerationConfigurationRequest moderation,
        List<DiscordMessageData> messageHistory) {

    public StoryGenerationRequest {
        messageHistory = List.copyOf(messageHistory);
    }
}