package me.moirai.storyengine.infrastructure.outbound.adapter.request;

import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;

public class StoryGenerationRequestFixture {

    public static StoryGenerationRequest.Builder create() {

        return StoryGenerationRequest.builder()
                .botId("BOTID")
                .botUsername("TestBot")
                .botNickname("BotNickname")
                .channelId("CHNLID")
                .guildId("GLDID")
                .adventureId("WRLDID")
                .personaId("PRSNID")
                .gameMode("CHAT")
                .authorsNote("Author's note")
                .nudge("Nudge")
                .remember("Remember")
                .bump("Bump")
                .bumpFrequency(4)
                .modelConfiguration(ModelConfigurationRequestFixture.gpt4Mini().build())
                .moderation(ModerationConfigurationRequestFixture.withFlags());
    }
}
